package com.example.arki_deportes.ui.campeonatos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.utils.SportType
import com.example.arki_deportes.utils.Validations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.Normalizer


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
    val hashtags: String = "",
    val deporte: String = SportType.FUTBOL.id,
    val alias: String = "",
    val tiempoJuego: String = "45",
    val duracion: String = "0",
    val mangas: String = "0",
    val vueltas: String = "0",
    val circuito: String = "",
    val lugar: String = "",
    val estadios: String = "" 
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
                                hashtags = campeonato.HASTAGEXTRAS,
                                deporte = campeonato.DEPORTE,
                                alias = campeonato.ALIAS,
                                tiempoJuego = campeonato.getTiempoJuegoStr(),
                                duracion = campeonato.getDuracionStr(),
                                mangas = campeonato.getMangasStr(),
                                vueltas = campeonato.getVueltasStr(),
                                circuito = campeonato.CIRCUITO,
                                lugar = campeonato.LUGAR,
                                estadios = campeonato.CIRCUITO
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
    fun onHashtagsChange(value: String) = updateForm { copy(hashtags = value) }
    fun onDeporteChange(value: String) = updateForm { copy(deporte = value) }
    fun onAliasChange(value: String) = updateForm { copy(alias = value) }
    fun onTiempoJuegoChange(value: String) = updateForm { copy(tiempoJuego = value) }
    fun onDuracionChange(value: String) = updateForm { copy(duracion = value) }
    fun onMangasChange(value: String) = updateForm { copy(mangas = value) }
    fun onVueltasChange(value: String) = updateForm { copy(vueltas = value) }
    fun onCircuitoChange(value: String) = updateForm { copy(circuito = value) }
    fun onLugarChange(value: String) = updateForm { copy(lugar = value) }
    fun onEstadiosChange(value: String) = updateForm { copy(estadios = value) }

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
        val anio = deriveYear(form.fechaInicio)
        val codigo = if (form.codigo.isNotBlank()) {
            form.codigo
        } else {
            generateCodigo(form.nombre, anio, timestamp)
        }

        val finalCircuito = if (isDeporteMotor(form.deporte)) form.circuito else form.estadios

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
            ORIGEN = Constants.ORIGEN_MOBILE,
            DEPORTE = form.deporte,
            ALIAS = form.alias,
            TIEMPOJUEGO = if (form.tiempoJuego.isBlank()) "45" else form.tiempoJuego,
            DURACION = form.duracion,
            MANGAS = form.mangas,
            VUELTAS = form.vueltas,
            CIRCUITO = finalCircuito,
            LUGAR = form.lugar
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

                repository.saveEstadiosYLugares(
                    campeonatoCodigo = campeonato.CODIGO,
                    estadiosString = form.estadios,
                    lugaresString = form.lugar
                )

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



    private fun isDeporteMotor(deporte: String): Boolean {
        return deporte in listOf(SportType.AUTOMOVILISMO.id, SportType.MOTOCICLISMO.id, SportType.CICLISMO.id)
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
