// app/src/main/java/com/example/arki_deportes/data/context/CampeonatoContext.kt

package com.example.arki_deportes.data.context

import com.example.arki_deportes.data.model.Campeonato
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CAMPEONATO CONTEXT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Singleton que mantiene el estado global del campeonato seleccionado.
 * Permite acceso desde cualquier parte de la app.
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
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
     * Campeonato actualmente seleccionado
     */
    private var campeonatoActual: Campeonato? = null

    /**
     * Establece el campeonato actual
     * Llamar cuando se selecciona un campeonato
     */
    fun seleccionarCampeonato(campeonato: Campeonato) {
        campeonatoActual = campeonato
    }

    /**
     * Obtiene el campeonato actual
     * @return Campeonato seleccionado o null si no hay ninguno
     */
    fun getCampeonatoActual(): Campeonato? = campeonatoActual

    /**
     * Verifica si hay un campeonato seleccionado
     */
    fun hayCampeonato(): Boolean = campeonatoActual != null

    /**
     * Limpia el campeonato actual
     */
    fun limpiar() {
        campeonatoActual = null
    }
}
