package com.example.arki_deportes.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.context.UsuarioContext
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.PartidoActual
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.Job

/**
 * Representa el estado de la pantalla de inicio.
 */
data class HomeUiState(
    val isLoadingLive: Boolean = true,
    val isRefreshing: Boolean = false,
    val liveMatch: PartidoActual? = null,
    val isLive: Boolean = false,
    val partidos: List<Partido> = emptyList(),
    
    // Asistente de Asignación
    val campeonatos: List<Campeonato> = emptyList(),
    val partidosDelCampeonato: List<Partido> = emptyList(),
    val isLoadingAsistente: Boolean = false,
    val mensajeAsistente: String? = null,
    val liveError: String? = null,
    val listError: String? = null,
    val forzarBusqueda: Boolean = false
)

/**
 * ViewModel mejorado para manejar el asistente de asignación en tiempo real.
 */
class HomeViewModel(
    private val repository: FirebaseCatalogRepository,
    private val database: FirebaseDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val TAG = "HomeViewModel"
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 2. Variable para controlar la observación activa
    private var partidoActualJob: Job? = null

    init {
        observarPartidoActual()
        observarCampeonatos()
    }

    /**
     * Observa campeonatos en tiempo real para el selector
     */
    private fun observarCampeonatos() {
        viewModelScope.launch(dispatcher) {
            repository.observeCampeonatos()
                .catch { e -> Log.e(TAG, "Error observando campeonatos: ${e.message}") }
                .collect { lista ->
                    _uiState.update { it.copy(campeonatos = lista) }
                }
        }
    }

    /**
     * Observa los partidos de un campeonato seleccionado
     */
    fun cargarPartidosDeCampeonato(campeonatoCodigo: String) {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoadingAsistente = true) }
            repository.observePartidos(campeonatoCodigo)
                .catch { e -> 
                    _uiState.update { it.copy(isLoadingAsistente = false, mensajeAsistente = "Error al cargar partidos") }
                }
                .collect { lista ->
                    _uiState.update { it.copy(
                        partidosDelCampeonato = lista,
                        isLoadingAsistente = false
                    )}
                }
        }
    }


    fun toggleBusquedaManual(activar: Boolean) {
        _uiState.update { it.copy(forzarBusqueda = activar) }
    }

    /**
     * Asigna el partido al perfil del usuario y marca el partido con el nombre del operador
     */
    fun asignarPartido(campeonatoCodigo: String, partidoCodigo: String) {
        // ... (lógica existente)
        viewModelScope.launch(dispatcher) {
            try {
                // ... (lógica de guardado) ...

                _uiState.update {
                    it.copy(
                        isLoadingAsistente = false,
                        forzarBusqueda = false, // 👈 Apagar búsqueda manual al asignar
                        mensajeAsistente = "✅ ¡Partido autoasignado con éxito!"
                    )
                }
                observarPartidoActual()
            } catch (e: Exception) { /* ... */ }
        }
    }

    // --- Actualizar limpiarAsignacionManual ---
    fun limpiarAsignacionManual() {
        // ...
        viewModelScope.launch(dispatcher) {
            try {
                // ... (lógica de limpieza) ...
                _uiState.update { it.copy(forzarBusqueda = false) } // 👈 Resetear flag
                observarPartidoActual()
            } catch (e: Exception) { /* ... */ }
        }
    }


    fun refrescarPartidos() {
        // Implementar si es necesario
    }

    private fun observarPartidoActual() {
        // 🛡️ Cancelar observación previa si existe
        partidoActualJob?.cancel()

        val usuario = UsuarioContext.getUsuario() ?: return
        val campId = usuario.permisos.codigoCampeonato
        val partId = usuario.permisos.codigoPartido

        if (campId.isNullOrBlank() || partId.isNullOrBlank() || partId == "NINGUNO") {
            _uiState.update { it.copy(liveMatch = null, isLive = false, isLoadingLive = false) }
            return
        }

        partidoActualJob = viewModelScope.launch(dispatcher) {
            repository.observePartido(campId, partId)
                .catch { _uiState.update { it.copy(isLoadingLive = false) } }
                .collect { partido ->
                    val esHoy = partido?.FECHA_PARTIDO == LocalDate.now().toString()
                    val estaTerminado = partido?.ESTADO == 1

                    if (partido == null || estaTerminado || !esHoy) {
                        _uiState.update { it.copy(liveMatch = null, isLive = false, isLoadingLive = false) }
                    } else {
                        _uiState.update { it.copy(
                            liveMatch = PartidoActual.fromPartido(partido),
                            isLive = true,
                            isLoadingLive = false
                        ) }
                    }
                }
        }
    }



}

/**
 * Factory actualizada para inyectar FirebaseCatalogRepository.
 */
class HomeViewModelFactory(
    private val repository: FirebaseCatalogRepository,
    private val database: FirebaseDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, database, dispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
