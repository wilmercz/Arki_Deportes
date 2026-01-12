// app/src/main/java/com/example/arki_deportes/data/context/PartidoContext.kt

package com.example.arki_deportes.data.context

import com.example.arki_deportes.data.model.Partido

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PARTIDO CONTEXT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Singleton que mantiene el estado global del partido activo.
 * Permite acceso desde cualquier parte de la app.
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
object PartidoContext {
    /**
     * Partido actualmente activo/seleccionado
     */
    private var partidoActivo: Partido? = null

    /**
     * Establece el partido activo
     * Llamar cuando se selecciona un partido
     */
    fun setPartidoActivo(partido: Partido) {
        partidoActivo = partido
    }

    /**
     * Obtiene el partido activo
     * @return Partido activo o null si no hay ninguno
     */
    fun getPartidoActivo(): Partido? = partidoActivo

    /**
     * Verifica si hay un partido activo
     */
    fun hayPartido(): Boolean = partidoActivo != null

    /**
     * Limpia el partido activo
     */
    fun limpiar() {
        partidoActivo = null
    }
}
