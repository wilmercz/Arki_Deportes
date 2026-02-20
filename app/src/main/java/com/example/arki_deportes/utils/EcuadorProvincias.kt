package com.example.arki_deportes.utils

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * ECUADOR PROVINCIAS - LISTADO MAESTRO
 * ═══════════════════════════════════════════════════════════════════════════
 */
object EcuadorProvincias {
    data class ProvinciaData(
        val nombre: String,
        val capital: String,
        val banderaUrl: String = ""
    )

    val LISTA = listOf(
        ProvinciaData("AZUAY", "CUENCA"),
        ProvinciaData("BOLIVAR", "GUARANDA"),
        ProvinciaData("CANAR", "AZOGUES"),
        ProvinciaData("CARCHI", "TULCAN"),
        ProvinciaData("CHIMBORAZO", "RIOBAMBA"),
        ProvinciaData("COTOPAXI", "LATACUNGA"),
        ProvinciaData("EL ORO", "MACHALA"),
        ProvinciaData("ESMERALDAS", "ESMERALDAS"),
        ProvinciaData("GALAPAGOS", "PUERTO BAQUERIZO MORENO"),
        ProvinciaData("GUAYAS", "GUAYAQUIL"),
        ProvinciaData("IMBABURA", "IBARRA"),
        ProvinciaData("LOJA", "LOJA"),
        ProvinciaData("LOS RIOS", "BABAHOYO"),
        ProvinciaData("MANABI", "PORTOVIEJO"),
        ProvinciaData("MORONA SANTIAGO", "MACAS"),
        ProvinciaData("NAPO", "TENA"),
        ProvinciaData("ORELLANA", "FRANCISCO DE ORELLANA"),
        ProvinciaData("PASTAZA", "PUYO"),
        ProvinciaData("PICHINCHA", "QUITO"),
        ProvinciaData("SANTA ELENA", "SANTA ELENA"),
        ProvinciaData("SANTO DOMINGO DE LOS TSACHILAS", "SANTO DOMINGO"),
        ProvinciaData("SUCUMBIOS", "NUEVA LOJA"),
        ProvinciaData("TUNGURAHUA", "AMBATO"),
        ProvinciaData("ZAMORA CHINCHIPE", "ZAMORA")
    )

    /**
     * Genera un nombre corto para transmisión (primeras 3 o 4 letras)
     */
    fun generarNombreCorto(nombre: String): String {
        val limpio = nombre.replace(" ", "")
        return if (limpio.length > 3) limpio.substring(0, 3).uppercase() else limpio.uppercase()
    }
}
