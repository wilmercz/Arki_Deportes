package com.example.arki_deportes.ui.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.Repository
import com.example.arki_deportes.data.model.PartidoActual
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Representa el estado de la pantalla de tiempo real. */
data class TiempoRealUiState(
    val isLoading: Boolean = true,
    val partido: PartidoActual? = null,
    val isLive: Boolean = false,
    val error: String? = null
)

/** ViewModel encargado de observar el nodo PartidoActual en Firebase. */
class TiempoRealViewModel(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(TiempoRealUiState())
    val uiState: StateFlow<TiempoRealUiState> = _uiState.asStateFlow()

    private var liveJob: Job? = null

    init {
        observarPartidoActual()
    }

    /** Vuelve a suscribirse al nodo de Firebase para reintentar la lectura. */
    fun reintentar() {
        observarPartidoActual()
    }

    private fun observarPartidoActual() {
        liveJob?.cancel()
        liveJob = viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.observePartidoActual()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                        )
                    }
                }
                .collect { partido ->
                    _uiState.update {
                        it.copy(
                            partido = if (partido.hayPartido()) partido else null,
                            isLive = partido.hayPartido() && partido.estaEnTransmision(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}

/** Factory para proveer dependencias al [TiempoRealViewModel]. */
class TiempoRealViewModelFactory(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TiempoRealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TiempoRealViewModel(repository, dispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
