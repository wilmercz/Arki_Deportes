package com.example.arki_deportes.ui.partidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

data class PartidoListUiState(
    val partidos: List<Partido> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val campeonatoActivo: String? = null,
    val campeonatoNombre: String = "Todos los campeonatos"
)

class PartidoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartidoListUiState())
    val uiState: StateFlow<PartidoListUiState> = _uiState

    private var partidosJob: Job? = null

    init {

        observeCampeonatoContext()
    }

    /**
     * Observa los cambios en el campeonato activo y recarga los partidos automáticamente
     */
    private fun observeCampeonatoContext() {
        viewModelScope.launch {
            CampeonatoContext.campeonatoActivo.collect { campeonato ->
                _uiState.update {
                    it.copy(
                        campeonatoActivo = campeonato?.CODIGO,
                        campeonatoNombre = campeonato?.CAMPEONATO ?: "Todos los campeonatos"
                    )
                }
                /*DESACTIVADO TEMPORALMENTE
                loadPartidos(campeonato?.CODIGO)

                 */
            }
        }
    }

    /**
     * Carga los partidos filtrados por el campeonato especificado
     * @param campeonatoCodigo Código del campeonato, o null para ver todos
     */
    /* DESACTIVADO TEMPORALMENTE
    private fun loadPartidos(campeonatoCodigo: String?) {
        partidosJob?.cancel()
        Log.d("VM_PARTIDOS", "loadPartidos(${campeonatoCodigo ?: "null"})")  // <—

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                partidosJob = launch {
                    repository.observePartidos(campeonatoCodigo).collect { partidos ->
                        Log.d("VM_PARTIDOS", "collect(): recibidos=${partidos.size}")
                        _uiState.update {
                            it.copy(
                                partidos = partidos.sortedByDescending { partido -> partido.FECHA_PARTIDO },
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }
*/


    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                // El observePartidos ya está activo, solo actualizamos el estado
                _uiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }

    fun deletePartido(codigo: String) {
        viewModelScope.launch {
            try {
                repository.deletePartido(codigo)
                // El observePartidos actualizará automáticamente la lista
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredPartidos(): List<Partido> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.partidos

        return _uiState.value.partidos.filter { partido ->
            partido.EQUIPO1.lowercase().contains(query) ||
                    partido.EQUIPO2.lowercase().contains(query) ||
                    partido.CAMPEONATOTXT.lowercase().contains(query) ||
                    partido.FECHA_PARTIDO.contains(query)
        }
    }
}