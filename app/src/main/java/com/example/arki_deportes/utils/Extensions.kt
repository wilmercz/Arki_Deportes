// app/src/main/java/com/example/arki_deportes/utils/Extensions.kt

package com.example.arki_deportes.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EXTENSIONS.KT - FUNCIONES DE EXTENSIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Contiene funciones de extensión útiles para diferentes tipos de datos.
 * Las extensiones facilitan operaciones comunes en la aplicación.
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */

// ═══════════════════════════════════════════════════════════════════════════
// EXTENSIONES DE STRING
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Convierte un String a mayúsculas y elimina espacios al inicio y final
 *
 * Ejemplo:
 * ```kotlin
 * val texto = "  barcelona  "
 * val resultado = texto.toUpperTrim() // "BARCELONA"
 * ```
 */
fun String.toUpperTrim(): String {
    return this.trim().uppercase()
}

/**
 * Genera un código único basado en el texto y un timestamp
 *
 * Ejemplo:
 * ```kotlin
 * val codigo = "Barcelona".generarCodigo() // "BARCELONA_1705334400000"
 * ```
 */
fun String.generarCodigo(): String {
    val timestamp = System.currentTimeMillis()
    val textoLimpio = this.replace(" ", "_").uppercase()
    return "${textoLimpio}_$timestamp"
}

/**
 * Verifica si el String es un número válido
 *
 * Ejemplo:
 * ```kotlin
 * "123".esNumero() // true
 * "abc".esNumero() // false
 * ```
 */
fun String.esNumero(): Boolean {
    return this.toIntOrNull() != null
}

/**
 * Capitaliza la primera letra de cada palabra
 *
 * Ejemplo:
 * ```kotlin
 * "hola mundo".capitalize() // "Hola Mundo"
 * ```
 */
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}

/**
 * Verifica si el String es una fecha válida en formato dd/MM/yyyy
 *
 * Ejemplo:
 * ```kotlin
 * "20/01/2025".esFechaValida() // true
 * "32/13/2025".esFechaValida() // false
 * ```
 */
