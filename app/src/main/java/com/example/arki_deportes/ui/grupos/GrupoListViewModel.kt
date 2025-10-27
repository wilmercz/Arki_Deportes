package com.example.arki_deportes.ui.grupos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.comparisons.compareBy

data class GrupoListUiState(
    val grupos: List<Grupo> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val campeonatoActivo: String? = null,
    val campeonatoNombre: String = "Todos los campeonatos"
)

class GrupoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrupoListUiState())
    val uiState: StateFlow<GrupoListUiState> = _uiState

    private var gruposJob: Job? = null

    init {
        observeCampeonatoContext()
    }

    /**
     * Observa los cambios en el campeonato activo y recarga los grupos automáticamente
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
                loadGrupos(campeonato?.CODIGO)
            }
        }
    }

    /**
     * Carga los grupos filtrados por el campeonato especificado
     * @param campeonatoCodigo Código del campeonato, o null para ver todos
     */
    private fun loadGrupos(campeonatoCodigo: String?) {
        gruposJob?.cancel()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                gruposJob = launch {
                    repository.observeGrupos(campeonatoCodigo).collect { grupos ->
                        _uiState.update {
                            it.copy(
                                grupos = grupos.sortedBy { grupo -> grupo.GRUPO },
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

    fun observeGrupos(campeonatoCodigo: String) {
        viewModelScope.launch {
            repository.observeGrupos(campeonatoCodigo)
                .collect { grupos ->
                    // ✅ ordenar por nombre del grupo y luego por posición
                    val ordenados = grupos.sortedWith(
                        compareBy<Grupo> { it.GRUPO.orEmpty() }
                            .thenBy { it.POSICION ?: Int.MAX_VALUE }
                    )

                    _uiState.update { it.copy(grupos = ordenados) }
                }
        }
    }

}