package com.example.arki_deportes.data.model

import com.example.arki_deportes.utils.SportType
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Campeonato(
    @get:PropertyName("CODIGO") @set:PropertyName("CODIGO")
    var CODIGO: String = "",
    
    @get:PropertyName("CAMPEONATO") @set:PropertyName("CAMPEONATO")
    var CAMPEONATO: String = "",
    
    @get:PropertyName("FECHAALTA") @set:PropertyName("FECHAALTA")
    var FECHAALTA: String = "",
    
    @get:PropertyName("FECHAINICIO") @set:PropertyName("FECHAINICIO")
    var FECHAINICIO: String = "",
    
    @get:PropertyName("FECHAFINAL") @set:PropertyName("FECHAFINAL")
    var FECHAFINAL: String = "",
    
    @get:PropertyName("PROVINCIA") @set:PropertyName("PROVINCIA")
    var PROVINCIA: String = "",
    
    @get:PropertyName("ANIO") @set:PropertyName("ANIO")
    var ANIO: Int = 0,
    
    @get:PropertyName("HASTAGEXTRAS") @set:PropertyName("HASTAGEXTRAS")
    var HASTAGEXTRAS: String = "",
    
    @get:PropertyName("TIMESTAMP_CREACION") @set:PropertyName("TIMESTAMP_CREACION")
    var TIMESTAMP_CREACION: Long = 0,
    
    @get:PropertyName("TIMESTAMP_MODIFICACION") @set:PropertyName("TIMESTAMP_MODIFICACION")
    var TIMESTAMP_MODIFICACION: Long = 0,
    
    @get:PropertyName("ORIGEN") @set:PropertyName("ORIGEN")
    var ORIGEN: String = "MOBILE",
    
    @get:PropertyName("DEPORTE") @set:PropertyName("DEPORTE")
    var DEPORTE: String = SportType.FUTBOL.id,
    
    @get:PropertyName("ALIAS") @set:PropertyName("ALIAS")
    var ALIAS: String = "",
    
    // Usamos Any para evitar errores de conversión si en Firebase es número o string
    @get:PropertyName("TIEMPOJUEGO") @set:PropertyName("TIEMPOJUEGO")
    var TIEMPOJUEGO: Any? = "45",
    
    @get:PropertyName("DURACION") @set:PropertyName("DURACION")
    var DURACION: Any? = "0",
    
    @get:PropertyName("MANGAS") @set:PropertyName("MANGAS")
    var MANGAS: Any? = "0",
    
    @get:PropertyName("VUELTAS") @set:PropertyName("VUELTAS")
    var VUELTAS: Any? = "0",
    
    @get:PropertyName("CIRCUITO") @set:PropertyName("CIRCUITO")
    var CIRCUITO: String = "",
    
    @get:PropertyName("LUGAR") @set:PropertyName("LUGAR")
    var LUGAR: String = ""
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
