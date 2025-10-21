// app/src/main/java/com/example/arki_deportes/data/model/Mencion.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MENCION - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa una mención utilizada durante las transmisiones. Las menciones se
 * almacenan en Firebase Realtime Database bajo el nodo "Menciones" y permiten
 * gestionar mensajes patrocinados o informativos que se muestran en pantalla.
 *
 * Estructura en Firebase:
 * ```
 * Menciones/
 *   └── [ID_UNICO]/
 *       ├── texto: "Disfruta de promociones especiales"
 *       ├── tipo: "Patrocinio"
 *       ├── activo: true
 *       ├── orden: 0
 *       └── timestamp: 1705770000000
 * ```
 */
@IgnoreExtraProperties
data class Mencion(
    /** Texto a presentar durante la transmisión. */
    val texto: String = "",

    /** Categoría o clasificación de la mención. */
    val tipo: String = "",

    /** Indica si la mención está disponible para mostrarse. */
    val activo: Boolean = true,

    /** Posición relativa dentro del listado ordenado. */
    val orden: Long = 0,

    /** Momento de la última actualización en milisegundos. */
    val timestamp: Long = 0,

    /** Identificador del nodo en Firebase. */
    @get:Exclude val id: String = ""
) {
    /**
     * Convierte el modelo a un mapa compatible con Firebase.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "texto" to texto,
        "tipo" to tipo,
        "activo" to activo,
        "orden" to orden,
        "timestamp" to timestamp
    )

    /**
     * Devuelve una copia del modelo asociada al identificador indicado.
     */
    fun withId(identifier: String): Mencion = copy(id = identifier)
}
