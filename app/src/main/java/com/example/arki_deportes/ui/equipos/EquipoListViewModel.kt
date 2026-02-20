package com.example.arki_deportes.ui.equipos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.utils.EcuadorProvincias
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EquipoListUiState(
    val equipos: List<Equipo> = emptyList(),
    val gruposMap: Map<String, String> = emptyMap(), // Mapeo de CODIGOGRUPO -> Nombre del Grupo
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
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
    private var gruposJob: Job? = null

    init {
        observeCampeonatoContext()
    }

    private fun observeCampeonatoContext() {
        viewModelScope.launch {
            CampeonatoContext.campeonatoActivo.collect { campeonato ->
                _uiState.update {
                    it.copy(
                        campeonatoActivo = campeonato?.CODIGO,
                        campeonatoNombre = campeonato?.CAMPEONATO ?: "Todos los campeonatos"
                    )
                }
                val codigo = campeonato?.CODIGO
                loadEquipos(codigo)
                observeGrupos(codigo)
            }
        }
    }

    private fun loadEquipos(campeonatoCodigo: String?) {
        equiposJob?.cancel()
        if (campeonatoCodigo == null) {
            _uiState.update { it.copy(equipos = emptyList(), isLoading = false) }
            return
        }

        equiposJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.observeEquipos(campeonatoCodigo).collect { equipos ->
                    _uiState.update {
                        it.copy(
                            equipos = equipos.sortedBy { e -> e.getNombreDisplay() },
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

    private fun observeGrupos(campeonatoCodigo: String?) {
        gruposJob?.cancel()
        if (campeonatoCodigo == null) {
            _uiState.update { it.copy(gruposMap = emptyMap()) }
            return
        }

        gruposJob = viewModelScope.launch {
            try {
                repository.observeGrupos(campeonatoCodigo).collect { grupos ->
                    val map = grupos.associate { grupo ->
                        val codigo = grupo.CODIGOGRUPO?.toString() ?: ""
                        codigo to grupo.GRUPO
                    }
                    _uiState.update { it.copy(gruposMap = map) }
                }
            } catch (e: Exception) {
                Log.e("EquipoListVM", "Error al observar grupos: ${e.message}")
            }
        }
    }

    fun refresh() {
        val codigo = _uiState.value.campeonatoActivo
        loadEquipos(codigo)
        observeGrupos(codigo)
    }

    fun deleteEquipo(codigo: String) {
        val campeonatoId = _uiState.value.campeonatoActivo ?: return
        viewModelScope.launch {
            try {
                repository.deleteEquipo(campeonatoId, codigo)
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

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun getFilteredEquipos(): List<Equipo> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.equipos

        return _uiState.value.equipos.filter { equipo ->
            equipo.EQUIPO_NOMBRECOMPLETO.lowercase().contains(query) ||
            equipo.EQUIPO.lowercase().contains(query) ||
            equipo.PROVINCIA.lowercase().contains(query)
        }
    }
}
