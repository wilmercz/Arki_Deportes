// app/src/main/java/com/example/arki_deportes/data/model/Campeonato.kt

package com.example.arki_deportes.data.model

import com.example.arki_deportes.utils.SportType
import com.google.firebase.database.IgnoreExtraProperties
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CAMPEONATO - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa un campeonato de fútbol en Firebase Realtime Database.
 *
 * Estructura en Firebase:
 * ```
 * CAMPEONATOS/
 *   └── [CODIGO]/
 *       ├── CODIGO: "PROVINCIAL_2025_123456"
 *       ├── CAMPEONATO: "PROVINCIAL 2025"
 *       ├── FECHAINICIO: "2025-01-15"
 *       ├── FECHAFINAL: "2025-06-30"
 *       ├── PROVINCIA: "PASTAZA"
 *       ├── ANIO: 2025
 *       ├── HASTAGEXTRAS: "#FutbolPastaza"
 *       ├── TIMESTAMP_CREACION: 1705334400000
 *       └── TIMESTAMP_MODIFICACION: 1705334400000
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
@IgnoreExtraProperties
data class Campeonato(
    /**
     * Código único del campeonato
     * Formato: NOMBRE_AÑO_TIMESTAMP
     * Ejemplo: "PROVINCIAL_2025_1705334400000"
     */
    val CODIGO: String = "",

    /**
     * Nombre del campeonato (en mayúsculas)
     * Ejemplo: "PROVINCIAL 2025", "COPA PASTAZA"
     */
    val CAMPEONATO: String = "",


    /**
     * Fecha de alta del registro en el sistema
     * Formato: yyyy-MM-dd
     * Ejemplo: "2025-01-15"
     */
    val FECHAALTA: String = "",

    /**
     * Fecha de inicio del campeonato
     * Formato: yyyy-MM-dd
     * Ejemplo: "2025-01-20"
     */
    val FECHAINICIO: String = "",

    /**
     * Fecha final del campeonato
     * Formato: yyyy-MM-dd
     * Ejemplo: "2025-06-30"
     */
    val FECHAFINAL: String = "",

    /**
     * Provincia donde se realiza el campeonato (en mayúsculas)
     * Ejemplo: "PASTAZA", "SUCUMBIOS"
     */
    val PROVINCIA: String = "",

    /**
     * Año del campeonato
     * Ejemplo: 2025
     */
    val ANIO: Int = 0,

    /**
     * Hashtags adicionales para redes sociales
     * Ejemplo: "#FutbolPastaza #Provincial2025"
     */
    val HASTAGEXTRAS: String = "",

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
     * Deporte al que pertenece el campeonato
     * Ejemplo: "FUTBOL", "BALONCESTO"
     */
    val DEPORTE: String = SportType.FUTBOL.id
) {
    /**
     * Convierte el objeto a un Map para Firebase
     * Útil para operaciones de guardado/actualización
     *
     * @return HashMap con los datos del campeonato
     */
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "CODIGO" to CODIGO,
            "CAMPEONATO" to CAMPEONATO.trim(),
            "FECHAALTA" to FECHAALTA,
            "FECHAINICIO" to FECHAINICIO,
            "FECHAFINAL" to FECHAFINAL,
            "PROVINCIA" to PROVINCIA.uppercase(),
            "ANIO" to ANIO,
            "HASTAGEXTRAS" to HASTAGEXTRAS,
            "TIMESTAMP_CREACION" to TIMESTAMP_CREACION,
            "TIMESTAMP_MODIFICACION" to TIMESTAMP_MODIFICACION,
            "ORIGEN" to ORIGEN,
            "DEPORTE" to DEPORTE.uppercase()
        )
    }

    /**
     * Verifica si el campeonato está activo actualmente
     * Un campeonato está activo si la fecha actual está entre
     * FECHAINICIO y FECHAFINAL
     *
     * @return true si está activo, false si no
     */
    fun estaActivo(): Boolean {
        // TODO: Implementar lógica de comparación de fechas
        // Comparar fecha actual con FECHAINICIO y FECHAFINAL
        return true
    }

    /**
     * Verifica si el campeonato ya finalizó
     *
     * @return true si ya finalizó, false si no
     */
    fun haFinalizado(): Boolean {
        // TODO: Implementar lógica de comparación de fechas
        // Comparar fecha actual con FECHAFINAL
        return false
    }

    /**
     * Obtiene el nombre formateado del campeonato
     * Ejemplo: "Provincial 2025 - Pastaza"
     *
     * @return String con el nombre formateado
     */
    fun getNombreFormateado(): String {
        return "$CAMPEONATO - $PROVINCIA"
    }

    /** Obtiene el nombre legible del deporte. */
    fun getDeporteTexto(): String = SportType.fromId(DEPORTE).displayName

    /**
     * Compañero para crear instancias vacías
     */
    companion object {
        /**
         * Crea una instancia vacía de Campeonato
         * Útil para formularios de creación
         */
        fun empty() = Campeonato()
    }
}