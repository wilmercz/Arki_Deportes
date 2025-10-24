package com.example.arki_deportes.ui.grupos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GrupoListUiState(
    val grupos: List<Grupo> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class GrupoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrupoListUiState())
    val uiState: StateFlow<GrupoListUiState> = _uiState

    init {
        loadGrupos()
    }

    fun loadGrupos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.observeGrupos().collect { grupos ->
                    _uiState.update {
                        it.copy(
                            grupos = grupos.sortedBy { grupo -> grupo.GRUPO },
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
                // La observación ya está activa, solo actualizamos el flag
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

    fun deleteGrupo(codigo: String) {
        viewModelScope.launch {
            try {
                repository.deleteGrupo(codigo)
                // La lista se actualiza automáticamente por el observador
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

    fun getFilteredGrupos(): List<Grupo> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.grupos

        return _uiState.value.grupos.filter { grupo ->
            grupo.GRUPO.lowercase().contains(query) ||
                    grupo.PROVINCIA.lowercase().contains(query) ||
                    grupo.ANIO.toString().contains(query)
        }
    }
}