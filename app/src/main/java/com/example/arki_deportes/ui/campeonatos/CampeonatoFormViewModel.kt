package com.example.arki_deportes.ui.campeonatos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.utils.Validations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

/**
 * Estado del formulario de campeonatos.
 */
data class CampeonatoFormData(
    val codigo: String = "",
    val nombre: String = "",
    val fechaAlta: String = LocalDate.now().format(DATE_FORMATTER),
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val provincia: String = "",
    val anio: String = "",
    val hashtags: String = ""
)

data class CampeonatoFormUiState(
    val formData: CampeonatoFormData = CampeonatoFormData(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showValidationErrors: Boolean = false,
    val message: FormMessage? = null,
    val shouldClose: Boolean = false
)

class CampeonatoFormViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampeonatoFormUiState())
    val uiState: StateFlow<CampeonatoFormUiState> = _uiState

    private var originalCampeonato: Campeonato? = null

    fun loadCampeonato(codigo: String?) {
        if (codigo.isNullOrBlank()) {
            _uiState.update {
                CampeonatoFormUiState(formData = CampeonatoFormData())
            }
            originalCampeonato = null
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            try {
                val campeonato = repository.getCampeonato(codigo)
                if (campeonato != null) {
                    originalCampeonato = campeonato
                    _uiState.update {
                        it.copy(
                            formData = CampeonatoFormData(
                                codigo = campeonato.CODIGO,
                                nombre = campeonato.CAMPEONATO,
                                fechaAlta = campeonato.FECHAALTA.ifBlank { currentDate() },
                                fechaInicio = campeonato.FECHAINICIO,
                                fechaFin = campeonato.FECHAFINAL,
                                provincia = campeonato.PROVINCIA,
                                anio = if (campeonato.ANIO == 0) "" else campeonato.ANIO.toString(),
                                hashtags = campeonato.HASTAGEXTRAS
                            ),
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            message = FormMessage("No se encontró el campeonato solicitado", isError = true),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = FormMessage(e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNombreChange(value: String) = updateForm { copy(nombre = value) }

    fun onFechaInicioChange(value: String) = updateForm { copy(fechaInicio = value) }

    fun onFechaFinChange(value: String) = updateForm { copy(fechaFin = value) }

    fun onProvinciaChange(value: String) = updateForm { copy(provincia = value) }

    fun onAnioChange(value: String) = updateForm { copy(anio = value.filter { it.isDigit() }) }

    fun onHashtagsChange(value: String) = updateForm { copy(hashtags = value) }

    private fun updateForm(transform: CampeonatoFormData.() -> CampeonatoFormData) {
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

    fun saveCampeonato() {
        val currentState = _uiState.value
        if (currentState.isSaving) return

        val form = currentState.formData
        val timestamp = System.currentTimeMillis()
        val anio = form.anio.toIntOrNull() ?: deriveYear(form.fechaInicio)
        val codigo = if (form.codigo.isNotBlank()) {
            form.codigo
        } else {
            generateCodigo(form.nombre, anio, timestamp)
        }

        val campeonato = Campeonato(
            CODIGO = codigo,
            CAMPEONATO = form.nombre,
            FECHAALTA = form.fechaAlta.ifBlank { currentDate() },
            FECHAINICIO = form.fechaInicio,
            FECHAFINAL = form.fechaFin,
            PROVINCIA = form.provincia,
            ANIO = anio,
            HASTAGEXTRAS = form.hashtags,
            TIMESTAMP_CREACION = originalCampeonato?.TIMESTAMP_CREACION ?: timestamp,
            TIMESTAMP_MODIFICACION = timestamp,
            ORIGEN = Constants.ORIGEN_MOBILE
        )

        val validationError = Validations.validarCampeonato(campeonato)
        if (validationError != null) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    showValidationErrors = true,
                    message = FormMessage(validationError, isError = true)
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveCampeonato(campeonato)
                originalCampeonato = campeonato
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

    fun deleteCampeonato() {
        val codigo = _uiState.value.formData.codigo
        if (codigo.isBlank() || _uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deleteCampeonato(codigo)
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

    private fun deriveYear(fecha: String): Int {
        return runCatching {
            if (fecha.length >= 4) fecha.substring(0, 4).toInt() else LocalDate.now().year
        }.getOrDefault(LocalDate.now().year)
    }

    private fun currentDate(): String = LocalDate.now().format(DATE_FORMATTER)

    private fun generateCodigo(nombre: String, anio: Int, timestamp: Long): String {
        val base = nombre.uppercase(Locale.getDefault())
            .replace("[^A-Z0-9]".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        val safeBase = if (base.isBlank()) "CAMPEONATO" else base
        val anioSegment = if (anio > 0) "_${anio}" else ""
        return "${safeBase}${anioSegment}_${timestamp}"
    }
}
