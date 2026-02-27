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
    val listError: String? = null
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

    /**
     * Asigna el partido al perfil del usuario y marca el partido con el nombre del operador
     */
    fun asignarPartido(campeonatoCodigo: String, partidoCodigo: String) {
        val usuarioActual = UsuarioContext.getUsuario() ?: return
        val idUsuario = usuarioActual.usuario // El ID de login
        val nombreDisplay = usuarioActual.nombre // "Carlos V"
        
        viewModelScope.launch(dispatcher) {
            try {
                // 1. Actualizar permisos del usuario en la ruta global
                val rutaPermisos = database.reference
                    .child("AppConfig")
                    .child("Usuarios")
                    .child(idUsuario)
                    .child("permisos")

                val permisosUpdate = mapOf(
                    "codigoCampeonato" to campeonatoCodigo,
                    "codigoPartido" to partidoCodigo
                )

                // 2. Actualizar el campo OPERADOR en el nodo del partido
                val nodoRaiz = repository.campeonatosReference().parent?.parent?.key ?: Constants.FIREBASE_NODO_RAIZ_DEFAULT
                // Nota: Usamos la referencia del repositorio para ser consistentes con el nodoRaiz
                val rutaPartido = repository.campeonatosReference()
                    .child(campeonatoCodigo)
                    .child("Partidos")
                    .child(partidoCodigo)

                val partidoUpdate = mapOf(
                    "OPERADOR" to nombreDisplay,
                    "TIMESTAMP_ASIGNACION" to System.currentTimeMillis()
                )

                // Realizar ambas actualizaciones
                rutaPermisos.updateChildren(permisosUpdate)
                rutaPartido.updateChildren(partidoUpdate)

                _uiState.update { it.copy(mensajeAsistente = "¡Partido asignado con éxito a $nombreDisplay!") }
            } catch (e: Exception) {
                Log.e(TAG, "Error en asignación: ${e.message}")
                _uiState.update { it.copy(mensajeAsistente = "Error al procesar la asignación") }
            }
        }
    }

    fun limpiarAsignacionManual() {
        val usuarioActual = UsuarioContext.getUsuario() ?: return
        val idUsuario = usuarioActual.usuario
        
        viewModelScope.launch(dispatcher) {
            try {
                // Obtenemos los datos actuales de asignación para limpiar también el nodo del partido
                val campeonatoId = usuarioActual.permisos.codigoCampeonato
                val partidoId = usuarioActual.permisos.codigoPartido

                // 1. Limpiar permisos del usuario
                database.reference
                    .child("AppConfig")
                    .child("Usuarios")
                    .child(idUsuario)
                    .child("permisos")
                    .updateChildren(mapOf("codigoCampeonato" to "NINGUNO", "codigoPartido" to "NINGUNO"))
                
                // 2. Limpiar OPERADOR en el partido si existía
                if (!campeonatoId.isNullOrBlank() && campeonatoId != "NINGUNO" && 
                    !partidoId.isNullOrBlank() && partidoId != "NINGUNO") {
                    
                    repository.campeonatosReference()
                        .child(campeonatoId)
                        .child("Partidos")
                        .child(partidoId)
                        .child("OPERADOR")
                        .setValue("NINGUNO")
                }

                UsuarioContext.limpiarPartidoAsignado()
                _uiState.update { it.copy(mensajeAsistente = "Asignación liberada") }
            } catch (e: Exception) {
                Log.e(TAG, "Error al limpiar: ${e.message}")
            }
        }
    }

    fun refrescarPartidos() {
        // Implementar si es necesario
    }

    private fun observarPartidoActual() {
        // Implementar si es necesario
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
