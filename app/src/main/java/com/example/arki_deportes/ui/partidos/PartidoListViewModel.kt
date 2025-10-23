
package com.example.arki_deportes.ui.partidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PartidoListUiState(
    val partidos: List<Partido> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class PartidoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartidoListUiState())
    val uiState: StateFlow<PartidoListUiState> = _uiState

    init {
        loadPartidos()
    }

    private fun loadPartidos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.observePartidos().collect { partidos ->
                    _uiState.update {
                        it.copy(
                            partidos = partidos.sortedByDescending { partido -> partido.FECHA_PARTIDO },
                            isLoading = false
                        )
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
