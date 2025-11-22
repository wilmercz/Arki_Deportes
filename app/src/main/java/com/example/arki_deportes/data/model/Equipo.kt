// app/src/main/java/com/example/arki_deportes/data/model/Equipo.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EQUIPO - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa un equipo de fútbol en Firebase Realtime Database.
 *
 * Estructura en Firebase:
 * ```
 * EQUIPOS/
 *   └── [CODIGOEQUIPO]/
 *       ├── CODIGOEQUIPO: "BARCELONA_123456"
 *       ├── EQUIPO: "BARCELONA"
 *       ├── PROVINCIA: "PASTAZA"
 *       ├── FECHAALTA: "2025-01-15"
 *       ├── ESCUDOLOCAL: "escudo_barcelona.png"
 *       ├── ESCUDOLINK: "https://firebase.../escudo.png"
 *       ├── CODIGOCAMPEONATO: "PROVINCIAL_2025"
 *       ├── EQUIPO_NOMBRECOMPLETO: "BARCELONA SPORTING CLUB"
 *       ├── TIMESTAMP_CREACION: 1705334400000
 *       └── TIMESTAMP_MODIFICACION: 1705334400000
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
@IgnoreExtraProperties
data class Equipo(
    /**
     * Código único del equipo
     * Formato: NOMBRE_TIMESTAMP
     * Ejemplo: "BARCELONA_1705334400000"
     */
    val CODIGOEQUIPO: String = "",

    /**
     * Nombre corto del equipo (en mayúsculas)
     * Ejemplo: "BARCELONA", "INDEPENDIENTE"
     */
    val EQUIPO: String = "",

    /**
     * Provincia de origen del equipo (en mayúsculas)
     * Ejemplo: "PASTAZA", "SUCUMBIOS"
     */
    val PROVINCIA: String = "",

    /**
     * Fecha de alta del registro en el sistema
     * Formato: yyyy-MM-dd
     * Ejemplo: "2025-01-15"
     */
    val FECHAALTA: String = "",

    /**
     * Nombre del archivo del escudo local
     * Ejemplo: "escudo_barcelona.png"
     */
    val ESCUDOLOCAL: String = "",

    /**
     * URL del escudo en Firebase Storage
     * Ejemplo: "https://firebasestorage.googleapis.com/.../escudo.png"
     */
    val ESCUDOLINK: String = "",

    /**
     * Código del campeonato al que pertenece el equipo
     * Ejemplo: "PROVINCIAL_2025_123456"
     */
    val CODIGOCAMPEONATO: String = "",

    /**
     * Nombre completo del equipo (en mayúsculas)
     * Ejemplo: "BARCELONA SPORTING CLUB"
     */
    val EQUIPO_NOMBRECOMPLETO: String = "",

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
     * Código del grupo al que pertenece el equipo
     * Ejemplo: "GRUPO_A_1705334400000"
     */
    val CODIGOGRUPO: String = "",

    /**
     * Posición del equipo en el grupo
     * Valores: 1 (primero), 2 (segundo), 3 (tercero), 4 (cuarto)
     * Si no tiene valor 1-4, significa que perdió todos los partidos
     */
    val POSICION: Int = 0,
    val ES_MEJOR_SEGUNDO: Boolean = false,
    val NOMBRESERIE: String = "",
    val NOMBRECOMPLETO: String = "",
    val SINCRONIZADO: Int = 0,
    val HASH_REGISTRO: String = "",
) {
    /**
     * Convierte el objeto a un Map para Firebase
     * Útil para operaciones de guardado/actualización
     *
     * @return HashMap con los datos del equipo
     */
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "CODIGOEQUIPO" to CODIGOEQUIPO,
            "EQUIPO" to EQUIPO.uppercase(),
            "PROVINCIA" to PROVINCIA.uppercase(),
            "FECHAALTA" to FECHAALTA,
            "ESCUDOLOCAL" to ESCUDOLOCAL,
            "ESCUDOLINK" to ESCUDOLINK,
            "CODIGOCAMPEONATO" to CODIGOCAMPEONATO,
            "EQUIPO_NOMBRECOMPLETO" to EQUIPO_NOMBRECOMPLETO.uppercase(),
            "TIMESTAMP_CREACION" to TIMESTAMP_CREACION,
            "TIMESTAMP_MODIFICACION" to TIMESTAMP_MODIFICACION,
            "ORIGEN" to ORIGEN,
            "CODIGOGRUPO" to CODIGOGRUPO,
            "POSICION" to POSICION,
            "ES_MEJOR_SEGUNDO" to ES_MEJOR_SEGUNDO,
            "NOMBRESERIE" to NOMBRESERIE,
            "NOMBRECOMPLETO" to (NOMBRECOMPLETO.ifBlank { EQUIPO_NOMBRECOMPLETO }),
            "SINCRONIZADO" to SINCRONIZADO,
            "HASH_REGISTRO" to HASH_REGISTRO
        )
    }

    /**
     * Verifica si el equipo tiene un escudo cargado
     *
     * @return true si tiene escudo, false si no
     */
    fun tieneEscudo(): Boolean {
        return ESCUDOLINK.isNotBlank()
    }

    /**
     * Obtiene el nombre a mostrar (completo si existe, sino el corto)
     *
     * @return String con el nombre para mostrar
     */
    fun getNombreDisplay(): String {
        return if (EQUIPO_NOMBRECOMPLETO.isNotBlank()) {
            EQUIPO_NOMBRECOMPLETO
        } else {
            EQUIPO
        }
    }

    /**
     * Compañero para crear instancias vacías
     */
    companion object {
        /**
         * Crea una instancia vacía de Equipo
         * Útil para formularios de creación
         */
        fun empty() = Equipo()
    }
}