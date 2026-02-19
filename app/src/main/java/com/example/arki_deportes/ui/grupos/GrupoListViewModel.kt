package com.example.arki_deportes.ui.grupos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GRUPO LIST VIEW MODEL - EQUIPOS POR GRUPO CON AGREGAR/ELIMINAR
 * ═══════════════════════════════════════════════════════════════════════════
 */
private const val TAG = "GrupoListVM_DEBUG"

data class GrupoListUiState(
    val grupos: List<Grupo> = emptyList(),
    val equiposDisponibles: List<Equipo> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val campeonatoActivo: String? = null,
    val campeonatoNombre: String = "Todos los campeonatos",
    val grupoSeleccionado: String = "A",
    val gruposDisponibles: List<String> = listOf("A", "B", "C", "D"),
    val showAddDialog: Boolean = false,
    val isAddingEquipo: Boolean = false
)

class GrupoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrupoListUiState())
    val uiState: StateFlow<GrupoListUiState> = _uiState

    private var gruposJob: Job? = null
    private var equiposJob: Job? = null

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
                if (codigo != null) {
                    loadGruposPorLetra(codigo, _uiState.value.grupoSeleccionado)
                    loadEquiposDisponibles(codigo)
                }
            }
        }
    }

    private fun loadGruposPorLetra(campeonatoCodigo: String, letraGrupo: String) {
        gruposJob?.cancel()

        viewModelScope.launch {
            Log.d(TAG, "🔍 Cargando grupos del Grupo $letraGrupo - Campeonato: $campeonatoCodigo")

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.observeGrupos(campeonatoCodigo).collect { todosGrupos ->
                    val filtrados = todosGrupos
                        .filter { it.GRUPO.equals(letraGrupo, ignoreCase = true) }
                        .sortedWith(
                            compareBy<Grupo> { it.POSICION == 0 }
                                .thenBy { it.POSICION }
                                .thenBy { it.getNombreEquipo() }
                        )

                    _uiState.update {
                        it.copy(
                            grupos = filtrados,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR al cargar grupos: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO)
                }
            }
        }
    }

    private fun loadEquiposDisponibles(campeonatoCodigo: String) {
        equiposJob?.cancel()
        viewModelScope.launch {
            try {
                repository.observeEquipos(campeonatoCodigo).collect { equipos ->
                    _uiState.update {
                        it.copy(equiposDisponibles = equipos.sortedBy { it.EQUIPO })
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error al cargar equipos disponibles: ${e.message}")
            }
        }
    }

    fun onGrupoSeleccionado(letra: String) {
        _uiState.update { it.copy(grupoSeleccionado = letra) }
        val codigo = _uiState.value.campeonatoActivo
        if (codigo != null) {
            loadGruposPorLetra(codigo, letra)
        }
    }

    fun showAddEquipoDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddEquipoDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addEquipoToGrupo(equipo: Equipo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingEquipo = true, errorMessage = null) }
            try {
                val campeonatoCodigo = _uiState.value.campeonatoActivo
                    ?: throw IllegalStateException("No hay campeonato activo")

                val siguientePosicion = (_uiState.value.grupos.maxOfOrNull { it.POSICION } ?: 0) + 1

                val nuevoGrupo = Grupo(
                    CODIGOCAMPEONATO = campeonatoCodigo,
                    CODIGOGRUPO = "${System.currentTimeMillis()}_${equipo.EQUIPO}",
                    GRUPO = _uiState.value.grupoSeleccionado,
                    PROVINCIA = equipo.EQUIPO,
                    CODIGOPROVINCIA = equipo.CODIGOEQUIPO,
                    POSICION = siguientePosicion,
                    ANIO = java.time.Year.now().value,
                    ORIGEN = "MOBILE",
                    TIMESTAMP_CREACION = System.currentTimeMillis(),
                    TIMESTAMP_MODIFICACION = System.currentTimeMillis()
                )

                repository.saveGrupo(nuevoGrupo)

                _uiState.update {
                    it.copy(
                        isAddingEquipo = false,
                        showAddDialog = false,
                        successMessage = "Equipo agregado al grupo ${_uiState.value.grupoSeleccionado}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isAddingEquipo = false, errorMessage = e.message ?: "Error al agregar equipo")
                }
            }
        }
    }

    fun deleteEquipoFromGrupo(grupo: Grupo) {
        viewModelScope.launch {
            try {
                val campeonatoCodigo = _uiState.value.campeonatoActivo
                    ?: throw IllegalStateException("No hay campeonato activo")

                repository.deleteGrupo(campeonatoCodigo, grupo.CODIGOGRUPO)

                _uiState.update {
                    it.copy(successMessage = "Equipo eliminado del grupo")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Error al eliminar equipo")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun refresh() {
        val codigo = _uiState.value.campeonatoActivo
        if (codigo != null) {
            loadGruposPorLetra(codigo, _uiState.value.grupoSeleccionado)
            loadEquiposDisponibles(codigo)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredGrupos(): List<Grupo> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.grupos

        return _uiState.value.grupos.filter { grupo ->
            grupo.getNombreEquipo().lowercase().contains(query) ||
                    grupo.POSICION.toString().contains(query)
        }
    }

    fun getEquiposNoEnGrupo(): List<Equipo> {
        val equiposEnGrupo = _uiState.value.grupos.map { it.CODIGOPROVINCIA }.toSet()
        return _uiState.value.equiposDisponibles.filter {
            it.CODIGOEQUIPO !in equiposEnGrupo
        }
    }
}
