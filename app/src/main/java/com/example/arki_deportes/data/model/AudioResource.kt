package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AudioResource(
    val id: String = "",
    val nombre: String = "",
    val url: String = "",
    val tipo: String = "", // "FX" o "MUSICA"
    val categoria: String = "", // Para FX: "TIRO_ESQUINA", "CORTINA". Para MUSICA: "FUTBOL", "AUTOMOVILISMO", etc.
    val deporte: String = "" // "FUTBOL", "AUTOMOVILISMO", "CICLISMO", "BASQUET"
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "url" to url,
            "tipo" to tipo,
            "categoria" to categoria,
            "deporte" to deporte
        )
    }
}
