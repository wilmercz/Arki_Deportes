package com.example.arki_deportes.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Serie
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import com.example.arki_deportes.utils.Constants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

/**
 * Estado del formulario de series.
 */
data class SerieFormData(
    val codigo: String = "",
    val codigoCampeonato: String = "",
    val nombreSerie: String = "",
    val descripcion: String = "",
    val cantGrupos: String = "2",
    val reglaClasificacion: String = "1ROS_Y_2DOS",
    val equiposClasifican: String = "4",
    val fechaAlta: String = LocalDate.now().format(DATE_FORMATTER)
)

data class SerieFormUiState(
    val formData: SerieFormData = SerieFormData(),
    val campeonatos: List<Campeonato> = emptyList(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showValidationErrors: Boolean = false,
    val message: FormMessage? = null,
    val shouldClose: Boolean = false
)

class SerieFormViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SerieFormUiState())
    val uiState: StateFlow<SerieFormUiState> = _uiState

    private var originalSerie: Serie? = null

    init {
        loadCampeonatos()
    }

    private fun loadCampeonatos() {
        viewModelScope.launch {
            try {
                val campeonatos = repository.getAllCampeonatos()
                    .sortedByDescending { it.FECHAINICIO }
                _uiState.update { it.copy(campeonatos = campeonatos) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = FormMessage(
                        "Error al cargar campeonatos: ${e.message}",
                        isError = true
                    ))
                }
            }
        }
    }

    fun loadSerie(codigo: String?) {
        if (codigo.isNullOrBlank()) {
            _uiState.update {
                SerieFormUiState(
                    formData = SerieFormData(),
                    campeonatos = it.campeonatos
                )
            }
            originalSerie = null
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            try {
                val serie = repository.getSerie(codigo)
                if (serie != null) {
                    originalSerie = serie
                    _uiState.update {
                        it.copy(
                            formData = SerieFormData(
                                codigo = serie.CODIGOSERIE,
                                codigoCampeonato = serie.CODIGOCAMPEONATO,
                                nombreSerie = serie.NOMBRESERIE,
                                descripcion = serie.DESCRIPCION,
                                cantGrupos = serie.CANTGRUPOS.toString(),
                                reglaClasificacion = serie.REGLA_CLASIFICACION,
                                equiposClasifican = serie.EQUIPOS_CLASIFICAN.toString(),
                                fechaAlta = serie.FECHAALTA.ifBlank { currentDate() }
                            ),
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            message = FormMessage("No se encontró la serie solicitada", isError = true),
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

    fun onCampeonatoSelected(codigoCampeonato: String) {
        updateForm { copy(codigoCampeonato = codigoCampeonato) }
    }

    fun onNombreSerieChange(value: String) = updateForm { copy(nombreSerie = value) }

    fun onDescripcionChange(value: String) = updateForm { copy(descripcion = value) }

    fun onCantGruposChange(value: String) {
        // Solo permitir números
        if (value.isEmpty() || value.all { it.isDigit() }) {
            updateForm { 
                val newCantGrupos = copy(cantGrupos = value)
                // Recalcular equipos que clasifican
                val grupos = value.toIntOrNull() ?: 0
                val regla = reglaClasificacion
                val equiposClasifican = calcularEquiposClasifican(grupos, regla)
                newCantGrupos.copy(equiposClasifican = equiposClasifican.toString())
            }
        }
    }

    fun onReglaClasificacionChange(value: String) {
        updateForm { 
            val newRegla = copy(reglaClasificacion = value)
            // Recalcular equipos que clasifican
            val grupos = cantGrupos.toIntOrNull() ?: 0
            val equiposClasifican = calcularEquiposClasifican(grupos, value)
            newRegla.copy(equiposClasifican = equiposClasifican.toString())
        }
    }

    private fun calcularEquiposClasifican(cantGrupos: Int, regla: String): Int {
        return when (regla) {
            "1ROS_Y_2DOS" -> cantGrupos * 2
            "1ROS_Y_MEJOR_2DO" -> cantGrupos + 1
            "SOLO_1ROS" -> cantGrupos
            else -> 0
        }
    }

    private fun updateForm(transform: SerieFormData.() -> SerieFormData) {
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

    fun saveSerie() {
        val currentState = _uiState.value
        if (currentState.isSaving) return

        val form = currentState.formData
        
        // Validaciones
        if (form.codigoCampeonato.isBlank()) {
            _uiState.update {
                it.copy(
                    showValidationErrors = true,
                    message = FormMessage("Debe seleccionar un campeonato", isError = true)
                )
            }
            return
        }

        if (form.nombreSerie.isBlank()) {
            _uiState.update {
                it.copy(
                    showValidationErrors = true,
                    message = FormMessage("El nombre de la serie es obligatorio", isError = true)
                )
            }
            return
        }

        val cantGrupos = form.cantGrupos.toIntOrNull()
        if (cantGrupos == null || cantGrupos <= 0) {
            _uiState.update {
                it.copy(
                    showValidationErrors = true,
                    message = FormMessage("La cantidad de grupos debe ser mayor a 0", isError = true)
                )
            }
            return
        }

        val timestamp = System.currentTimeMillis()
        val anio = deriveYearFromCampeonato()
        val codigo = if (form.codigo.isNotBlank()) {
            form.codigo
        } else {
            generateCodigo(form.codigoCampeonato, form.nombreSerie, timestamp)
        }

        val serie = Serie(
            CODIGOSERIE = codigo,
            CODIGOCAMPEONATO = form.codigoCampeonato,
            NOMBRESERIE = form.nombreSerie,
            DESCRIPCION = form.descripcion,
            CANTGRUPOS = cantGrupos,
            REGLA_CLASIFICACION = form.reglaClasificacion,
            EQUIPOS_CLASIFICAN = form.equiposClasifican.toIntOrNull() ?: 0,
            FECHAALTA = form.fechaAlta.ifBlank { currentDate() },
            ANIO = anio,
            TIMESTAMP_CREACION = originalSerie?.TIMESTAMP_CREACION ?: timestamp,
            TIMESTAMP_MODIFICACION = timestamp,
            ORIGEN = Constants.ORIGEN_MOBILE
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveSerie(serie)
                originalSerie = serie
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

    fun deleteSerie() {
        val codigo = _uiState.value.formData.codigo
        if (codigo.isBlank() || _uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deleteSerie(codigo)
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

    private fun deriveYearFromCampeonato(): Int {
        val campeonato = _uiState.value.campeonatos.find { 
            it.CODIGO == _uiState.value.formData.codigoCampeonato 
        }
        return campeonato?.ANIO ?: LocalDate.now().year
    }

    private fun currentDate(): String = LocalDate.now().format(DATE_FORMATTER)

    private fun generateCodigo(codigoCampeonato: String, nombreSerie: String, timestamp: Long): String {
        val serieSegment = nombreSerie.uppercase(Locale.getDefault())
            .replace("[^A-Z0-9]".toRegex(), "")
            .take(3)
        return "${codigoCampeonato}_SERIE_${serieSegment}_${timestamp}"
    }
}
