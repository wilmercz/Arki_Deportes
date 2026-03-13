package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class BannerResource(
    val id: String = "",
    val nombre: String = "",
    val urlImagen: String = "",
    val urlVideo: String = "",
    val codigoHtml: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val tipo: String = "IMAGEN", // "IMAGEN", "VIDEO", "HTML"
    val activo: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "urlImagen" to urlImagen,
            "urlVideo" to urlVideo,
            "codigoHtml" to codigoHtml,
            "fechaInicio" to fechaInicio,
            "fechaFin" to fechaFin,
            "tipo" to tipo,
            "activo" to activo
        )
    }
}
