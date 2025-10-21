// app/src/main/java/com/example/arki_deportes/data/model/PartidoActual.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PARTIDO ACTUAL - MODELO DE DATOS EN TIEMPO REAL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa el partido que se está jugando EN VIVO en Firebase Realtime Database.
 * Este nodo es actualizado constantemente por el software VB.NET durante transmisiones.
 *
 * ⚠️ IMPORTANTE: La app móvil SOLO LEE este nodo, NUNCA escribe en él.
 *
 * Estructura en Firebase:
 * ```
 * PartidoActual/
 *   ├── EQUIPO1: "BARCELONA"
 *   ├── EQUIPO2: "INDEPENDIENTE"
 *   ├── GOLES1: 2
 *   ├── GOLES2: 1
 *   ├── TIEMPO_TRANSCURRIDO: "65:45"
 *   ├── ESTADO: "EnJuego"
 *   ├── TARJETAS_AMARILLAS1: 2
 *   ├── TARJETAS_AMARILLAS2: 1
 *   ├── TARJETAS_ROJAS1: 0
 *   ├── TARJETAS_ROJAS2: 1
 *   ├── ESCUDO1_URL: "https://firebase.../escudo1.png"
 *   ├── ESCUDO2_URL: "https://firebase.../escudo2.png"
 *   └── ULTIMA_ACTUALIZACION: 1705334400000
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
@IgnoreExtraProperties
data class PartidoActual(
    /**
     * Nombre del equipo 1 (en mayúsculas)
     * Ejemplo: "BARCELONA"
     */
    val EQUIPO1: String = "",

    /**
     * Nombre del equipo 2 (en mayúsculas)
     * Ejemplo: "INDEPENDIENTE"
     */
    val EQUIPO2: String = "",

    /**
     * Goles del equipo 1
     * Ejemplo: 2
     */
    val GOLES1: Int = 0,

    /**
     * Goles del equipo 2
     * Ejemplo: 1
     */
    val GOLES2: Int = 0,

    /**
     * Tiempo transcurrido del partido
     * Formato: MM:SS
     * Ejemplo: "65:45" (65 minutos, 45 segundos)
     */
    val TIEMPO_TRANSCURRIDO: String = "00:00",

    /**
     * Indica si el cronómetro del partido está en marcha
     * true cuando el software de transmisión mantiene corriendo el tiempo
     */
    val CRONOMETRANDO: Boolean = false,

    /**
     * Estado actual del partido
     * Valores posibles:
     * - "EnJuego": Partido en curso
     * - "Finalizado": Partido terminado
     * - "Pausado": Partido pausado (medio tiempo, lesión, etc.)
     * - "NoIniciado": Partido no ha comenzado
     */
    val ESTADO: String = "NoIniciado",

    /**
     * Cantidad de tarjetas amarillas del equipo 1
     * Ejemplo: 2
     */
    val TARJETAS_AMARILLAS1: Int = 0,

    /**
     * Cantidad de tarjetas amarillas del equipo 2
     * Ejemplo: 1
     */
    val TARJETAS_AMARILLAS2: Int = 0,

    /**
     * Cantidad de tarjetas rojas del equipo 1
     * Ejemplo: 0
     */
    val TARJETAS_ROJAS1: Int = 0,

    /**
     * Cantidad de tarjetas rojas del equipo 2
     * Ejemplo: 1
     */
    val TARJETAS_ROJAS2: Int = 0,

    /**
     * URL del escudo del equipo 1 en Firebase Storage
     * Ejemplo: "https://firebasestorage.googleapis.com/.../escudo1.png"
     */
    val ESCUDO1_URL: String = "",

    /**
     * URL del escudo del equipo 2 en Firebase Storage
     * Ejemplo: "https://firebasestorage.googleapis.com/.../escudo2.png"
     */
    val ESCUDO2_URL: String = "",

    /**
     * Timestamp de la última actualización (en milisegundos)
     * Generado por VB.NET cada vez que actualiza los datos
     */
    val ULTIMA_ACTUALIZACION: Long = 0
) {
    /**
     * Verifica si hay un partido en curso
     *
     * @return true si el estado es "EnJuego", false si no
     */
    fun estaEnJuego(): Boolean {
        return ESTADO == "EnJuego"
    }

    /**
     * Verifica si el partido ha finalizado
     *
     * @return true si el estado es "Finalizado", false si no
     */
    fun haFinalizado(): Boolean {
        return ESTADO == "Finalizado"
    }

    /**
     * Verifica si el partido está pausado
     *
     * @return true si el estado es "Pausado", false si no
     */
    fun estaPausado(): Boolean {
        return ESTADO == "Pausado"
    }

    /**
     * Obtiene el marcador formateado
     * Ejemplo: "2 - 1"
     *
     * @return String con el marcador
     */
    fun getMarcador(): String {
        return "$GOLES1 - $GOLES2"
    }

    /**
     * Obtiene el texto del estado en español amigable
     *
     * @return String con el estado legible
     */
    fun getEstadoTexto(): String {
        return when (ESTADO) {
            "EnJuego" -> "EN JUEGO"
            "Finalizado" -> "FINALIZADO"
            "Pausado" -> "PAUSADO"
            "NoIniciado" -> "NO INICIADO"
            else -> "DESCONOCIDO"
        }
    }

    /**
     * Obtiene el icono según el estado
     *
     * @return String con el emoji del estado
     */
    fun getEstadoIcono(): String {
        return when (ESTADO) {
            "EnJuego" -> "🔴"
            "Finalizado" -> "✅"
            "Pausado" -> "⏸️"
            "NoIniciado" -> "⏱️"
            else -> "❓"
        }
    }

    /**
     * Verifica si hay datos de partido cargados
     *
     * @return true si hay equipos definidos, false si no
     */
    fun hayPartido(): Boolean {
        return EQUIPO1.isNotBlank() && EQUIPO2.isNotBlank()
    }

    /**
     * Obtiene información resumida del partido
     * Ejemplo: "BARCELONA 2 - 1 INDEPENDIENTE (65:45)"
     *
     * @return String con resumen del partido
     */
    fun getResumen(): String {
        return "$EQUIPO1 $GOLES1 - $GOLES2 $EQUIPO2 ($TIEMPO_TRANSCURRIDO)"
    }

    /**
     * Compañero para crear instancias vacías
     */
    companion object {
        /**
         * Crea una instancia vacía de PartidoActual
         * Útil cuando no hay partido en vivo
         */
        fun empty() = PartidoActual()
    }
}