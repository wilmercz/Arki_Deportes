package com.example.arki_deportes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
/**
 * ═══════════════════════════════════════════════════════════════════════════
 * APP NAVIGATOR - INTERFAZ DE NAVEGACIÓN COMPLETA
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Interfaz que proporciona métodos de navegación para toda la aplicación.
 * Abstrae la navegación del NavController para facilitar su uso en ViewModels
 * y Composables.
 */


interface AppNavigator {
    /**
     * Navega a una ruta específica con opciones personalizadas
     */
    fun navigateTo(route: String, builder: NavOptionsBuilder.() -> Unit = {})

    /**
     * Navega hacia atrás en la pila de navegación
     * @return true si se pudo navegar hacia atrás, false si no
     */
    fun navigateBack(): Boolean

    // ═══════════════════════════════════════════════════════════════════════
    // PANTALLAS PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Navega a la pantalla de login
     * @param clearBackStack Si es true, limpia toda la pila de navegación
     */
    fun navigateToLogin(clearBackStack: Boolean = false)

    /**
     * Navega a la pantalla principal (Home híbrida)
     * @param clearBackStack Si es true, limpia toda la pila de navegación
     */
    fun navigateToHybridHome(clearBackStack: Boolean = false)

    /**
     * Navega a la pantalla de tiempo real
     */
    fun navigateToRealTime()

    /**
     * Navega a la pantalla de catálogos
     */
    fun navigateToCatalogs()

    // ═══════════════════════════════════════════════════════════════════════
    // FORMULARIOS CRUD
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Navega al formulario de campeonatos
     * @param codigoCampeonato Código del campeonato a editar, o null para crear nuevo
     */
    fun navigateToCampeonatoForm(codigoCampeonato: String? = null)

    /**
     * Navega al formulario de grupos
     * @param codigoGrupo Código del grupo a editar, o null para crear nuevo
     */
    fun navigateToGrupoForm(codigoGrupo: String? = null)

    /**
     * Navega al formulario de equipos
     * @param codigoEquipo Código del equipo a editar, o null para crear nuevo
     */
    fun navigateToEquipoForm(codigoEquipo: String? = null)

    /**
     * Navega al formulario de partidos
     * @param codigoPartido Código del partido a editar, o null para crear nuevo
     */
    fun navigateToPartidoForm(codigoPartido: String? = null)

    // ═══════════════════════════════════════════════════════════════════════
    // OTRAS PANTALLAS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Navega a la pantalla de menciones
     */
    fun navigateToMenciones()

    /**
     * Navega a la pantalla de equipo de producción
     */
    fun navigateToEquipoProduccion()

