package com.example.arki_deportes.ui.equipos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.utils.Validations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

data class EquipoFormData(
    val codigoCampeonato: String = "",
    val codigoEquipo: String = "",
    val nombreCorto: String = "",
    val nombreCompleto: String = "",
    val provincia: String = "",
    val fechaAlta: String = LocalDate.now().format(DATE_FORMATTER),
    val escudoLocal: String = "",
    val escudoLink: String = ""
)

data class EquipoFormUiState(
    val formData: EquipoFormData = EquipoFormData(),
    val campeonatos: List<Campeonato> = emptyList(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showValidationErrors: Boolean = false,
    val message: FormMessage? = null,
    val shouldClose: Boolean = false,
    val isCreatingMassive: Boolean = false,
    val massiveCreationProgress: Int = 0,
    val massiveCreationTotal: Int = 0
)

class EquipoFormViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipoFormUiState())
    val uiState: StateFlow<EquipoFormUiState> = _uiState

    private var observeCampeonatosJob: Job? = null
    private var originalEquipo: Equipo? = null

    init {
        observeCampeonatos()
    }



    fun loadEquipo(codigoEquipo: String?) {
        if (codigoEquipo.isNullOrBlank()) {
            _uiState.update { EquipoFormUiState(campeonatos = it.campeonatos) }
            originalEquipo = null
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            try {
                val equipo = repository.getEquipo(codigoEquipo)
                if (equipo != null) {
                    originalEquipo = equipo
                    _uiState.update {
                        it.copy(
                            formData = EquipoFormData(
                                codigoCampeonato = equipo.CODIGOCAMPEONATO,
                                codigoEquipo = equipo.CODIGOEQUIPO,
                                nombreCorto = equipo.EQUIPO,
                                nombreCompleto = equipo.EQUIPO_NOMBRECOMPLETO,
                                provincia = equipo.PROVINCIA,
                                fechaAlta = equipo.FECHAALTA.ifBlank { currentDate() },
                                escudoLocal = equipo.ESCUDOLOCAL,
                                escudoLink = equipo.ESCUDOLINK
                            ),
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = FormMessage("No se encontró el equipo solicitado", isError = true)
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
                    _uiState.update { it.copy(campeonatos = campeonatos) }
                }
        }
    }

    fun onCampeonatoSelected(codigo: String) {
        updateForm { copy(codigoCampeonato = codigo) }
    }

    fun onNombreCortoChange(value: String) = updateForm { copy(nombreCorto = value) }

    fun onNombreCompletoChange(value: String) = updateForm { copy(nombreCompleto = value) }

    fun onProvinciaChange(value: String) = updateForm { copy(provincia = value) }

    fun onEscudoLocalChange(value: String) = updateForm { copy(escudoLocal = value) }

    fun onEscudoLinkChange(value: String) = updateForm { copy(escudoLink = value) }

    fun onFechaAltaChange(value: String) = updateForm { copy(fechaAlta = value) }

    private fun updateForm(transform: EquipoFormData.() -> EquipoFormData) {
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

    fun saveEquipo() {
        val form = _uiState.value.formData
        val timestamp = System.currentTimeMillis()
        val codigo = if (form.codigoEquipo.isNotBlank()) {
            form.codigoEquipo
        } else {
            generateCodigo(form.nombreCorto, timestamp)
        }

        val equipo = Equipo(
            CODIGOEQUIPO = codigo,
            EQUIPO = form.nombreCorto,
            PROVINCIA = form.provincia,
            FECHAALTA = form.fechaAlta.ifBlank { currentDate() },
            ESCUDOLOCAL = form.escudoLocal,
            ESCUDOLINK = form.escudoLink,
            CODIGOCAMPEONATO = form.codigoCampeonato,
            EQUIPO_NOMBRECOMPLETO = form.nombreCompleto.ifBlank { form.nombreCorto },
            TIMESTAMP_CREACION = originalEquipo?.TIMESTAMP_CREACION ?: timestamp,
            TIMESTAMP_MODIFICACION = timestamp,
            ORIGEN = Constants.ORIGEN_MOBILE
        )

        val validationError = Validations.validarEquipo(equipo)
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
                repository.saveEquipo(equipo)
                originalEquipo = equipo
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

    fun deleteEquipo() {
        val codigo = _uiState.value.formData.codigoEquipo
        if (codigo.isBlank() || _uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deleteEquipo(codigo)
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
        val base = nombre.uppercase(Locale.getDefault())
            .replace("[^A-Z0-9]".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        val safeBase = if (base.isBlank()) "EQUIPO" else base
        return "${safeBase}_${timestamp}"
    }

    private fun currentDate(): String = LocalDate.now().format(DATE_FORMATTER)

    fun crearEquiposPorProvincia() {
        val codigoCampeonato = _uiState.value.formData.codigoCampeonato

        if (codigoCampeonato.isBlank()) {
            _uiState.update {
                it.copy(
                    message = FormMessage("Debe seleccionar un campeonato primero", isError = true)
                )
            }
            return
        }

        viewModelScope.launch {
            val provincias = Constants.ProvinciasEcuador.TODAS
            _uiState.update {
                it.copy(
                    isCreatingMassive = true,
                    massiveCreationTotal = provincias.size,
                    massiveCreationProgress = 0
                )
            }

            try {
                provincias.forEachIndexed { index, provincia ->
                    val timestamp = System.currentTimeMillis() + index
                    val nombreCorto = provincia
                    val codigo = generateCodigo(nombreCorto, timestamp)
                    val fechaActual = currentDate()

                    val equipo = Equipo(
                        CODIGOEQUIPO = codigo,
                        EQUIPO = nombreCorto,
                        PROVINCIA = provincia,
                        FECHAALTA = fechaActual,
                        ESCUDOLOCAL = "",
                        ESCUDOLINK = "",
                        CODIGOCAMPEONATO = codigoCampeonato,
                        EQUIPO_NOMBRECOMPLETO = "SELECCIÓN $provincia",
                        TIMESTAMP_CREACION = timestamp,
                        TIMESTAMP_MODIFICACION = timestamp,
                        ORIGEN = Constants.ORIGEN_MOBILE
                    )

                    repository.saveEquipo(equipo)

                    // Actualizar progreso
                    _uiState.update {
                        it.copy(massiveCreationProgress = index + 1)
                    }

                    // Pequeña pausa para no saturar Firebase
                    delay(100)
                }

                _uiState.update {
                    it.copy(
                        isCreatingMassive = false,
                        massiveCreationProgress = 0,
                        massiveCreationTotal = 0,
                        message = FormMessage("✓ ${provincias.size} equipos creados exitosamente")
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingMassive = false,
                        massiveCreationProgress = 0,
                        massiveCreationTotal = 0,
                        message = FormMessage(
                            "Error al crear equipos: ${e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO}",
                            isError = true
                        )
                    )
                }
            }
        }
    }
}
