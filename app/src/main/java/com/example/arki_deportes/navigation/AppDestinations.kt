package com.example.arki_deportes.navigation

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * APP DESTINATIONS - RUTAS DE NAVEGACIÓN ACTUALIZADAS
 * ═══════════════════════════════════════════════════════════════════════════
 */
object AppDestinations {
    // Autenticación
    const val LOGIN: String = "login"

    // Pantallas principales
    const val HYBRID_HOME: String = "hybrid_home"
    const val REAL_TIME: String = "real_time"
    const val CATALOGS: String = "catalogs"

    // Formularios CRUD
    const val CAMPEONATO_FORM: String = "campeonato_form"
    const val GRUPO_FORM: String = "grupo_form"
    const val EQUIPO_FORM: String = "equipo_form"
    const val PARTIDO_FORM: String = "partido_form"

    // Otras pantallas
    const val MENCIONES: String = "menciones"
    const val EQUIPO_PRODUCCION: String = "equipo_produccion"
    const val SETTINGS: String = "settings"

    // Ruta inicial
    const val START: String = LOGIN

    // Rutas principales para drawer
    val mainDestinations: List<String> = listOf(
        HYBRID_HOME,
        REAL_TIME,
        CATALOGS,
        MENCIONES,
        EQUIPO_PRODUCCION,
        SETTINGS
    )

    // Argumentos de navegación con parámetros opcionales
    fun campeonatoFormRoute(codigoCampeonato: String? = null): String {
        return if (codigoCampeonato != null) {
            "$CAMPEONATO_FORM/$codigoCampeonato"
        } else {
            CAMPEONATO_FORM
        }
    }

    fun grupoFormRoute(codigoGrupo: String? = null): String {
        return if (codigoGrupo != null) {
            "$GRUPO_FORM/$codigoGrupo"
        } else {
            GRUPO_FORM
        }
    }

    fun equipoFormRoute(codigoEquipo: String? = null): String {
        return if (codigoEquipo != null) {
            "$EQUIPO_FORM/$codigoEquipo"
        } else {
            EQUIPO_FORM
        }
    }

    fun partidoFormRoute(codigoPartido: String? = null): String {
        return if (codigoPartido != null) {
            "$PARTIDO_FORM/$codigoPartido"
        } else {
            PARTIDO_FORM
        }
    }
}