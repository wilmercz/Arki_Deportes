package com.example.arki_deportes.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * UTILIDADES DE FECHA - MANEJO CORRECTO DE ZONA HORARIA
 * ═══════════════════════════════════════════════════════════════════════════
 */

/**
 * Convierte milisegundos UTC a LocalDate de forma segura.
 *
 * El DatePicker de Material3 usa UTC medianoche (00:00:00 UTC), pero dependiendo
 * de la zona horaria local, esto puede resultar en el día anterior.
 *
 * Esta función añade 12 horas para garantizar que siempre estamos en el día correcto,
 * sin importar la zona horaria.
 *
 * Ejemplo:
 * - Millis: 1704067200000 (2024-01-01 00:00:00 UTC)
 * - Zona: America/Guayaquil (UTC-5)
 * - Sin ajuste: 2023-12-31
 * - Con ajuste: 2024-01-01 ✓
 */
fun millisToLocalDateSafe(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis)
        .plus(12, ChronoUnit.HOURS)  // Ajuste para evitar problemas de zona horaria
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

/**
 * Convierte un LocalDate a milisegundos en el inicio del día (00:00:00)
 * en la zona horaria local.
 *
 * IMPORTANTE: Esta función garantiza que al convertir de vuelta con millisToLocalDateSafe,
 * obtendremos el mismo día.
 *
 * @param date Fecha local a convertir
 * @return Milisegundos desde epoch representando la medianoche de esa fecha en zona local
 */
fun localDateToMillisAtStartOfDay(date: LocalDate): Long {
    val zonedDateTime = ZonedDateTime.of(
        date,
        LocalTime.MIDNIGHT,
        ZoneId.systemDefault()
    )
    return zonedDateTime.toInstant().toEpochMilli()
}

/**
 * Parsea un String en formato yyyy-MM-dd a LocalDate.
 *
 * @param dateString String de fecha en formato ISO (yyyy-MM-dd)
 * @return LocalDate si el parsing es exitoso, null en caso contrario
 */
fun parseLocalDate(dateString: String): LocalDate? {
    return try {
        if (dateString.isBlank()) return null
        LocalDate.parse(
            dateString.trim(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Formatea un LocalDate a String en formato yyyy-MM-dd.
 *
 * @param date Fecha a formatear
 * @return String en formato yyyy-MM-dd
 */
fun formatLocalDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
}

/**
 * Obtiene la fecha actual formateada como yyyy-MM-dd.
 *
 * @return String con la fecha actual
 */
fun currentDateString(): String {
    return formatLocalDate(LocalDate.now())
}
