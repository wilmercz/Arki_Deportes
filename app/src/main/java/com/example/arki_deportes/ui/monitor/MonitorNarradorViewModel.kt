package com.example.arki_deportes.ui.monitor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.*
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MonitorUiState(
    val partido: Partido? = null,
    val tiempoActual: String = "00:00",
    val goles: List<GolEvento> = emptyList(),
    val cambiosE1: List<CambioEvento> = emptyList(),
    val cambiosE2: List<CambioEvento> = emptyList(),
    val jugadoresE1: List<Jugador> = emptyList(),
    val jugadoresE2: List<Jugador> = emptyList(),
    val ultimoEvento: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class MonitorNarradorViewModel(
    private val repository: FirebaseCatalogRepository,
    private val campeonatoId: String,
    private val partidoId: String
) : ViewModel() {

    private val TAG = "MonitorNarradorViewModel"
    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null

    init {
        iniciarObservacion()
        iniciarActualizadorDeTiempo()
    }

    private fun iniciarObservacion() {
        viewModelScope.launch {
            repository.observePartido(campeonatoId, partidoId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { p -> 
                    _uiState.update { it.copy(partido = p, isLoading = false) }
                    cargarPlantillas(p.CODIGOEQUIPO1, p.CODIGOEQUIPO2)
                }
        }

        viewModelScope.launch {
            repository.observeGoles(campeonatoId, partidoId).collect { lista ->
                if (lista.size > _uiState.value.goles.size) {
                    val nuevo = lista.last()
                    dispararNotificacion("⚽ ¡GOL! - ${nuevo.JUGADOR} (${nuevo.MINUTO}')")
                }
                _uiState.update { it.copy(goles = lista) }
            }
        }

        viewModelScope.launch {
            repository.observeCambios(campeonatoId, partidoId, 1).collect { lista ->
                if (lista.size > _uiState.value.cambiosE1.size) {
                    val c = lista.last()
                    dispararNotificacion("🔄 CAMBIO: Entra ${c.ENTRA_NOMBRE}, sale ${c.SALE_NOMBRE}")
                }
                _uiState.update { it.copy(cambiosE1 = lista) }
            }
        }

        viewModelScope.launch {
            repository.observeCambios(campeonatoId, partidoId, 2).collect { lista ->
                if (lista.size > _uiState.value.cambiosE2.size) {
                    val c = lista.last()
                    dispararNotificacion("🔄 CAMBIO: Entra ${c.ENTRA_NOMBRE}, sale ${c.SALE_NOMBRE}")
                }
                _uiState.update { it.copy(cambiosE2 = lista) }
            }
        }
    }

    /**
     * Timer que recalcula el tiempo cada segundo basado en FECHA_PLAY
     */
    private fun iniciarActualizadorDeTiempo() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val partido = _uiState.value.partido
                if (partido != null && partido.estaEnCurso()) {
                    val segundos = partido.calcularTiempoActualSegundos()
                    val tiempoFormateado = partido.formatearTiempo(segundos)
                    _uiState.update { it.copy(tiempoActual = tiempoFormateado) }
                }
            }
        }
    }

    private fun cargarPlantillas(equipo1Id: String, equipo2Id: String) {
        if (equipo1Id.isBlank() || equipo2Id.isBlank()) return
        viewModelScope.launch {
            repository.observeJugadores(campeonatoId, equipo1Id).collect { lista ->
                _uiState.update { it.copy(jugadoresE1 = lista) }
            }
        }
        viewModelScope.launch {
            repository.observeJugadores(campeonatoId, equipo2Id).collect { lista ->
                _uiState.update { it.copy(jugadoresE2 = lista) }
            }
        }
    }

    private fun dispararNotificacion(mensaje: String) {
        _uiState.update { it.copy(ultimoEvento = mensaje) }
    }

    fun limpiarNotificacion() {
        _uiState.update { it.copy(ultimoEvento = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class MonitorNarradorViewModelFactory(
    private val repository: FirebaseCatalogRepository,
    private val campeonatoId: String,
    private val partidoId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MonitorNarradorViewModel(repository, campeonatoId, partidoId) as T
    }
}
