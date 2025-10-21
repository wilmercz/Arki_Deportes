package com.example.arki_deportes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.Repository
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.PartidoActual
import com.example.arki_deportes.utils.Constants
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
    val isRefreshing: Boolean = true,
    val liveMatch: PartidoActual? = null,
    val isLive: Boolean = false,
    val partidos: List<Partido> = emptyList(),
    val liveError: String? = null,
    val listError: String? = null
)

/**
 * ViewModel encargado de orquestar la información mostrada en la pantalla Home.
 */
class HomeViewModel(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observarPartidoActual()
        refrescarPartidos()
    }

    /**
     * Refresca manualmente la lista de partidos dentro del rango configurado.
     */
    fun refrescarPartidos(fechaReferencia: LocalDate = LocalDate.now()) {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isRefreshing = true, listError = null) }
            try {
                val partidos = repository.obtenerPartidosRango(fechaReferencia)
                _uiState.update { state ->
                    state.copy(
                        partidos = partidos,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isRefreshing = false,
                        listError = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }

    private fun observarPartidoActual() {
        viewModelScope.launch(dispatcher) {
            repository.observePartidoActual()
                .catch { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingLive = false,
                            liveError = throwable.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                        )
                    }
                }
                .collect { partido ->
                    val partidoNormalizado = partido.normalizado()
                    val hayPartido = partidoNormalizado.hayPartido()
                    _uiState.update { state ->
                        state.copy(
                            liveMatch = if (hayPartido) partidoNormalizado else null,
                            isLive = hayPartido && partidoNormalizado.estaEnTransmision(),
                            isLoadingLive = false,
                            liveError = null
                        )
                    }
                }
        }
    }
}

/**
 * Factory manual para proveer dependencias al HomeViewModel.
 */
class HomeViewModelFactory(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, dispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
