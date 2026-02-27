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
    val marcadorE1: Int = 0, // 👈 Goles calculados Equipo 1
    val marcadorE2: Int = 0, // 👈 Goles calculados Equipo 2
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

    private val TAG = "MonitorNarradorVM"
    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null

    init {
        iniciarObservacion()
        iniciarActualizadorDeTiempo()
    }

    private fun iniciarObservacion() {
        // 1. OBSERVAR PARTIDO (MARCADOR Y ESTADO)
        viewModelScope.launch {
            repository.observePartido(campeonatoId, partidoId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { p -> 
                    _uiState.update { it.copy(
                        partido = p, 
                        isLoading = false,
                        // Sincronizamos marcador inicial desde el objeto partido
                        marcadorE1 = p.GOLES1,
                        marcadorE2 = p.GOLES2
                    ) }
                    actualizarTiempoSync(p)
                    
                    if (p.CODIGOEQUIPO1.isNotBlank() && _uiState.value.jugadoresE1.isEmpty()) {
                        cargarPlantillas(p.CODIGOEQUIPO1, p.CODIGOEQUIPO2)
                    }
                }
        }

        // 2. OBSERVAR GOLES (Sincronización reforzada)
        viewModelScope.launch {
            repository.observeGoles(campeonatoId, partidoId).collect { lista ->
                // Calculamos goles por equipo contando el historial si es más confiable
                val golesE1 = lista.count { it.CODIGOEQUIPO == _uiState.value.partido?.CODIGOEQUIPO1 }
                val golesE2 = lista.count { it.CODIGOEQUIPO == _uiState.value.partido?.CODIGOEQUIPO2 }

                if (lista.size > _uiState.value.goles.size && _uiState.value.goles.isNotEmpty()) {
                    val nuevo = lista.lastOrNull()
                    nuevo?.let { dispararNotificacion("⚽ ¡GOL! - ${it.JUGADOR} (${it.MINUTO}')") }
                }
                
                _uiState.update { it.copy(
                    goles = lista,
                    // Si el conteo de la lista es mayor, lo usamos como verdad absoluta
                    marcadorE1 = maxOf(it.marcadorE1, golesE1),
                    marcadorE2 = maxOf(it.marcadorE2, golesE2)
                ) }
            }
        }

        // 3. OBSERVAR CAMBIOS
        viewModelScope.launch {
            repository.observeCambios(campeonatoId, partidoId, 1).collect { lista ->
                if (lista.size > _uiState.value.cambiosE1.size && _uiState.value.cambiosE1.isNotEmpty()) {
                    val c = lista.lastOrNull()
                    c?.let { dispararNotificacion("🔄 CAMBIO: Entra ${it.ENTRA_NOMBRE}") }
                }
                _uiState.update { it.copy(cambiosE1 = lista) }
            }
        }

        viewModelScope.launch {
            repository.observeCambios(campeonatoId, partidoId, 2).collect { lista ->
                if (lista.size > _uiState.value.cambiosE2.size && _uiState.value.cambiosE2.isNotEmpty()) {
                    val c = lista.lastOrNull()
                    c?.let { dispararNotificacion("🔄 CAMBIO: Entra ${it.ENTRA_NOMBRE}") }
                }
                _uiState.update { it.copy(cambiosE2 = lista) }
            }
        }
    }

    private fun iniciarActualizadorDeTiempo() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                _uiState.value.partido?.let { actualizarTiempoSync(it) }
                delay(1000)
            }
        }
    }

    private fun actualizarTiempoSync(partido: Partido) {
        val yaEmpezo = partido.TIEMPOSJUGADOS > 0 || partido.estaEnCurso()
        if (partido.FECHA_PLAY.isNotBlank() && yaEmpezo && !partido.estaEnDescanso() && !partido.estaFinalizado()) {
            val segundos = partido.calcularTiempoActualSegundos()
            _uiState.update { it.copy(tiempoActual = partido.formatearTiempo(segundos)) }
        } else {
            _uiState.update { it.copy(tiempoActual = partido.TIEMPOJUEGO.ifBlank { "00:00" }) }
        }
    }

    private fun cargarPlantillas(e1: String, e2: String) {
        viewModelScope.launch { repository.observeJugadores(campeonatoId, e1).collect { l -> _uiState.update { it.copy(jugadoresE1 = l) } } }
        viewModelScope.launch { repository.observeJugadores(campeonatoId, e2).collect { l -> _uiState.update { it.copy(jugadoresE2 = l) } } }
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
