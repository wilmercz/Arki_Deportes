package com.example.arki_deportes.ui.campeonatos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CampeonatoListUiState(
    val campeonatos: List<Campeonato> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class CampeonatoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private var observeJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(CampeonatoListUiState())
    val uiState: StateFlow<CampeonatoListUiState> = _uiState

    init {
        //loadCampeonatos()
        startObserving()
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observeCampeonatos()
                .collect { list ->
                    // ordénalo como prefieras (ej. por FECHAINICIO desc)
                    val ordenados = list.sortedByDescending { it.FECHAINICIO }
                    _uiState.update {
                        it.copy(
                            campeonatos = ordenados,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    fun loadCampeonatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val campeonatos = repository.getAllCampeonatos()
                    .sortedByDescending { it.FECHAINICIO } // Más recientes primero
                _uiState.update {
                    it.copy(
                        campeonatos = campeonatos,
                        isLoading = false
                    )
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
        // Reinicia la observación para mostrar spinner y “refrescar”
        _uiState.update { it.copy(isRefreshing = true) }
        startObserving()
    }

    fun refresh_Antiguo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val campeonatos = repository.getAllCampeonatos()
                    .sortedByDescending { it.FECHAINICIO }
                _uiState.update {
                    it.copy(
                        campeonatos = campeonatos,
                        isRefreshing = false
                    )
                }
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

    fun deleteCampeonato(codigo: String) {
        viewModelScope.launch {
            try {
                repository.deleteCampeonato(codigo)
                loadCampeonatos() // Recargar lista
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

    fun getFilteredCampeonatos(): List<Campeonato> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.campeonatos

        return _uiState.value.campeonatos.filter { campeonato ->
            campeonato.CAMPEONATO.lowercase().contains(query) ||
                    campeonato.PROVINCIA.lowercase().contains(query) ||
                    campeonato.ANIO.toString().contains(query)
        }
    }
}