fun String.esFechaValida(): Boolean {
    return try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        format.isLenient = false
        format.parse(this)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Limita la longitud del String y agrega "..." si es necesario
 *
 * Ejemplo:
 * ```kotlin
 * "Texto muy largo".truncate(10) // "Texto muy..."
 * ```
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        "${this.substring(0, maxLength)}..."
    } else {
        this
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// EXTENSIONES DE CONTEXT
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Muestra un Toast corto
 *
 * Ejemplo:
 * ```kotlin
 * context.showToast("Guardado exitosamente")
 * ```
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Muestra un Toast largo
 *
 * Ejemplo:
 * ```kotlin
 * context.showLongToast("Error al guardar los datos")
 * ```
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ═══════════════════════════════════════════════════════════════════════════
// EXTENSIONES DE DATE Y TIMESTAMP
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Convierte un timestamp (Long) a fecha formateada
 *
 * @param format Formato de fecha (por defecto: dd/MM/yyyy HH:mm:ss)
 * @return String con la fecha formateada
 *
 * Ejemplo:
 * ```kotlin
 * val timestamp = 1705334400000L
 * val fecha = timestamp.toFormattedDate() // "15/01/2025 14:00:00"
 * ```
 */
fun Long.toFormattedDate(format: String = Constants.DATETIME_FORMAT): String {
    return try {
        val date = Date(this)
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        "Fecha inválida"
    }
}

/**
 * Convierte un timestamp a formato de tiempo relativo
 *
 * Ejemplo:
 * ```kotlin
 * timestamp.toRelativeTime() // "Hace 5 minutos"
 * ```
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Hace $seconds segundos"
        minutes < 60 -> "Hace $minutes minutos"
        hours < 24 -> "Hace $hours horas"
        days < 7 -> "Hace $days días"
        else -> this.toFormattedDate(Constants.DATE_FORMAT)
    }
}

/**
 * Convierte String de fecha a timestamp
 *
 * @param format Formato de la fecha (por defecto: dd/MM/yyyy)
 * @return Timestamp en milisegundos o 0 si hay error
 *
 * Ejemplo:
 * ```kotlin
 * val fecha = "20/01/2025"
 * val timestamp = fecha.toTimestamp() // 1705766400000
 * ```
 */
fun String.toTimestamp(format: String = Constants.DATE_FORMAT): Long {
    return try {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        val date = formatter.parse(this)
        date?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

/**
 * Convierte fecha de formato yyyy-MM-dd a dd/MM/yyyy
 *
 * Ejemplo:
 * ```kotlin
 * "2025-01-20".formatearFecha() // "20/01/2025"
 * ```
 */
fun String.formatearFecha(): String {
    return try {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = formatoEntrada.parse(this)
        formatoSalida.format(fecha ?: Date())
    } catch (e: Exception) {
        this
    }
}

/**
 * Convierte fecha de formato dd/MM/yyyy a yyyy-MM-dd
 *
 * Ejemplo:
 * ```kotlin
 * "20/01/2025".toFirebaseDate() // "2025-01-20"
 * ```
 */
fun String.toFirebaseDate(): String {
    return try {
        val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = formatoEntrada.parse(this)
        formatoSalida.format(fecha ?: Date())
    } catch (e: Exception) {
        this
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// EXTENSIONES DE INT
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Verifica si un número está en un rango
 *
 * Ejemplo:
 * ```kotlin
 * 5.inRange(1, 10) // true
 * 15.inRange(1, 10) // false
 * ```
 */
fun Int.inRange(min: Int, max: Int): Boolean {
    return this in min..max
}

/**
 * Convierte minutos a formato HH:mm
 *
 * Ejemplo:
 * ```kotlin
 * 90.minutosToTime() // "01:30"
 * 45.minutosToTime() // "00:45"
 * ```
 */
fun Int.minutosToTime(): String {
    val hours = this / 60
    val minutes = this % 60
    return String.format("%02d:%02d", hours, minutes)
}

// ═══════════════════════════════════════════════════════════════════════════
// EXTENSIONES PARA VALIDACIONES
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Valida si un String es un email válido
 *
 * Ejemplo:
 * ```kotlin
 * "test@example.com".isValidEmail() // true
 * "test@".isValidEmail() // false
 * ```
 */
fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return this.matches(emailRegex)
}

/**
 * Valida si un String es un teléfono válido (Ecuador)
 *
 * Ejemplo:
 * ```kotlin
 * "0991234567".isValidPhone() // true
 * "+593991234567".isValidPhone() // true
 * ```
 */
fun String.isValidPhone(): Boolean {
    // Acepta formatos: 0991234567 o +593991234567
    val phoneRegex = "^(\\+593|0)[0-9]{9}$".toRegex()
    return this.matches(phoneRegex)
}

/**
 * Valida si un String tiene la longitud mínima requerida
 *
 * Ejemplo:
 * ```kotlin
 * "Hola".hasMinLength(3) // true
 * "Hi".hasMinLength(5) // false
 * ```
 */
fun String.hasMinLength(minLength: Int): Boolean {
    return this.length >= minLength
}

// ═══════════════════════════════════════════════════════════════════════════
// EXTENSIONES PARA LISTAS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Obtiene un elemento de la lista de forma segura (devuelve null si está fuera de rango)
 *
 * Ejemplo:
 * ```kotlin
 * val lista = listOf(1, 2, 3)
 * lista.getOrNull(5) // null
 * lista.getOrNull(1) // 2
 * ```
 */
fun <T> List<T>.getSafe(index: Int): T? {
    return if (index in 0 until this.size) this[index] else null
}

/**
 * Filtra una lista removiendo elementos null
 *
 * Ejemplo:
 * ```kotlin
 * val lista = listOf(1, null, 3, null, 5)
 * lista.filterNotNull() // [1, 3, 5]
 * ```
 */
fun <T> List<T?>.removeNulls(): List<T> {
    return this.filterNotNull()
}

// ═══════════════════════════════════════════════════════════════════════════
// EXTENSIONES PARA FECHAS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Obtiene la fecha actual en formato yyyy-MM-dd
 *
 * Ejemplo:
 * ```kotlin
 * getFechaActual() // "2025-01-20"
 * ```
 */
fun getFechaActual(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

/**
 * Obtiene la hora actual en formato HH:mm
 *
 * Ejemplo:
 * ```kotlin
 * getHoraActual() // "14:30"
 * ```
 */
fun getHoraActual(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

/**
 * Obtiene el año actual
 *
 * Ejemplo:
 * ```kotlin
 * getAnioActual() // 2025
 * ```
 */
fun getAnioActual(): Int {
    return Calendar.getInstance().get(Calendar.YEAR)
}

/**
 * Compara dos fechas en formato yyyy-MM-dd
 *
 * @return -1 si fecha1 < fecha2, 0 si son iguales, 1 si fecha1 > fecha2
 *
 * Ejemplo:
 * ```kotlin
 * compararFechas("2025-01-20", "2025-01-15") // 1
 * ```
 */
fun compararFechas(fecha1: String, fecha2: String): Int {
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date1 = formato.parse(fecha1)
        val date2 = formato.parse(fecha2)
        date1?.compareTo(date2 ?: Date()) ?: 0
    } catch (e: Exception) {
        0
    }
}