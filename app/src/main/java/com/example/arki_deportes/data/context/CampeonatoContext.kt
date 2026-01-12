// app/src/main/java/com/example/arki_deportes/data/context/CampeonatoContext.kt

package com.example.arki_deportes.data.context

import com.example.arki_deportes.data.model.Campeonato

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
