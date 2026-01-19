package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Serie(
    val CODIGOSERIE: String = "",
    val CODIGOCAMPEONATO: String = "",
    val NOMBRESERIE: String = "",
    val DESCRIPCION: String = "",
    val CANTGRUPOS: Int = 0,
    val REGLA_CLASIFICACION: String = "",
    val EQUIPOS_CLASIFICAN: Int = 0,
    val FECHAALTA: String = "",
    val ANIO: Int = 0,
    val TIMESTAMP_CREACION: Long = 0,
    val TIMESTAMP_MODIFICACION: Long = 0,
    val ORIGEN: String = "MOBILE"
) {
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "CODIGOSERIE" to CODIGOSERIE,
            "CODIGOCAMPEONATO" to CODIGOCAMPEONATO,
            "NOMBRESERIE" to NOMBRESERIE.uppercase(),
            "DESCRIPCION" to DESCRIPCION,
            "CANTGRUPOS" to CANTGRUPOS,
            "REGLA_CLASIFICACION" to REGLA_CLASIFICACION,
            "EQUIPOS_CLASIFICAN" to EQUIPOS_CLASIFICAN,
            "FECHAALTA" to FECHAALTA,
            "ANIO" to ANIO,
            "TIMESTAMP_CREACION" to TIMESTAMP_CREACION,
            "TIMESTAMP_MODIFICACION" to TIMESTAMP_MODIFICACION,
            "ORIGEN" to ORIGEN
        )
    }

    fun getNombreFormateado(): String = "Serie $NOMBRESERIE"

    fun getReglaTexto(): String {
        return when (REGLA_CLASIFICACION) {
            "1ROS_Y_2DOS" -> "Clasifican 1° y 2° de cada grupo"
            "1ROS_Y_MEJOR_2DO" -> "Clasifican 1° y el mejor 2°"
            "SOLO_1ROS" -> "Solo clasifican los 1° de cada grupo"
            else -> "No definida"
        }
    }

    companion object {
        fun empty() = Serie()

        val REGLAS_CLASIFICACION = listOf(
            "1ROS_Y_2DOS" to "Clasifican 1° y 2° de cada grupo",
            "1ROS_Y_MEJOR_2DO" to "Clasifican 1° y el mejor 2°",
            "SOLO_1ROS" to "Solo clasifican los 1° de cada grupo"
        )
    }
}