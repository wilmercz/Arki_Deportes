// app/src/main/java/com/example/arki_deportes/data/context/DeporteContext.kt

package com.example.arki_deportes.data.context

import com.example.arki_deportes.utils.SportType

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEPORTE CONTEXT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Singleton que mantiene el estado global del deporte seleccionado.
 * Permite acceso desde cualquier parte de la app.
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
object DeporteContext {
    /**
     * Deporte actualmente seleccionado
     * Por defecto: FUTBOL
     */
    private var deporteActual: SportType = SportType.FUTBOL

    /**
     * Establece el deporte actual
     * Llamar cuando se selecciona un campeonato con un deporte específico
     */
    fun seleccionarDeporte(deporte: SportType) {
        deporteActual = deporte
    }

    /**
     * Obtiene el deporte actual
     * @return Deporte seleccionado (nunca null, por defecto FUTBOL)
     */
    fun getDeporteActual(): SportType = deporteActual

    /**
     * Limpia el deporte actual (resetea a FUTBOL)
     */
    fun limpiar() {
        deporteActual = SportType.FUTBOL
    }
}
