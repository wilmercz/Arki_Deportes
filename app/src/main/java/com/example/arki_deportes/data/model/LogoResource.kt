package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LogoResource(
    val id: String = "",
    val nombre: String = "",
    val url: String = "",
    val onAir: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "url" to url,
            "onAir" to onAir
        )
    }
}