    /**
     * Navega a la pantalla de ajustes/configuración
     */
    fun navigateToSettings()
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * IMPLEMENTACIÓN POR DEFECTO DE APP NAVIGATOR
 * ═══════════════════════════════════════════════════════════════════════════
 */
private class DefaultAppNavigator(
    private val navController: NavHostController
) : AppNavigator {

    override fun navigateTo(route: String, builder: NavOptionsBuilder.() -> Unit) {
        navController.navigate(route, builder)
    }

    override fun navigateBack(): Boolean = navController.navigateUp()

    override fun navigateToLogin(clearBackStack: Boolean) {
        navController.navigate(AppDestinations.LOGIN) {
            launchSingleTop = true
            if (clearBackStack) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    override fun navigateToHybridHome(clearBackStack: Boolean) {
        navController.navigate(AppDestinations.HYBRID_HOME) {
            launchSingleTop = true
            if (clearBackStack) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    override fun navigateToRealTime() {
        navController.navigate(AppDestinations.REAL_TIME) {
            launchSingleTop = true
        }
    }

    override fun navigateToCatalogs() {
        navController.navigate(AppDestinations.CATALOGS) {
            launchSingleTop = true
        }
    }

    override fun navigateToCampeonatoForm(codigoCampeonato: String?) {
        val route = if (codigoCampeonato != null) {
            "${AppDestinations.CAMPEONATO_FORM}/$codigoCampeonato"
        } else {
            AppDestinations.CAMPEONATO_FORM
        }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    override fun navigateToGrupoForm(codigoGrupo: String?) {
        val route = if (codigoGrupo != null) {
            "${AppDestinations.GRUPO_FORM}/$codigoGrupo"
        } else {
            AppDestinations.GRUPO_FORM
        }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    override fun navigateToEquipoForm(codigoEquipo: String?) {
        val route = if (codigoEquipo != null) {
            "${AppDestinations.EQUIPO_FORM}/$codigoEquipo"
        } else {
            AppDestinations.EQUIPO_FORM
        }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    override fun navigateToPartidoForm(codigoPartido: String?) {
        val route = if (codigoPartido != null) {
            "${AppDestinations.PARTIDO_FORM}/$codigoPartido"
        } else {
            AppDestinations.PARTIDO_FORM
        }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    override fun navigateToMenciones() {
        navController.navigate(AppDestinations.MENCIONES) {
            launchSingleTop = true
        }
    }

    override fun navigateToEquipoProduccion() {
        navController.navigate(AppDestinations.EQUIPO_PRODUCCION) {
            launchSingleTop = true
        }
    }

    override fun navigateToSettings() {
        navController.navigate(AppDestinations.SETTINGS) {
            launchSingleTop = true
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * COMPOSABLE PARA CREAR Y RECORDAR EL NAVIGATOR
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
fun rememberAppNavigator(navController: NavHostController): AppNavigator {
    return remember(navController) { DefaultAppNavigator(navController) }
}

/**
 * Grafo de navegación principal de la app.
 * Recibe lambdas para renderizar cada ruta con acceso al AppNavigator.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    navigator: AppNavigator,
    loginRoute: @Composable (AppNavigator) -> Unit,
    hybridHomeRoute: @Composable (AppNavigator) -> Unit,
    realTimeRoute: @Composable (AppNavigator) -> Unit,
    catalogsRoute: @Composable (AppNavigator) -> Unit,
    mencionesRoute: @Composable (AppNavigator) -> Unit,
    equipoProduccionRoute: @Composable (AppNavigator) -> Unit,
    settingsRoute: @Composable (AppNavigator) -> Unit,
    campeonatoFormRoute: @Composable (AppNavigator, String?) -> Unit,
    grupoFormRoute: @Composable (AppNavigator, String?) -> Unit,
    equipoFormRoute: @Composable (AppNavigator, String?) -> Unit,
    partidoFormRoute: @Composable (AppNavigator, String?) -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = AppDestinations.START
    ) {
        composable(AppDestinations.LOGIN) { loginRoute(navigator) }
        composable(AppDestinations.HYBRID_HOME) { hybridHomeRoute(navigator) }
        composable(AppDestinations.REAL_TIME) { realTimeRoute(navigator) }
        composable(AppDestinations.CATALOGS) { catalogsRoute(navigator) }
        composable(AppDestinations.MENCIONES) { mencionesRoute(navigator) }
        composable(AppDestinations.EQUIPO_PRODUCCION) { equipoProduccionRoute(navigator) }
        composable(AppDestinations.SETTINGS) { settingsRoute(navigator) }
        composable(AppDestinations.CAMPEONATO_FORM) { campeonatoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.CAMPEONATO_FORM}/{codigoCampeonato}",
            arguments = listOf(navArgument("codigoCampeonato") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoCampeonato")
            campeonatoFormRoute(navigator, codigo)
        }
        composable(AppDestinations.GRUPO_FORM) { grupoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.GRUPO_FORM}/{codigoGrupo}",
            arguments = listOf(navArgument("codigoGrupo") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoGrupo")
            grupoFormRoute(navigator, codigo)
        }
        composable(AppDestinations.EQUIPO_FORM) { equipoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.EQUIPO_FORM}/{codigoEquipo}",
            arguments = listOf(navArgument("codigoEquipo") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoEquipo")
            equipoFormRoute(navigator, codigo)
        }
        composable(AppDestinations.PARTIDO_FORM) { partidoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.PARTIDO_FORM}/{codigoPartido}",
            arguments = listOf(navArgument("codigoPartido") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoPartido")
            partidoFormRoute(navigator, codigo)
        }
    }
}
