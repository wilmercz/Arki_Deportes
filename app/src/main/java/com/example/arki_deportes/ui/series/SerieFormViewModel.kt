package com.example.arki_deportes.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Serie
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SerieFormData(
    val codigoSerie: String = "",
    val codigoCampeonato: String = "",
    val nombreSerie: String = "",
    val descripcion: String = "",
    val cantGrupos: String = "0",
    val reglaClasificacion: String = "1ROS_Y_2DOS",
    val equiposClasifican: String = "0",
    val gruposRaw: String = "" // Ej: "A, B, C"
)

data class SerieFormUiState(
    val formData: SerieFormData = SerieFormData(),
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isLoading: Boolean = false,
    val message: FormMessage? = null,
    val shouldClose: Boolean = false,
    val showValidationErrors: Boolean = false
)

class SerieFormViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SerieFormUiState())
    val uiState: StateFlow<SerieFormUiState> = _uiState

    private var originalSerie: Serie? = null

    fun onNombreChange(value: String) = updateForm { copy(nombreSerie = value) }
    fun onDescripcionChange(value: String) = updateForm { copy(descripcion = value) }
    fun onCantGruposChange(value: String) = updateForm { copy(cantGrupos = value) }
    fun onReglaChange(value: String) = updateForm { copy(reglaClasificacion = value) }
    fun onEquiposClasificanChange(value: String) = updateForm { copy(equiposClasifican = value) }
    fun onGruposRawChange(value: String) = updateForm { copy(gruposRaw = value) }

    fun init(campeonatoId: String) {
        updateForm { copy(codigoCampeonato = campeonatoId) }
    }

    private fun updateForm(transform: SerieFormData.() -> SerieFormData) {
        _uiState.update { it.copy(formData = it.formData.transform()) }
    }

    fun loadSerie(campeonatoId: String, serieId: String?) {
        if (serieId == null) {
            _uiState.update { 
                it.copy(
                    isEditMode = false, 
                    formData = SerieFormData(codigoCampeonato = campeonatoId)
                ) 
            }
            originalSerie = null
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val serie = repository.getSerie(campeonatoId, serieId)
                if (serie != null) {
                    originalSerie = serie
                    _uiState.update {
                        it.copy(
                            isEditMode = true,
                            isLoading = false,
                            formData = SerieFormData(
                                codigoSerie = serie.CODIGOSERIE,
                                codigoCampeonato = serie.CODIGOCAMPEONATO,
                                nombreSerie = serie.NOMBRESERIE,
                                descripcion = serie.DESCRIPCION,
                                cantGrupos = serie.CANTGRUPOS.toString(),
                                reglaClasificacion = serie.REGLA_CLASIFICACION,
                                equiposClasifican = serie.EQUIPOS_CLASIFICAN.toString(),
                                gruposRaw = serie.GRUPOS
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, message = FormMessage("No se encontró la serie", true)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = FormMessage(e.message ?: "Error al cargar", true)) }
            }
        }
    }

    fun saveSerie() {
        val currentState = _uiState.value
        val form = currentState.formData
        
        // Validaciones básicas
        if (form.nombreSerie.isBlank() || form.gruposRaw.isBlank() || form.codigoCampeonato.isBlank()) {
            _uiState.update { it.copy(showValidationErrors = true, message = FormMessage("Complete los campos obligatorios", true)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val timestamp = System.currentTimeMillis()
                val anio = originalSerie?.ANIO ?: LocalDate.now().year
                val fechaAlta = originalSerie?.FECHAALTA ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                
                // Si es edición usamos el código existente, si no generamos uno
                val codigoSerie = if (currentState.isEditMode) form.codigoSerie else {
                    val safeNombre = form.nombreSerie.uppercase().replace(" ", "_").replace(Regex("[^A-Z0-9_]"), "")
                    "SERIE_${safeNombre}_$timestamp"
                }

                val serie = Serie(
                    CODIGOSERIE = codigoSerie,
                    CODIGOCAMPEONATO = form.codigoCampeonato,
                    NOMBRESERIE = form.nombreSerie.uppercase(),
                    DESCRIPCION = form.descripcion,
                    CANTGRUPOS = form.cantGrupos.toIntOrNull() ?: 0,
                    REGLA_CLASIFICACION = form.reglaClasificacion,
                    EQUIPOS_CLASIFICAN = form.equiposClasifican.toIntOrNull() ?: 0,
                    FECHAALTA = fechaAlta,
                    ANIO = anio,
                    TIMESTAMP_CREACION = originalSerie?.TIMESTAMP_CREACION ?: timestamp,
                    TIMESTAMP_MODIFICACION = timestamp,
                    GRUPOS = form.gruposRaw,
                    ORIGEN = "MOBILE"
                )

                repository.saveSerieConGrupos(serie, form.gruposRaw)
                
                _uiState.update { it.copy(isSaving = false, shouldClose = true, message = FormMessage("Serie guardada exitosamente")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, message = FormMessage(e.message ?: "Error al guardar", true)) }
            }
        }
    }

    fun deleteSerie() {
        val currentState = _uiState.value
        val form = currentState.formData
        if (form.codigoSerie.isBlank() || form.codigoCampeonato.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deleteSerie(form.codigoCampeonato, form.codigoSerie)
                _uiState.update { 
                    it.copy(
                        isDeleting = false, 
                        shouldClose = true, 
                        message = FormMessage("Serie eliminada exitosamente")
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isDeleting = false, 
                        message = FormMessage(e.message ?: "Error al eliminar", true)
                    ) 
                }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun onCloseConsumed() {
        _uiState.update { it.copy(shouldClose = false) }
    }
}
