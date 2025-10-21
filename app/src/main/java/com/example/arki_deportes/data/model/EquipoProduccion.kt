// app/src/main/java/com/example/arki_deportes/data/model/EquipoProduccion.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EQUIPO DE PRODUCCIÓN - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa la configuración del equipo de producción que participa en una
 * transmisión. Los datos se almacenan en el nodo `EquipoProduccion` dentro del
 * esquema principal de Firebase Realtime Database.
 *
 * Estructura esperada en Firebase:
 * ```
 * EquipoProduccion/
 *   ├── default/
 *   │   ├── narrador: "Juan Pérez"
 *   │   ├── comentarista: "Carlos López"
 *   │   ├── bordeCampo: "María Ruiz"
 *   │   ├── anfitriones: ["Ana Torres", "Luis García"]
 *   │   └── timestamp: 1705958400000
 *   └── campeonatos/
 *       └── [CODIGO_CAMPEONATO]/
 *           ├── narrador: "..."
 *           ├── comentarista: "..."
 *           ├── bordeCampo: "..."
 *           ├── anfitriones: ["..."]
 *           └── timestamp: 1705958400000
 * ```
 */
@IgnoreExtraProperties
data class EquipoProduccion(
    /** Responsable principal de la narración de la transmisión. */
    val narrador: String = "",

    /** Comentarista que acompaña al narrador durante el partido. */
    val comentarista: String = "",

    /** Reportero asignado al borde de campo. */
    val bordeCampo: String = "",

    /** Lista de anfitriones o presentadores del programa previo/post. */
    val anfitriones: List<String> = emptyList(),

    /** Timestamp de última actualización en milisegundos. */
    val timestamp: Long = 0L
) {
    /** Indica si el registro está vacío (sin datos significativos). */
    fun isEmpty(): Boolean {
        return narrador.isBlank() &&
            comentarista.isBlank() &&
            bordeCampo.isBlank() &&
            anfitriones.isEmpty()
    }

    /** Crea una copia normalizada (valores sin espacios extra). */
    fun normalized(): EquipoProduccion {
        return copy(
            narrador = narrador.trim(),
            comentarista = comentarista.trim(),
            bordeCampo = bordeCampo.trim(),
            anfitriones = anfitriones.map { it.trim() }.filter { it.isNotBlank() }
        )
    }

    companion object {
        /** Instancia vacía para valores por defecto. */
        fun empty(): EquipoProduccion = EquipoProduccion()
    }
}
