package com.example.arki_deportes.data.model

import com.example.arki_deportes.utils.SportType
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Campeonato(
    val CODIGO: String = "",
    val CAMPEONATO: String = "",
    val FECHAALTA: String = "",
    val FECHAINICIO: String = "",
    val FECHAFINAL: String = "",
    val PROVINCIA: String = "",
    val ANIO: Int = 0,
    val HASTAGEXTRAS: String = "",
    val TIMESTAMP_CREACION: Long = 0,
    val TIMESTAMP_MODIFICACION: Long = 0,
    val ORIGEN: String = "MOBILE",
    val DEPORTE: String = SportType.FUTBOL.id,
    val ALIAS: String = "",
    
    // Usamos Any para evitar errores de conversión si en Firebase es número o string
    // Firebase intentará convertirlo automáticamente si usamos los tipos correctos
    val TIEMPOJUEGO: Any? = "45",
    val DURACION: Any? = "0",
    val MANGAS: Any? = "0",
    val VUELTAS: Any? = "0",
    
    val CIRCUITO: String = "",
    val LUGAR: String = ""
) {
    // Funciones auxiliares para obtener los valores como String de forma segura
    fun getTiempoJuegoStr(): String = TIEMPOJUEGO?.toString() ?: "45"
    fun getDuracionStr(): String = DURACION?.toString() ?: "0"
    fun getMangasStr(): String = MANGAS?.toString() ?: "0"
    fun getVueltasStr(): String = VUELTAS?.toString() ?: "0"

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "CODIGO" to CODIGO,
            "CAMPEONATO" to CAMPEONATO.uppercase(),
            "FECHAALTA" to FECHAALTA,
            "FECHAINICIO" to FECHAINICIO,
            "FECHAFINAL" to FECHAFINAL,
            "PROVINCIA" to PROVINCIA.uppercase(),
            "ANIO" to ANIO,
            "HASTAGEXTRAS" to HASTAGEXTRAS,
            "TIMESTAMP_CREACION" to TIMESTAMP_CREACION,
            "TIMESTAMP_MODIFICACION" to TIMESTAMP_MODIFICACION,
            "ORIGEN" to ORIGEN,
            "DEPORTE" to DEPORTE.uppercase(),
            "ALIAS" to ALIAS,
            "TIEMPOJUEGO" to TIEMPOJUEGO,
            "DURACION" to DURACION,
            "MANGAS" to MANGAS,
            "VUELTAS" to VUELTAS,
            "CIRCUITO" to CIRCUITO,
            "LUGAR" to LUGAR
        )
    }

    fun getDeporteTexto(): String = SportType.fromId(DEPORTE).displayName

    companion object {
        fun empty() = Campeonato()
    }
}
