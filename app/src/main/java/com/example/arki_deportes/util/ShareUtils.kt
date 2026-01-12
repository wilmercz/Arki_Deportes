package com.example.arki_deportes.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.arki_deportes.data.model.Partido
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Utilidades para construir textos y lanzar Intents de compartido.
 */
object ShareUtils {

    /** Paquete oficial de WhatsApp. */
    const val WHATSAPP_PACKAGE: String = "com.whatsapp"

    private val displayLocale = Locale("es", "ES")
    private val inputDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val outputDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", displayLocale)

    // En tu app suele venir "HH:mm"
    private val inputTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val outputTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(displayLocale)

    /**
     * Construye el mensaje para compartir un partido.
     * Adaptado al modelo Partido.kt actual (VB.NET compatible).
     */
    fun buildPartidoShareMessage(partido: Partido): String {
        val equipo1 = partido.Equipo1.ifBlank { "Equipo 1" }
        val equipo2 = partido.Equipo2.ifBlank { "Equipo 2" }

        val fecha = formatDate(partido.FECHA_PARTIDO)
        val hora = formatTime(partido.HORA_PARTIDO)

        val ubicacion = buildLocation(
            estadio = partido.ESTADIO,
            lugar = partido.LUGAR
        )

        val marcador = buildMarcador(partido.GOLES1, partido.GOLES2)

        return buildString {
            appendLine("⚽ $equipo1 vs $equipo2")

            if (marcador.isNotBlank()) {
                appendLine("🔢 Marcador: $marcador")
            }

            if (fecha.isNotBlank()) appendLine("📅 $fecha")
            if (hora.isNotBlank()) appendLine("🕒 $hora")
            if (ubicacion.isNotBlank()) appendLine("📍 $ubicacion")

            // Etapa sí existe en tu modelo (Etapa: Int)
            val etapaTxt = getNombreEtapa(partido.Etapa)
            if (etapaTxt.isNotBlank()) {
                appendLine("🔁 Etapa: $etapaTxt")
            }

            // Hashtag final
            if (length > 0) appendLine()
            append("#ArkiDeportes")
        }.trim()
    }

    /**
     * Construye el mensaje para compartir una mención.
     */
    fun buildMentionShareMessage(
        title: String,
        description: String,
        link: String? = null,
        hashtags: List<String> = emptyList()
    ): String {
        val formattedHashtags = hashtags
            .mapNotNull { tag ->
                val cleaned = tag.trim()
                when {
                    cleaned.isEmpty() -> null
                    cleaned.startsWith("#") -> cleaned
                    else -> "#" + cleaned.replace("\\s+".toRegex(), "")
                }
            }
            .distinct()

        return buildString {
            if (title.isNotBlank()) appendLine(title.trim())
            if (description.isNotBlank()) appendLine(description.trim())
            if (!link.isNullOrBlank()) appendLine(link.trim())

            if (formattedHashtags.isNotEmpty()) {
                if (length > 0) appendLine()
                append(formattedHashtags.joinToString(separator = " "))
            }
        }.trim()
    }

    /**
     * Comparte el partido directamente por WhatsApp.
     */
    fun sharePartidoViaWhatsApp(
        context: Context,
        partido: Partido,
        chooserTitle: String? = null
    ) {
        val message = buildPartidoShareMessage(partido)
        shareText(
            context = context,
            message = message,
            chooserTitle = chooserTitle,
            packageName = WHATSAPP_PACKAGE,
            missingAppMessage = "Necesitas instalar WhatsApp para compartir el partido."
        )
    }

    /**
     * Comparte una mención utilizando WhatsApp.
     */
    fun shareMentionViaWhatsApp(
        context: Context,
        title: String,
        description: String,
        link: String? = null,
        hashtags: List<String> = emptyList(),
        chooserTitle: String? = null
    ) {
        val message = buildMentionShareMessage(title, description, link, hashtags)
        shareText(
            context = context,
            message = message,
            chooserTitle = chooserTitle,
            packageName = WHATSAPP_PACKAGE,
            missingAppMessage = "Necesitas instalar WhatsApp para compartir la mención."
        )
    }

    /**
     * Lanza un intent ACTION_SEND con el mensaje provisto.
     */
    fun shareText(
        context: Context,
        message: String,
        chooserTitle: String? = null,
        packageName: String? = null,
        missingAppMessage: String? = null
    ) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            packageName?.let { setPackage(it) }
        }

        val finalIntent = chooserTitle?.let { Intent.createChooser(sendIntent, it) } ?: sendIntent
        finalIntent.ensureNewTaskFlag(context)

        try {
            context.startActivity(finalIntent)
        } catch (error: ActivityNotFoundException) {
            if (packageName != null) {
                if (!missingAppMessage.isNullOrBlank()) {
                    Toast.makeText(context, missingAppMessage, Toast.LENGTH_LONG).show()
                }

                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                val chooser = Intent.createChooser(fallbackIntent, chooserTitle)
                chooser.ensureNewTaskFlag(context)
                context.startActivity(chooser)
            } else {
                throw error
            }
        }
    }

    private fun Intent.ensureNewTaskFlag(context: Context) {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun buildLocation(estadio: String?, lugar: String?): String {
        return listOf(estadio, lugar)
            .mapNotNull { it?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(separator = ", ")
    }

    private fun formatDate(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return ""
        return try {
            LocalDate.parse(value, inputDateFormatter).format(outputDateFormatter)
        } catch (_: Exception) {
            value
        }
    }

    private fun formatTime(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return ""
        return try {
            LocalTime.parse(value, inputTimeFormatter).format(outputTimeFormatter)
        } catch (_: Exception) {
            value
        }
    }

    /**
     * Si ya hay un marcador real (no ambos vacíos), lo muestra.
     */
    private fun buildMarcador(g1: String, g2: String): String {
        val a = g1.trim()
        val b = g2.trim()

        val ai = a.toIntOrNull()
        val bi = b.toIntOrNull()

        // Si ambos son null y están vacíos -> no mostrar
        if ((ai == null && a.isBlank()) && (bi == null && b.isBlank())) return ""

        // Normaliza: si no parsea, deja el string
        val left = ai?.toString() ?: a.ifBlank { "0" }
        val right = bi?.toString() ?: b.ifBlank { "0" }
        return "$left - $right"
    }

    /**
     * Etapa del campeonato según tu modelo:
     * 0 = Grupos / Fase regular
     * 1 = Cuartos
     * 2 = Semifinal
     * 3 = Final
     */
    private fun getNombreEtapa(etapa: Int): String {
        return when (etapa) {
            0 -> "" // normalmente no hace falta mostrar "Grupos"
            1 -> "Cuartos de final"
            2 -> "Semifinal"
            3 -> "Final"
            else -> ""
        }
    }
}
