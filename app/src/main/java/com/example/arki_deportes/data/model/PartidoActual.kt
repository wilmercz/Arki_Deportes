// app/src/main/java/com/example/arki_deportes/data/model/PartidoActual.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties
import java.util.Locale

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
     * Tiempo transcurrido del partido.
     * Formato esperado: `mm:ss`.
     *
     * ⚙️ Integración VB.NET:
     *  - El backend debe escribir el valor con dos dígitos para los minutos y segundos (`00:00`).
     *  - El parser de la app tolera temporalmente el formato heredado `mmMss`, pero el sistema
     *    debe dejar de reemplazar `:` por `M` y enviar únicamente `mm:ss`.
     * Ejemplo: "65:45" (65 minutos, 45 segundos)
     */
    val TIEMPO_TRANSCURRIDO: String = "00:00",

    /**

     * Estado actual del partido heredado del backend clásico.

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
     * Indica si el partido está siendo transmitido en vivo (clave heredada).
     */
    val EN_TRANSMISION: Boolean = false,

    /**
     * Indica si el partido está siendo transmitido en vivo (nueva nomenclatura camelCase).
     */
    val enTransmision: Boolean = false,

    /**
     * Estado del partido reportado con la nueva nomenclatura camelCase.
     */
    val estado: String = "",

    /**
     * Número del tiempo o periodo actual del encuentro (1 = primer tiempo, 2 = segundo tiempo,
     * 3 = prórroga, etc.).
     */
    val numeroDeTiempo: Int = 0,

    /**
     * Bandera booleana que indica si la transmisión está pausada.
     */
    val pausado: Boolean = false,

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
        return estadoEfectivo() == Estado.EN_JUEGO
    }

    /**
     * Verifica si el partido se encuentra en transmisión en vivo.
     */
    fun estaEnTransmision(): Boolean {
        return (enTransmision || EN_TRANSMISION || estadoEfectivo() == Estado.EN_JUEGO) && !estaPausado()
    }

    /**
     * Verifica si el partido ha finalizado
     *
     * @return true si el estado es "Finalizado", false si no
     */
    fun haFinalizado(): Boolean {
        return estadoEfectivo() == Estado.FINALIZADO
    }

    /**
     * Verifica si el partido está pausado
     *
     * @return true si el estado es "Pausado", false si no
     */
    fun estaPausado(): Boolean {
        return pausado || estadoEfectivo() == Estado.PAUSADO
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
        return when (estadoEfectivo()) {
            Estado.EN_JUEGO -> "EN JUEGO"
            Estado.FINALIZADO -> "FINALIZADO"
            Estado.PAUSADO -> "PAUSADO"
            Estado.NO_INICIADO -> "NO INICIADO"
            else -> "DESCONOCIDO"
        }
    }

    /**
     * Obtiene el icono según el estado
     *
     * @return String con el emoji del estado
     */
    fun getEstadoIcono(): String {
        return when (estadoEfectivo()) {
            Estado.EN_JUEGO -> "🔴"
            Estado.FINALIZADO -> "✅"
            Estado.PAUSADO -> "⏸️"
            Estado.NO_INICIADO -> "⏱️"
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
        return "$EQUIPO1 $GOLES1 - $GOLES2 $EQUIPO2 (${getTiempoNormalizado()})"
    }

    /**
     * Obtiene el tiempo transcurrido del partido en formato normalizado (`mm:ss`).
     */
    fun getTiempoNormalizado(): String {
        return TiempoTranscurridoParser.normalizar(TIEMPO_TRANSCURRIDO)
    }

    /**
     * Devuelve una copia del modelo con los campos derivados normalizados.
     */
    fun normalizado(): PartidoActual {
        val estadoCanonico = estadoEfectivo()
        val pausadoEfectivo = pausado || estadoCanonico == Estado.PAUSADO
        val transmisionEfectiva = (enTransmision || EN_TRANSMISION || estadoCanonico == Estado.EN_JUEGO) && !pausadoEfectivo

        return copy(
            TIEMPO_TRANSCURRIDO = TiempoTranscurridoParser.normalizar(TIEMPO_TRANSCURRIDO),
            ESTADO = estadoCanonico,
            EN_TRANSMISION = transmisionEfectiva,
            enTransmision = transmisionEfectiva,
            estado = estadoCanonico,
            numeroDeTiempo = numeroDeTiempo.coerceAtLeast(0),
            pausado = pausadoEfectivo
        )
    }

    private fun estadoEfectivo(): String {
        val origen = when {
            estado.isNotBlank() -> estado
            ESTADO.isNotBlank() -> ESTADO
            pausado -> Estado.PAUSADO
            else -> Estado.NO_INICIADO
        }

        return Estado.normalizar(origen)
    }

    private object Estado {
        const val EN_JUEGO = "EnJuego"
        const val FINALIZADO = "Finalizado"
        const val PAUSADO = "Pausado"
        const val NO_INICIADO = "NoIniciado"

        private val equivalencias = mapOf(
            "enjuego" to EN_JUEGO,
            "en_juego" to EN_JUEGO,
            "juego" to EN_JUEGO,
            "jugando" to EN_JUEGO,
            "finalizado" to FINALIZADO,
            "terminado" to FINALIZADO,
            "pausado" to PAUSADO,
            "pausa" to PAUSADO,
            "noiniciado" to NO_INICIADO,
            "pendiente" to NO_INICIADO,
            "no_iniciado" to NO_INICIADO
        )

        fun normalizar(valor: String): String {
            val clave = valor
                .trim()
                .lowercase(Locale.getDefault())
                .replace(" ", "")
            return equivalencias[clave] ?: valor.trim().ifBlank { NO_INICIADO }
        }
    }

    private object TiempoTranscurridoParser {
        private val patron = Regex("""^(?<min>\d{1,3})([:M])(?<seg>\d{2})$""")

        fun normalizar(valor: String): String {
            val limpio = valor.trim()
            if (limpio.isEmpty()) {
                return "00:00"
            }

            val match = patron.matchEntire(limpio)
            if (match != null) {
                val minutos = match.groups["min"]!!.value
                val segundos = match.groups["seg"]!!.value
                val minutosNormalizados = if (minutos.length == 1) minutos.padStart(2, '0') else minutos
                return "$minutosNormalizados:$segundos"
            }

            // Intento adicional por si vienen símbolos mezclados (p. ej. "45M30")
            val reemplazado = limpio.replace('M', ':')
            val fallback = patron.matchEntire(reemplazado)
            if (fallback != null) {
                val minutos = fallback.groups["min"]!!.value
                val segundos = fallback.groups["seg"]!!.value
                val minutosNormalizados = if (minutos.length == 1) minutos.padStart(2, '0') else minutos
                return "$minutosNormalizados:$segundos"
            }

            return limpio
                .replace('M', ':')
                .let { valorNormalizado ->
                    if (valorNormalizado.contains(':')) {
                        val partes = valorNormalizado.split(":")
                        if (partes.size == 2 && partes[1].length == 2) {
                            val minutosNormalizados = partes[0].padStart(2, '0')
                            return@let "$minutosNormalizados:${partes[1]}"
                        }
                    }
                    valorNormalizado
                }
                .ifBlank { "00:00" }
        }
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