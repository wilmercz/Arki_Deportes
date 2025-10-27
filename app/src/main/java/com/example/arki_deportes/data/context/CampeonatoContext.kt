package com.example.arki_deportes.data.context

import android.content.Context
import android.content.SharedPreferences
import com.example.arki_deportes.data.model.Campeonato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CAMPEONATO CONTEXT - CONTEXTO GLOBAL DEL CAMPEONATO ACTIVO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Singleton que mantiene el estado del campeonato actualmente seleccionado
 * en toda la aplicación. Permite filtrar automáticamente Equipos, Grupos
 * y Partidos según el campeonato activo.
 *
 * Características:
 * - Persiste el campeonato seleccionado en SharedPreferences
 * - Emite cambios mediante Flow para actualización reactiva
 * - Permite seleccionar "Todos" (null) para ver sin filtro
 */
object CampeonatoContext {

    private const val PREFS_NAME = "campeonato_prefs"
    private const val KEY_CAMPEONATO_CODIGO = "campeonato_codigo"
    private const val KEY_CAMPEONATO_NOMBRE = "campeonato_nombre"
    private const val KEY_CAMPEONATO_ANIO = "campeonato_anio"

    private var prefs: SharedPreferences? = null

    private val _campeonatoActivo = MutableStateFlow<Campeonato?>(null)
    
    /**
     * Flow del campeonato actualmente seleccionado.
     * null = "Ver Todos" (sin filtro)
     */
    val campeonatoActivo: StateFlow<Campeonato?> = _campeonatoActivo.asStateFlow()

    /**
     * Inicializa el contexto con el Context de Android para acceder a SharedPreferences
     * Debe llamarse en Application o MainActivity onCreate
     */
    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            restaurarCampeonatoGuardado()
        }
    }

    /**
     * Selecciona un campeonato como activo
     * @param campeonato El campeonato a activar, o null para "Ver Todos"
     */
    fun seleccionarCampeonato(campeonato: Campeonato?) {
        _campeonatoActivo.value = campeonato
        guardarCampeonato(campeonato)
    }

    /**
     * Obtiene el código del campeonato activo, o null si no hay seleccionado
     */
    fun getCampeonatoActivoCodigo(): String? {
        return _campeonatoActivo.value?.CODIGO
    }

    /**
     * Obtiene el nombre del campeonato activo, o "Todos los campeonatos" si no hay seleccionado
     */
    fun getCampeonatoActivoNombre(): String {
        return _campeonatoActivo.value?.CAMPEONATO ?: "Todos los campeonatos"
    }

    /**
     * Verifica si hay un campeonato seleccionado
     */
    fun hayCampeonatoSeleccionado(): Boolean {
        return _campeonatoActivo.value != null
    }

    /**
     * Limpia la selección (equivale a "Ver Todos")
     */
    fun limpiarSeleccion() {
        seleccionarCampeonato(null)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PERSISTENCIA EN SHAREDPREFERENCES
    // ═══════════════════════════════════════════════════════════════════════

    private fun guardarCampeonato(campeonato: Campeonato?) {
        prefs?.edit()?.apply {
            if (campeonato != null) {
                putString(KEY_CAMPEONATO_CODIGO, campeonato.CODIGO)
                putString(KEY_CAMPEONATO_NOMBRE, campeonato.CAMPEONATO)
                putInt(KEY_CAMPEONATO_ANIO, campeonato.ANIO)
            } else {
                remove(KEY_CAMPEONATO_CODIGO)
                remove(KEY_CAMPEONATO_NOMBRE)
                remove(KEY_CAMPEONATO_ANIO)
            }
            apply()
        }
    }

    private fun restaurarCampeonatoGuardado() {
        val codigo = prefs?.getString(KEY_CAMPEONATO_CODIGO, null)
        val nombre = prefs?.getString(KEY_CAMPEONATO_NOMBRE, null)
        val anio = prefs?.getInt(KEY_CAMPEONATO_ANIO, 0) ?: 0

        if (codigo != null && nombre != null && anio > 0) {
            // Restauramos un campeonato básico con los datos guardados
            // Los datos completos se cargarán cuando se observe la lista de campeonatos
            _campeonatoActivo.value = Campeonato(
                CODIGO = codigo,
                CAMPEONATO = nombre,
                ANIO = anio
            )
        }
    }

    /**
     * Actualiza el campeonato activo con datos completos si coincide el código
     * Útil cuando se carga la lista completa de campeonatos
     */
    fun actualizarSiCoincide(campeonato: Campeonato) {
        val actual = _campeonatoActivo.value
        if (actual != null && actual.CODIGO == campeonato.CODIGO) {
            _campeonatoActivo.value = campeonato
        }
    }
}
