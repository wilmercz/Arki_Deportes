package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AnuncioPublicidad(
    val tipo: String = "imagen", // "imagen", "video" o "html"
    val contenido: String = "",  // URL de la imagen/video o código HTML
    val duracion: Int = 10,      // Segundos que durará en pantalla
    val mostrar: Boolean = true  // Control general
)
