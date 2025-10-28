// app/src/main/java/com/example/arki_deportes/data/model/Partido.kt

package com.example.arki_deportes.data.model

import com.example.arki_deportes.utils.SportType
import com.google.firebase.database.IgnoreExtraProperties

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PARTIDO - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa un partido de fútbol en Firebase Realtime Database.
 *
 * Estructura en Firebase:
 * ```
 * PARTIDOS/
 *   └── [CODIGOPARTIDO]/
 *       ├── CODIGOPARTIDO: "BARCELONA_INDEPENDIENTE_123456"
 *       ├── EQUIPO1: "BARCELONA"
 *       ├── EQUIPO2: "INDEPENDIENTE"
 *       ├── CAMPEONATOCODIGO: "PROVINCIAL_2025"
 *       ├── CAMPEONATOTXT: "PROVINCIAL 2025"
 *       ├── FECHAALTA: "2025-01-15"
 *       ├── FECHA_PARTIDO: "2025-01-20"
 *       ├── HORA_PARTIDO: "14:00"
 *       ├── ESTADIO: "MUNICIPAL"
 *       ├── PROVINCIA: "PASTAZA"
 *       ├── GOLES1: "2"
 *       ├── GOLES2: "1"
 *       ├── ETAPA: 0
 *       └── TRANSMISION: true
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
@IgnoreExtraProperties
data class Partido(
    /**
     * Código único del partido
     * Formato: EQUIPO1_EQUIPO2_TIMESTAMP
     * Ejemplo: "BARCELONA_INDEPENDIENTE_1705334400000"
     */
    val CODIGOPARTIDO: String = "",

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
     * Código del campeonato al que pertenece el partido
     * Ejemplo: "PROVINCIAL_2025_123456"
     */
    val CAMPEONATOCODIGO: String = "",

    /**
     * Nombre del campeonato (texto legible)
     * Ejemplo: "PROVINCIAL 2025"
     */
    val CAMPEONATOTXT: String = "",

    /**
     * Fecha de alta del registro en el sistema
     * Formato: yyyy-MM-dd
     * Ejemplo: "2025-01-15"
     */
    val FECHAALTA: String = "",

    /**
     * Fecha en que se jugará el partido
     * Formato: yyyy-MM-dd
     * Ejemplo: "2025-01-20"
     */
    val FECHA_PARTIDO: String = "",

    /**
     * Hora en que se jugará el partido
     * Formato: HH:mm
     * Ejemplo: "14:00"
     */
    val HORA_PARTIDO: String = "",

    /**
     * Texto para publicar en Facebook (opcional)
     * Puede incluir hashtags y descripción del partido
     */
    val TEXTOFACEBOOK: String = "",

    /**
     * Nombre del estadio o cancha donde se jugará
     * Ejemplo: "ESTADIO MUNICIPAL", "CANCHA SINTÉTICA"
     */
    val ESTADIO: String = "",

    /**
     * Provincia donde se jugará el partido (en mayúsculas)
     * Ejemplo: "PASTAZA", "SUCUMBIOS"
     */
    val PROVINCIA: String = "",

    /**
     * Tiempo de juego en minutos
     * Por defecto: "90"
     */
    val TIEMPOJUEGO: String = "90",

    /**
     * Goles del equipo 1
     * String numérico. Ejemplo: "2", "0"
     */
    val GOLES1: String = "0",

    /**
     * Goles del equipo 2
     * String numérico. Ejemplo: "1", "0"
     */
    val GOLES2: String = "0",

    /**
     * Año del partido
     * Ejemplo: 2025
     */
    val ANIO: Int = 0,

    /**
     * Código del equipo 1
     * Ejemplo: "BARCELONA_123456"
     */
    val CODIGOEQUIPO1: String = "",

    /**
     * Código del equipo 2
     * Ejemplo: "INDEPENDIENTE_123456"
     */
    val CODIGOEQUIPO2: String = "",

    /**
     * Indica si el partido será transmitido
     * true = se transmitirá, false = no se transmitirá
     */
    val TRANSMISION: Boolean = false,

    /**
     * Etapa del campeonato
     * 0 = Ninguno/Fase de Grupos
     * 1 = Cuartos de Final
     * 2 = Semifinal
     * 3 = Final
     */
    val ETAPA: Int = 0,

    /**
     * Lugar específico del partido
     * Puede incluir ciudad, sector, etc.
     */
    val LUGAR: String = "",

    /**
     * Timestamp de creación del registro (en milisegundos)
     * Generado automáticamente por Firebase ServerValue.TIMESTAMP
     */
    val TIMESTAMP_CREACION: Long = 0,

    /**
     * Timestamp de última modificación (en milisegundos)
     * Se actualiza cada vez que se edita el registro
     */
    val TIMESTAMP_MODIFICACION: Long = 0,

    /**
     * Origen del registro
     * Valores: "MOBILE" o "DESKTOP"
     * Indica desde qué aplicación se creó el registro
     */
    val ORIGEN: String = "MOBILE",

    /**
     * Deporte al que pertenece el partido
     */
    val DEPORTE: String = SportType.FUTBOL.id,

    /**
     * Estado actual del partido en tiempo real (tiempo de juego actual)
     * null o ausente = Partido programado, no ha comenzado
     * "0T" = Preparación, aún no comienza
     * "1T" = Primer tiempo en curso
     * "2T" = Entre tiempo (descanso)
     * "3T" = Segundo tiempo en curso
     * "4T" = Tiempo completo (90 minutos cumplidos)
     * "5T" = Se va a definir por penales
     * "6T" = Partido finalizado
     *
     * NOTA: No confundir con TIEMPOJUEGO (duración configurada del partido)
     */
    val TIEMPODEJUEGO: String? = null,

    /**
     * Timestamp de cuando inició el cronómetro del partido
     * Se registra al iniciar el primer tiempo ("1T")
     * Formato: milisegundos desde epoch (Unix timestamp)
     * Ejemplo: 1729796400000 representa Oct 24, 2025 18:00:00 GMT
     */
    val CRONOMETRO_INICIO: Long? = null,

    /**
     * Timestamp de cuando finalizó el cronómetro
     * Se registra al finalizar el partido ("6T")
     * Formato: milisegundos desde epoch (Unix timestamp)
     * null = El partido aún no ha finalizado
     */
    val CRONOMETRO_FIN: Long? = null,

    /**
     * Penales convertidos por el equipo 1
     * Se usa cuando el partido se define por penales (ESTADO_PARTIDO = "5T" o "6T")
     * Valor por defecto: 0
     */
    val PENALES1: Int = 0,

    /**
     * Penales convertidos por el equipo 2
     * Se usa cuando el partido se define por penales (ESTADO_PARTIDO = "5T" o "6T")
     * Valor por defecto: 0
     */
    val PENALES2: Int = 0,

    /**
     * Nombre del equipo ganador
     * Se establece cuando el partido finaliza
     * Puede ser EQUIPO1, EQUIPO2, o vacío si es empate
     */
    val NOMBREGANADOR: String = ""
) {
    /**
     * Convierte el objeto a un Map para Firebase
     * Útil para operaciones de guardado/actualización
     *
     * @return HashMap con los datos del partido
     */
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "CODIGOPARTIDO" to CODIGOPARTIDO,
            "EQUIPO1" to EQUIPO1.uppercase(),
            "EQUIPO2" to EQUIPO2.uppercase(),
            "CAMPEONATOCODIGO" to CAMPEONATOCODIGO,
            "CAMPEONATOTXT" to CAMPEONATOTXT.uppercase(),
            "FECHAALTA" to FECHAALTA,
            "FECHA_PARTIDO" to FECHA_PARTIDO,
            "HORA_PARTIDO" to HORA_PARTIDO,
            "TEXTOFACEBOOK" to TEXTOFACEBOOK,
            "ESTADIO" to ESTADIO.uppercase(),
            "PROVINCIA" to PROVINCIA.uppercase(),
            "TIEMPOJUEGO" to TIEMPOJUEGO,
            "GOLES1" to GOLES1,
            "GOLES2" to GOLES2,
            "ANIO" to ANIO,
            "CODIGOEQUIPO1" to CODIGOEQUIPO1,
            "CODIGOEQUIPO2" to CODIGOEQUIPO2,
            "TRANSMISION" to TRANSMISION,
            "ETAPA" to ETAPA,
            "LUGAR" to LUGAR.uppercase(),
            "TIMESTAMP_CREACION" to TIMESTAMP_CREACION,
            "TIMESTAMP_MODIFICACION" to TIMESTAMP_MODIFICACION,
            "ORIGEN" to ORIGEN,
            "DEPORTE" to DEPORTE.uppercase(),
            "PENALES1" to PENALES1,
            "PENALES2" to PENALES2,
            "NOMBREGANADOR" to NOMBREGANADOR.uppercase()
        )

    }

    /**
     * Verifica si el partido ya se jugó (tiene resultado)
     *
     * @return true si ya tiene goles registrados, false si no
     */
    fun tieneResultado(): Boolean {
        return GOLES1.toIntOrNull() != null && GOLES2.toIntOrNull() != null
    }

    /**
     * Obtiene el marcador formateado
     * Ejemplo: "2 - 1"
     *
     * @return String con el marcador o "vs" si no hay resultado
     */
    fun getMarcador(): String = if (tieneResultado()) {
        "$GOLES1 - $GOLES2"
    } else {
        "vs"
    }

    /** Obtiene la etiqueta del marcador de acuerdo al deporte. */
    fun getMarcadorLabel(): String = sportType().scoreboardLabel

    /** Obtiene la etiqueta para los goles/puntos individuales. */
    fun getAnotacionesLabel(): String = sportType().teamScoreLabel

    /** Obtiene el nombre legible del deporte. */
    fun getDeporteTexto(): String = sportType().displayName

    /** Obtiene el texto descriptivo del tiempo de juego configurado. */
    fun getTiempoJuegoDescripcion(): String {
        val valor = TIEMPOJUEGO.trim()
        if (valor.isEmpty()) return ""
        val sufijo = sportType().durationUnitSuffix
        return "$valor $sufijo"
    }

    /** Obtiene la etiqueta del campo de duración según el deporte. */
    fun getTiempoJuegoLabel(): String = sportType().scheduleDurationLabel

    private fun sportType(): SportType = SportType.fromId(DEPORTE)

    /**
     * Obtiene el texto completo del partido
     * Ejemplo: "BARCELONA 2 - 1 INDEPENDIENTE"
     *
     * @return String formateado con los equipos y marcador
     */
    fun getTextoCompleto(): String {
        return "$EQUIPO1 ${getMarcador()} $EQUIPO2"
    }

    /**
     * Obtiene el nombre de la etapa
     *
     * @return String con el nombre de la etapa
     */
    fun getNombreEtapa(): String {
        return when (ETAPA) {
            1 -> "Cuartos de Final"
            2 -> "Semifinal"
            3 -> "Final"
            else -> "Fase de Grupos"
        }
    }

    /**
     * Compañero para crear instancias vacías
     */
    companion object {
        /**
         * Crea una instancia vacía de Partido
         * Útil para formularios de creación
         */
        fun empty() = Partido()
    }
}