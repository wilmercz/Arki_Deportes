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
    private val inputTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val outputTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(displayLocale)

    /**
     * Construye el mensaje para compartir un partido.
     */
    fun buildPartidoShareMessage(partido: Partido): String {
        val equipos = listOf(
            partido.EQUIPO1.ifBlank { "Equipo 1" },
            partido.EQUIPO2.ifBlank { "Equipo 2" }
        )

        val fecha = formatDate(partido.FECHA_PARTIDO)
        val hora = formatTime(partido.HORA_PARTIDO)
        val ubicacion = buildLocation(
            estadio = partido.ESTADIO,
            lugar = partido.LUGAR,
            provincia = partido.PROVINCIA
        )

        return buildString {
            appendLine("⚽ ${equipos[0]} vs ${equipos[1]}")

            if (fecha.isNotBlank()) {
                appendLine("📅 $fecha")
            }
            if (hora.isNotBlank()) {
                appendLine("🕒 $hora")
            }
            if (ubicacion.isNotBlank()) {
                appendLine("📍 $ubicacion")
            }
            if (partido.CAMPEONATOTXT.isNotBlank()) {
                appendLine("🏆 ${partido.CAMPEONATOTXT}")
            }
            if (partido.ETAPA != 0) {
                appendLine("🔁 Etapa: ${partido.getNombreEtapa()}")
            }
            if (partido.TRANSMISION) {
                appendLine("🎥 Transmisión en vivo")
            }

            val marcador = partido.getMarcador()
            if (marcador != "vs") {
                appendLine("🔢 Marcador: $marcador")
            }

            if (partido.TEXTOFACEBOOK.isNotBlank()) {
                appendLine()
                appendLine(partido.TEXTOFACEBOOK.trim())
            }

            if (length > 0) {
                appendLine()
            }
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
            if (title.isNotBlank()) {
                appendLine(title.trim())
            }
            if (description.isNotBlank()) {
                appendLine(description.trim())
            }
            if (!link.isNullOrBlank()) {
                appendLine(link.trim())
            }
            if (formattedHashtags.isNotEmpty()) {
                if (length > 0) {
                    appendLine()
                }
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

    private fun buildLocation(estadio: String, lugar: String, provincia: String): String {
        return listOf(estadio, lugar, provincia)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(separator = ", ")
    }

    private fun formatDate(raw: String): String {
        return try {
            if (raw.isBlank()) {
                ""
            } else {
                LocalDate.parse(raw.trim(), inputDateFormatter).format(outputDateFormatter)
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun formatTime(raw: String): String {
        return try {
            if (raw.isBlank()) {
                ""
            } else {
                LocalTime.parse(raw.trim(), inputTimeFormatter).format(outputTimeFormatter)
            }
        } catch (_: Exception) {
            raw
        }
    }
}
