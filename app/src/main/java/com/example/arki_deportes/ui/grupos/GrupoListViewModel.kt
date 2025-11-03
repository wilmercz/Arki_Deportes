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
 *
 * Maneja:
 * - Visualización de equipos por grupo
 * - Agregar equipos al grupo seleccionado
 * - Eliminar equipos del grupo
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

    /**
     * Observa los cambios en el campeonato activo
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
                loadGruposPorLetra(campeonato?.CODIGO, _uiState.value.grupoSeleccionado)
                loadEquiposDisponibles(campeonato?.CODIGO)
            }
        }
    }

    /**
     * Carga los equipos del grupo seleccionado
     */
    private fun loadGruposPorLetra(campeonatoCodigo: String?, letraGrupo: String) {
        gruposJob?.cancel()

        viewModelScope.launch {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔍 Cargando grupos del Grupo $letraGrupo")
            Log.d(TAG, "   Campeonato: $campeonatoCodigo")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                gruposJob = launch {
                    repository.observeGrupos(campeonatoCodigo).collect { todosGrupos ->
                        Log.d(TAG, "📦 Firebase devolvió ${todosGrupos.size} registros totales")

                        // Log de TODOS los registros recibidos
                        todosGrupos.forEachIndexed { index, grupo ->
                            Log.d(TAG, "")
                            Log.d(TAG, "📋 Registro #$index:")
                            Log.d(TAG, "   CODIGOGRUPO: '${grupo.CODIGOGRUPO}'")
                            Log.d(TAG, "   GRUPO: '${grupo.GRUPO}'")
                            Log.d(TAG, "   POSICION: ${grupo.POSICION}")
                            Log.d(TAG, "   PROVINCIA: '${grupo.PROVINCIA}'")
                            Log.d(TAG, "   CODIGOPROVINCIA: '${grupo.CODIGOPROVINCIA}'")
                            Log.d(TAG, "   NOMBREEQUIPO: '${grupo.NOMBREEQUIPO}'")
                            Log.d(TAG, "   CODIGOEQUIPO: '${grupo.CODIGOEQUIPO}'")
                            Log.d(TAG, "   → getNombreEquipo(): '${grupo.getNombreEquipo()}'")
                            Log.d(TAG, "   → getCodigoEquipo(): '${grupo.getCodigoEquipo()}'")
                        }

                        val gruposDelLetra = todosGrupos
                            .filter { it.GRUPO.equals(letraGrupo, ignoreCase = true) }
                            .sortedBy { it.POSICION }

                        Log.d(TAG, "")
                        Log.d(TAG, "✅ Filtrado: ${gruposDelLetra.size} equipos del Grupo $letraGrupo")
                        gruposDelLetra.forEach { grupo ->
                            Log.d(TAG, "   - Posición ${grupo.POSICION}: ${grupo.getNombreEquipo()}")
                        }

                        // ✅ Ordenar los grupos visibles por posición (1,2,3,4 y luego los 0)
                        val ordenados = gruposDelLetra.sortedWith(
                            compareBy<Grupo> { it.POSICION == 0 }      // los 0 van al final
                                .thenBy { it.POSICION }                // orden 1,2,3,4
                                .thenBy { it.getNombreEquipo() }       // desempate por nombre
                        )

                        _uiState.update {
                            it.copy(
                                grupos = gruposDelLetra,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR al cargar grupos: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }

    /**
     * Carga TODOS los equipos disponibles de la colección Equipos
     */
    private fun loadEquiposDisponibles(campeonatoCodigo: String?) {
        equiposJob?.cancel()

        viewModelScope.launch {
            try {
                equiposJob = launch {
                    repository.observeEquipos(campeonatoCodigo).collect { equipos ->
                        Log.d(TAG, "🏟️ Equipos disponibles cargados: ${equipos.size}")
                        _uiState.update {
                            it.copy(equiposDisponibles = equipos.sortedBy { it.EQUIPO })
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error al cargar equipos disponibles: ${e.message}")
            }
        }
    }

    /**
     * Cambia el grupo seleccionado
     */
    fun onGrupoSeleccionado(letra: String) {
        _uiState.update { it.copy(grupoSeleccionado = letra) }
        loadGruposPorLetra(_uiState.value.campeonatoActivo, letra)
    }

    /**
     * Muestra el diálogo para agregar equipo
     */
    fun showAddEquipoDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    /**
     * Oculta el diálogo
     */
    fun hideAddEquipoDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    /**
     * Agrega un equipo al grupo seleccionado
     * Crea un nuevo registro en la colección Grupos
     */
    fun addEquipoToGrupo(equipo: Equipo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingEquipo = true, errorMessage = null) }
            try {
                val campeonatoCodigo = _uiState.value.campeonatoActivo
                    ?: throw IllegalStateException("No hay campeonato activo")

                // Calcular la siguiente posición disponible
                val siguientePosicion = (_uiState.value.grupos.maxOfOrNull { it.POSICION } ?: 0) + 1

                // Crear registro en Grupos
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
                    it.copy(
                        isAddingEquipo = false,
                        errorMessage = e.message ?: "Error al agregar equipo"
                    )
                }
            }
        }
    }

    /**
     * Elimina un equipo del grupo
     * Elimina el registro de la colección Grupos
     */
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
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
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

    /**
     * Filtra equipos que NO están ya en el grupo
     */
    fun getEquiposNoEnGrupo(): List<Equipo> {
        val equiposEnGrupo = _uiState.value.grupos.map { it.CODIGOPROVINCIA }.toSet()
        return _uiState.value.equiposDisponibles.filter {
            it.CODIGOEQUIPO !in equiposEnGrupo
        }
    }
}