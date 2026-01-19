package com.example.arki_deportes.ui.grupos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.utils.Validations
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado del formulario de grupos.
 */
data class GrupoFormData(
    val codigoCampeonato: String = "",
    val codigoGrupo: String = "",
    val nombre: String = "",
    val provincia: String = "",
    val anio: String = "",
    val codigoProvincia: String = ""
)

data class GrupoFormUiState(
    val formData: GrupoFormData = GrupoFormData(),
    val campeonatos: List<Campeonato> = emptyList(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showValidationErrors: Boolean = false,
    val message: FormMessage? = null,
    val shouldClose: Boolean = false
)

class GrupoFormViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrupoFormUiState())
    val uiState: StateFlow<GrupoFormUiState> = _uiState

    private var observeCampeonatosJob: Job? = null
    private var originalGrupo: Grupo? = null

    init {
        observeCampeonatos()
    }

    /*DESACTIVADO TEMPORALMENTE
    fun loadGrupo(codigoGrupo: String?) {
        if (codigoGrupo.isNullOrBlank()) {
            _uiState.update { GrupoFormUiState(campeonatos = it.campeonatos) }
            originalGrupo = null
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            try {
                val campeonatoCodigo =
                    _uiState.value.formData.codigoCampeonato.ifBlank {
                        com.example.arki_deportes.data.context.CampeonatoContext
                            .campeonatoActivo.value?.CODIGO.orEmpty()
                    }

                val grupo = repository.getGrupo(campeonatoCodigo, codigoGrupo)
                if (grupo != null) {
                    originalGrupo = grupo
                    _uiState.update {
                        it.copy(
                            formData = GrupoFormData(
                                codigoCampeonato = grupo.CODIGOCAMPEONATO,
                                codigoGrupo = grupo.CODIGOGRUPO,
                                nombre = grupo.GRUPO,
                                provincia = grupo.PROVINCIA,
                                anio = if (grupo.ANIO == 0) "" else grupo.ANIO.toString(),
                                codigoProvincia = grupo.CODIGOPROVINCIA
                            ),
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = FormMessage("No se encontró el grupo solicitado", isError = true)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = FormMessage(e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true)
                    )
                }
            }
        }
    }
*/
    private fun observeCampeonatos() {
        observeCampeonatosJob?.cancel()
        observeCampeonatosJob = viewModelScope.launch {
            repository.observeCampeonatos()
                .catch { error ->
                    _uiState.update {
                        it.copy(message = FormMessage(error.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true))
                    }
                }
                .collect { campeonatos ->
                    _uiState.update {
                        it.copy(campeonatos = campeonatos)
                    }
                }
        }
    }

    fun onCampeonatoSelected(codigo: String) {
        updateForm { copy(codigoCampeonato = codigo) }
    }

    fun onNombreChange(value: String) = updateForm { copy(nombre = value) }

    fun onProvinciaChange(value: String) = updateForm { copy(provincia = value) }

    fun onAnioChange(value: String) = updateForm { copy(anio = value.filter { it.isDigit() }) }

    fun onCodigoProvinciaChange(value: String) = updateForm { copy(codigoProvincia = value) }

    private fun updateForm(transform: GrupoFormData.() -> GrupoFormData) {
        _uiState.update {
            it.copy(
                formData = it.formData.transform(),
                showValidationErrors = false,
                message = null
            )
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun onCloseConsumed() {
        _uiState.update { it.copy(shouldClose = false) }
    }

    fun saveGrupo() {
        val form = _uiState.value.formData
        val timestamp = System.currentTimeMillis()
        val anio = form.anio.toIntOrNull() ?: LocalDate.now().year
        val codigo = if (form.codigoGrupo.isNotBlank()) {
            form.codigoGrupo
        } else {
            generateCodigo(form.nombre, timestamp)
        }

        val grupo = Grupo(
            CODIGOCAMPEONATO = form.codigoCampeonato,
            CODIGOGRUPO = codigo,
            GRUPO = form.nombre,
            PROVINCIA = form.provincia,
            ANIO = anio,
            CODIGOPROVINCIA = form.codigoProvincia,
            TIMESTAMP_CREACION = originalGrupo?.TIMESTAMP_CREACION ?: timestamp,
            TIMESTAMP_MODIFICACION = timestamp,
            ORIGEN = Constants.ORIGEN_MOBILE
        )

        val validationError = Validations.validarGrupo(grupo)
        if (validationError != null) {
            _uiState.update {
                it.copy(
                    showValidationErrors = true,
                    message = FormMessage(validationError, isError = true)
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveGrupo(grupo)
                originalGrupo = grupo
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditMode = true,
                        shouldClose = true,
                        message = FormMessage(Constants.Mensajes.EXITO_GUARDAR)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = FormMessage(e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true)
                    )
                }
            }
        }
    }

    fun deleteGrupo() {
        val codigo = _uiState.value.formData.codigoGrupo
        if (codigo.isBlank() || _uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deleteGrupo(codigo)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        shouldClose = true,
                        message = FormMessage(Constants.Mensajes.EXITO_ELIMINAR)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        message = FormMessage(e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true)
                    )
                }
            }
        }
    }

    private fun generateCodigo(nombre: String, timestamp: Long): String {
        val base = nombre.uppercase()
            .replace("[^A-Z0-9]".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        val safeBase = if (base.isBlank()) "GRUPO" else base
        return "${safeBase}_${timestamp}"
    }
}
