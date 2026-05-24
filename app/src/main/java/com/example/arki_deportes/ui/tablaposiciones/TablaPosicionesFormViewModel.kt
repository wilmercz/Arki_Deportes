package com.example.arki_deportes.ui.tablaposiciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.TablaPosicionesItem
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado para la pantalla de edición de tabla de posiciones
 */
data class TablaPosicionesFormUiState(
    val items: List<TablaPosicionesItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class TablaPosicionesFormViewModel(
    private val repository: FirebaseCatalogRepository,
    private val campeonatoId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TablaPosicionesFormUiState())
    val uiState: StateFlow<TablaPosicionesFormUiState> = _uiState.asStateFlow()

    init { 
        cargarPosiciones() 
    }

    private fun cargarPosiciones() {
        viewModelScope.launch {
            try {
                repository.observeTablaPosiciones(campeonatoId).collect { lista ->
                    _uiState.update { it.copy(items = lista, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    // Lógica para importar equipos si la tabla está vacía
    fun importarEquipos() {
        viewModelScope.launch {
            try {
                // Se usa observeEquipos para obtener la lista actual de equipos del campeonato
                repository.observeEquipos(campeonatoId).first().let { equipos ->
                    val nuevosItems = equipos.map { equipo ->
                        TablaPosicionesItem(
                            EQUIPO_CODIGO = equipo.CODIGOEQUIPO,
                            EQUIPO_NOMBRE = equipo.EQUIPO,
                            CAMPEONATO_CODIGO = campeonatoId,
                            PJ = 0, PG = 0, PE = 0, PP = 0, GF = 0, GC = 0, DG = 0, PTS = 0
                        )
                    }
                    repository.saveTablaPosiciones(campeonatoId, nuevosItems)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun actualizarCampo(equipoCodigo: String, campo: String, valor: String) {
        val num = valor.toIntOrNull() ?: 0
        _uiState.update { state ->
            val listaModificada = state.items.map { item ->
                if (item.EQUIPO_CODIGO == equipoCodigo) {
                    val actual = when(campo) {
                        "PJ" -> item.copy(PJ = num)
                        "PG" -> item.copy(PG = num)
                        "PE" -> item.copy(PE = num)
                        "PP" -> item.copy(PP = num)
                        "GF" -> item.copy(GF = num)
                        "GC" -> item.copy(GC = num)
                        else -> item
                    }
                    // Auto-cálculo: PTS = (PG*3) + PE | DG = GF - GC
                    actual.copy(
                        PTS = (actual.PG * 3) + (actual.PE * 1),
                        DG = actual.GF - actual.GC
                    )
                } else item
            }
            state.copy(items = listaModificada)
        }
    }

    fun guardar() {
        viewModelScope.launch {
            try {
                repository.saveTablaPosiciones(campeonatoId, _uiState.value.items)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
