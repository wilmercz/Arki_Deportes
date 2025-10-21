// app/src/main/java/com/example/arki_deportes/utils/Validations.kt

package com.example.arki_deportes.utils

import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.Grupo

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * VALIDATIONS.KT - FUNCIONES DE VALIDACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Contiene funciones de validación para los diferentes modelos de datos.
 * Cada función retorna null si la validación es exitosa, o un String con
 * el mensaje de error si falla.
 *
 * Uso:
 * ```kotlin
 * val error = validarCampeonato(campeonato)
 * if (error != null) {
 *     // Mostrar error
 * } else {
 *     // Guardar campeonato
 * }
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */

object Validations {

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDACIONES DE CAMPEONATO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Valida los datos de un campeonato
     *
     * @param campeonato Objeto Campeonato a validar
     * @return null si es válido, String con mensaje de error si no
     *
     * Ejemplo:
     * ```kotlin
     * val error = validarCampeonato(campeonato)
     * if (error != null) {
     *     Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
     * }
     * ```
     */
    fun validarCampeonato(campeonato: Campeonato): String? {
        return when {
            campeonato.CAMPEONATO.isBlank() ->
                "El nombre del campeonato es obligatorio"

            campeonato.CAMPEONATO.length < 3 ->
                "El nombre debe tener al menos 3 caracteres"

            campeonato.FECHAINICIO.isBlank() ->
                "La fecha de inicio es obligatoria"

            campeonato.FECHAFINAL.isBlank() ->
                "La fecha final es obligatoria"

            !esFechaValida(campeonato.FECHAINICIO) ->
                "La fecha de inicio no es válida"

            !esFechaValida(campeonato.FECHAFINAL) ->
                "La fecha final no es válida"

            compararFechas(campeonato.FECHAFINAL, campeonato.FECHAINICIO) < 0 ->
                "La fecha final debe ser posterior a la fecha de inicio"

            campeonato.PROVINCIA.isBlank() ->
                "La provincia es obligatoria"

            else -> null // Todo está bien
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDACIONES DE EQUIPO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Valida los datos de un equipo
     *
     * @param equipo Objeto Equipo a validar
     * @return null si es válido, String con mensaje de error si no
     *
     * Ejemplo:
     * ```kotlin
     * val error = validarEquipo(equipo)
     * if (error != null) {
     *     mostrarError(error)
     * }
     * ```
     */
    fun validarEquipo(equipo: Equipo): String? {
        return when {
            equipo.EQUIPO.isBlank() ->
                "El nombre del equipo es obligatorio"

            equipo.EQUIPO.length < 3 ->
                "El nombre debe tener al menos 3 caracteres"

            equipo.CODIGOCAMPEONATO.isBlank() ->
                "Debe seleccionar un campeonato"

            equipo.PROVINCIA.isBlank() ->
                "La provincia es obligatoria"

            else -> null // Todo está bien
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDACIONES DE PARTIDO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Valida los datos de un partido
     *
     * @param partido Objeto Partido a validar
     * @return null si es válido, String con mensaje de error si no
     *
     * Ejemplo:
     * ```kotlin
     * val error = validarPartido(partido)
     * if (error != null) {
     *     viewModel.mostrarError(error)
     * }
     * ```
     */
    fun validarPartido(partido: Partido): String? {
        return when {
            partido.CAMPEONATOCODIGO.isBlank() ->
                "Debe seleccionar un campeonato"

            partido.EQUIPO1.isBlank() ->
                "Debe seleccionar el equipo 1"

            partido.EQUIPO2.isBlank() ->
                "Debe seleccionar el equipo 2"

            partido.EQUIPO1 == partido.EQUIPO2 ->
                "Los equipos deben ser diferentes"

            partido.FECHA_PARTIDO.isBlank() ->
                "La fecha del partido es obligatoria"

            partido.HORA_PARTIDO.isBlank() ->
                "La hora del partido es obligatoria"

            !esFechaValida(partido.FECHA_PARTIDO) ->
                "La fecha del partido no es válida"

            !esHoraValida(partido.HORA_PARTIDO) ->
                "La hora del partido no es válida (formato: HH:mm)"

            partido.ESTADIO.isBlank() ->
                "El estadio/cancha es obligatorio"

            partido.GOLES1.isNotBlank() && !partido.GOLES1.esNumero() ->
                "Los goles del equipo 1 deben ser un número"

            partido.GOLES2.isNotBlank() && !partido.GOLES2.esNumero() ->
                "Los goles del equipo 2 deben ser un número"

            partido.ETAPA !in 0..3 ->
                "La etapa debe ser 0 (Ninguno), 1 (Cuartos), 2 (Semifinal) o 3 (Final)"

            else -> null // Todo está bien
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDACIONES DE GRUPO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Valida los datos de un grupo
     *
     * @param grupo Objeto Grupo a validar
     * @return null si es válido, String con mensaje de error si no
     */
    fun validarGrupo(grupo: Grupo): String? {
        return when {
            grupo.CODIGOCAMPEONATO.isBlank() ->
                "Debe seleccionar un campeonato"

            grupo.GRUPO.isBlank() ->
                "El nombre del grupo es obligatorio"

            grupo.GRUPO.length < 2 ->
                "El nombre del grupo debe tener al menos 2 caracteres"

            grupo.PROVINCIA.isBlank() ->
                "La provincia es obligatoria"

            else -> null // Todo está bien
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDACIONES DE CAMPOS ESPECÍFICOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Valida el nombre del nodo raíz de Firebase
     *
     * @param nodoRaiz Nombre del nodo a validar
     * @return null si es válido, String con mensaje de error si no
     *
     * Reglas:
     * - No puede estar vacío
     * - No puede contener: / . $ [ ] # espacios
     * - Longitud máxima: 50 caracteres
     */
    fun validarNodoRaiz(nodoRaiz: String): String? {
        return when {
            nodoRaiz.isBlank() ->
                "El nombre del nodo no puede estar vacío"

            nodoRaiz.contains("/") ->
                "El nombre no puede contener '/'"

            nodoRaiz.contains(".") ->
                "El nombre no puede contener '.'"

            nodoRaiz.contains("$") ->
                "El nombre no puede contener '$'"

            nodoRaiz.contains("[") || nodoRaiz.contains("]") ->
                "El nombre no puede contener corchetes"

            nodoRaiz.contains("#") ->
                "El nombre no puede contener '#'"

            nodoRaiz.contains(" ") ->
                "El nombre no puede contener espacios"

            nodoRaiz.length > 50 ->
                "El nombre es demasiado largo (máximo 50 caracteres)"

            else -> null // Todo está bien
        }
    }

    /**
     * Valida una contraseña
     *
     * @param password Contraseña a validar
     * @param minLength Longitud mínima (por defecto 6)
     * @return null si es válida, String con mensaje de error si no
     */
    fun validarPassword(password: String, minLength: Int = 6): String? {
        return when {
            password.isBlank() ->
                "La contraseña no puede estar vacía"

            password.length < minLength ->
                "La contraseña debe tener al menos $minLength caracteres"

            else -> null // Todo está bien
        }
    }

    /**
     * Valida que dos contraseñas coincidan
     *
     * @param password1 Primera contraseña
     * @param password2 Segunda contraseña
     * @return null si coinciden, String con mensaje de error si no
     */
    fun validarPasswordsCoinciden(password1: String, password2: String): String? {
        return if (password1 != password2) {
            "Las contraseñas no coinciden"
        } else {
            null
        }
    }

    /**
     * Valida campos obligatorios múltiples
     *
     * @param campos Mapa de nombre_campo -> valor
     * @return null si todos están completos, String con mensaje de error si no
     *
     * Ejemplo:
     * ```kotlin
     * val error = validarCamposObligatorios(
     *     "Nombre" to nombre,
     *     "Email" to email,
     *     "Teléfono" to telefono
     * )
     * ```
     */
    fun validarCamposObligatorios(vararg campos: Pair<String, String>): String? {
        campos.forEach { (nombre, valor) ->
            if (valor.isBlank()) {
                return "El campo '$nombre' es obligatorio"
            }
        }
        return null
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDACIONES AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Compara dos fechas en formato yyyy-MM-dd
     *
     * @param fecha1 Primera fecha
     * @param fecha2 Segunda fecha
     * @return -1 si fecha1 < fecha2, 0 si son iguales, 1 si fecha1 > fecha2
     */
    private fun compararFechas(fecha1: String, fecha2: String): Int {
        return try {
            val partes1 = fecha1.split("-")
            val partes2 = fecha2.split("-")

            val anio1 = partes1[0].toInt()
            val mes1 = partes1[1].toInt()
            val dia1 = partes1[2].toInt()

            val anio2 = partes2[0].toInt()
            val mes2 = partes2[1].toInt()
            val dia2 = partes2[2].toInt()

            when {
                anio1 != anio2 -> anio1.compareTo(anio2)
                mes1 != mes2 -> mes1.compareTo(mes2)
                else -> dia1.compareTo(dia2)
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Verifica si un String es un número válido
     *
     * @param valor String a verificar
     * @return true si es número, false si no
     */
    private fun esNumero(valor: String): Boolean {
        return valor.toIntOrNull() != null
    }

    /**
     * Valida si una fecha es válida en formato yyyy-MM-dd
     *
     * @param fecha Fecha a validar
     * @return true si es válida, false si no
     */
    private fun esFechaValida(fecha: String): Boolean {
        if (fecha.isBlank()) return false

        // Formato: yyyy-MM-dd
        val regex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
        if (!fecha.matches(regex)) return false

        return try {
            val partes = fecha.split("-")
            val anio = partes[0].toInt()
            val mes = partes[1].toInt()
            val dia = partes[2].toInt()

            // Validar rangos
            anio in 2020..2100 && mes in 1..12 && dia in 1..31
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida si una hora es válida en formato HH:mm
     *
     * @param hora Hora a validar
     * @return true si es válida, false si no
     */
    private fun esHoraValida(hora: String): Boolean {
        if (hora.isBlank()) return false

        // Formato: HH:mm
        val regex = "^\\d{2}:\\d{2}$".toRegex()
        if (!hora.matches(regex)) return false

        return try {
            val partes = hora.split(":")
            val horas = partes[0].toInt()
            val minutos = partes[1].toInt()

            // Validar rangos
            horas in 0..23 && minutos in 0..59
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida el tamaño de un archivo en bytes
     *
     * @param sizeInBytes Tamaño del archivo en bytes
     * @param maxSizeKB Tamaño máximo permitido en KB
     * @return null si es válido, String con mensaje de error si no
     */
    fun validarTamanoArchivo(sizeInBytes: Long, maxSizeKB: Int = Constants.MAX_ESCUDO_SIZE_KB): String? {
        val sizeInKB = sizeInBytes / 1024
        return if (sizeInKB > maxSizeKB) {
            "El archivo es demasiado grande. Máximo permitido: ${maxSizeKB}KB"
        } else {
            null
        }
    }

    /**
     * Valida el formato de un archivo de imagen
     *
     * @param fileName Nombre del archivo
     * @return null si es válido, String con mensaje de error si no
     */
    fun validarFormatoImagen(fileName: String): String? {
        val extension = fileName.substringAfterLast(".", "")
        val formatosPermitidos = listOf("jpg", "jpeg", "png", "webp")

        return if (extension.lowercase() !in formatosPermitidos) {
            "Formato no válido. Formatos permitidos: ${formatosPermitidos.joinToString(", ")}"
        } else {
            null
        }
    }
}