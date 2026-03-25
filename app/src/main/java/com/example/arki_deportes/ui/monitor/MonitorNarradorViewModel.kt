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

    private fun iniciarObservacion() {        // 1. OBSERVAR PARTIDO (MARCADOR Y ESTADO)
        viewModelScope.launch {
            repository.observePartido(campeonatoId, partidoId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { p ->
                    // Actualizamos el estado básico
                    _uiState.update { it.copy(
                        partido = p,
                        isLoading = false,
                        // Usamos el operador ?. y ?: para asignar valores seguros si p es null
                        marcadorE1 = p?.GOLES1 ?: it.marcadorE1,
                        marcadorE2 = p?.GOLES2 ?: it.marcadorE2
                    ) }

                    // Solo ejecutamos lógica adicional si el partido realmente existe (no es null)
                    if (p != null) {
                        actualizarTiempoSync(p)

                        if (p.CODIGOEQUIPO1.isNotBlank() && _uiState.value.jugadoresE1.isEmpty()) {
                            cargarPlantillas(p.CODIGOEQUIPO1, p.CODIGOEQUIPO2)
                        }
                    }
                }
        }

        // 2. OBSERVAR GOLES (Sincronización reforzada)
        viewModelScope.launch {
            repository.observeGoles(campeonatoId, partidoId).collect { lista ->
                // Aquí ya tenías ?. que es correcto
                val golesE1 = lista.count { it.CODIGOEQUIPO == _uiState.value.partido?.CODIGOEQUIPO1 }
                val golesE2 = lista.count { it.CODIGOEQUIPO == _uiState.value.partido?.CODIGOEQUIPO2 }

                if (lista.size > _uiState.value.goles.size && _uiState.value.goles.isNotEmpty()) {
                    val nuevo = lista.lastOrNull()
                    nuevo?.let { dispararNotificacion("⚽ ¡GOL! - ${it.JUGADOR} (${it.MINUTO}')") }
                }

                _uiState.update { it.copy(
                    goles = lista,
                    marcadorE1 = maxOf(it.marcadorE1, golesE1),
                    marcadorE2 = maxOf(it.marcadorE2, golesE2)
                ) }
            }
        }
        // ... el resto de la función se mantiene igual ...
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
        // Manejamos FECHA_PLAY como Any?
        val tieneFechaPlay = when (val fp = partido.FECHA_PLAY) {
            is String -> fp.isNotBlank()
            is Long -> fp > 0L
            else -> fp != null
        }

        if (tieneFechaPlay && yaEmpezo && !partido.estaEnDescanso() && !partido.estaFinalizado()) {
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
