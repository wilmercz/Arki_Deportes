package com.example.arki_deportes.ui.equipos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EquipoListUiState(
    val equipos: List<Equipo> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val campeonatoActivo: String? = null,
    val campeonatoNombre: String = "Todos los campeonatos"
)

class EquipoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipoListUiState())
    val uiState: StateFlow<EquipoListUiState> = _uiState

    private var equiposJob: Job? = null

    init {
        observeCampeonatoContext()
    }

    /**
     * Observa los cambios en el campeonato activo y recarga los equipos automáticamente
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
                loadEquipos(campeonato?.CODIGO)
            }
        }
    }

    /**
     * Carga los equipos filtrados por el campeonato especificado
     * @param campeonatoCodigo Código del campeonato, o null para ver todos
     */
    private fun loadEquipos(campeonatoCodigo: String?) {
        equiposJob?.cancel()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                equiposJob = launch {
                    repository.observeEquipos(campeonatoCodigo, null).collect { equipos ->
                        _uiState.update {
                            it.copy(
                                equipos = equipos.sortedBy { equipo -> equipo.getNombreDisplay() },
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

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                // El observeEquipos ya está activo, solo actualizamos el estado
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

    fun deleteEquipo(codigo: String) {
        viewModelScope.launch {
            try {
                repository.deleteEquipo(codigo)
                // El observeEquipos actualizará automáticamente la lista
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

    fun getFilteredEquipos(): List<Equipo> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.equipos

        return _uiState.value.equipos.filter { equipo ->
            equipo.EQUIPO_NOMBRECOMPLETO.lowercase().contains(query) ||
                    equipo.EQUIPO.lowercase().contains(query) ||
                    equipo.CODIGOCAMPEONATO.lowercase().contains(query)
        }
    }
}