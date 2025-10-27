// app/src/main/java/com/example/arki_deportes/data/model/Grupo.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GRUPO - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa un grupo dentro de un campeonato en Firebase Realtime Database.
 * Los grupos son opcionales y se usan cuando el campeonato se divide en fases.
 *
 * Estructura en Firebase:
 * ```
 * GRUPOS/
 *   └── [CODIGOGRUPO]/
 *       ├── CODIGOCAMPEONATO: "PROVINCIAL_2025"
 *       ├── CODIGOGRUPO: "GRUPO_A_123456"
 *       ├── GRUPO: "GRUPO A"
 *       ├── PROVINCIA: "PASTAZA"
 *       ├── ANIO: 2025
 *       ├── CODIGOPROVINCIA: "16"
 *       ├── TIMESTAMP_CREACION: 1705334400000
 *       └── TIMESTAMP_MODIFICACION: 1705334400000
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
@IgnoreExtraProperties
data class Grupo(
    /**
     * Código del campeonato al que pertenece el grupo
     * Ejemplo: "PROVINCIAL_2025_123456"
     */
    val CODIGOCAMPEONATO: String = "",

    /**
     * Código único del grupo
     * Formato: GRUPO_LETRA_TIMESTAMP
     * Ejemplo: "GRUPO_A_1705334400000"
     */
    val CODIGOGRUPO: String = "",

    /**
     * Nombre del grupo (en mayúsculas)
     * Ejemplo: "GRUPO A", "GRUPO B", "ZONA NORTE"
     */
    val GRUPO: String = "",

    /**
     * Provincia donde se juega el grupo (en mayúsculas)
     * Ejemplo: "PASTAZA", "SUCUMBIOS"
     */
    val PROVINCIA: String = "",

    /**
     * Año del grupo
     * Ejemplo: 2025
     */
    val ANIO: Int = 0,

    /**
     * Código de la provincia (código INEC u otro sistema)
     * Ejemplo: "16" para Pastaza
     */
    val CODIGOPROVINCIA: String = "",

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
    val POSICION: Int = 0,
) {
    /**
     * Convierte el objeto a un Map para Firebase
     * Útil para operaciones de guardado/actualización
     *
     * @return HashMap con los datos del grupo
     */
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "CODIGOCAMPEONATO" to CODIGOCAMPEONATO,
            "CODIGOGRUPO" to CODIGOGRUPO,
            "GRUPO" to GRUPO.uppercase(),
            "PROVINCIA" to PROVINCIA.uppercase(),
            "ANIO" to ANIO,
            "CODIGOPROVINCIA" to CODIGOPROVINCIA,
            "TIMESTAMP_CREACION" to TIMESTAMP_CREACION,
            "TIMESTAMP_MODIFICACION" to TIMESTAMP_MODIFICACION,
            "ORIGEN" to ORIGEN
        )
    }

    /**
     * Obtiene el nombre formateado del grupo
     * Ejemplo: "Grupo A - Pastaza"
     *
     * @return String con el nombre formateado
     */
    fun getNombreFormateado(): String {
        return "$GRUPO - $PROVINCIA"
    }

    /**
     * Compañero para crear instancias vacías
     */
    companion object {
        /**
         * Crea una instancia vacía de Grupo
         * Útil para formularios de creación
         */
        fun empty() = Grupo()
    }
}