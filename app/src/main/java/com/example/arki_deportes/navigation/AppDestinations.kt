package com.example.arki_deportes.navigation

/**
 * Define las rutas principales de navegación de la aplicación.
 */
object AppDestinations {
    /** Pantalla inicial para autenticación. */
    const val LOGIN: String = "login"

    /** Inicio híbrido con accesos generales. */
    const val HYBRID_HOME: String = "hybrid_home"

    /** Panel de datos en tiempo real. */
    const val REAL_TIME: String = "real_time"

    /** Sección de catálogos y recursos. */
    const val CATALOGS: String = "catalogs"

    /** Ruta inicial del grafo de navegación. */
    const val START: String = LOGIN

    /** Colección de rutas principales para componentes compartidos. */
    val mainDestinations: List<String> = listOf(HYBRID_HOME, REAL_TIME, CATALOGS)
}
