package com.example.arki_deportes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.arki_deportes.ui.series.SerieFormScreen
import com.example.arki_deportes.ui.series.SerieListScreen


/**
 * ═══════════════════════════════════════════════════════════════════════════
 * APP NAVIGATOR - INTERFAZ DE NAVEGACIÓN COMPLETA
 * ═══════════════════════════════════════════════════════════════════════════
 */
interface AppNavigator {
    fun navigateTo(route: String, builder: NavOptionsBuilder.() -> Unit = {})
    fun navigateBack(): Boolean

    // PANTALLAS PRINCIPALES
    fun navigateToLogin(clearBackStack: Boolean = false)
    fun navigateToHybridHome(clearBackStack: Boolean = false)
    fun navigateToRealTime()
    fun navigateToTiempoReal(campeonatoId: String, partidoId: String, clearBackStack: Boolean = false)
    fun navigateToPartidosEnVivo()
    fun navigateToMonitorNarrador(campeonatoId: String, partidoId: String)
    fun navigateToCatalogs()

    // Listas de catálogos
    fun navigateToCampeonatoList()
    fun navigateToGrupoList()
    fun navigateToEquipoList()
    fun navigateToPartidoList()

    // FORMULARIOS CRUD
    fun navigateToCampeonatoForm(codigoCampeonato: String? = null)
    fun navigateToGrupoForm(codigoGrupo: String? = null)
    fun navigateToEquipoForm(codigoEquipo: String? = null)
    fun navigateToPartidoForm(codigoPartido: String? = null)

    // SERIES
    fun navigateToSerieList(campeonatoId: String)
    fun navigateToSerieForm(campeonatoId: String, serieId: String? = null)

    // GESTIÓN DE PRODUCCIÓN
    fun navigateToGestionAudio()
    fun navigateToGestionBanner()

    // OTRAS PANTALLAS
    fun navigateToMenciones()
    fun navigateToEquipoProduccion()
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
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    override fun navigateToHybridHome(clearBackStack: Boolean) {
        navController.navigate(AppDestinations.HYBRID_HOME) {
            launchSingleTop = true
            if (clearBackStack) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    override fun navigateToRealTime() {
        navController.navigate(AppDestinations.REAL_TIME) { launchSingleTop = true }
    }

    override fun navigateToTiempoReal(campeonatoId: String, partidoId: String, clearBackStack: Boolean) {
        val route = "tiempo_real/$campeonatoId/$partidoId"
        navController.navigate(route) {
            launchSingleTop = true
            if (clearBackStack) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    override fun navigateToPartidosEnVivo() {
        navController.navigate(AppDestinations.PARTIDOS_EN_VIVO) { launchSingleTop = true }
    }

    override fun navigateToMonitorNarrador(campeonatoId: String, partidoId: String) {
        val route = "monitor_narrador/$campeonatoId/$partidoId"
        navController.navigate(route) { launchSingleTop = true }
    }

    override fun navigateToCatalogs() {
        navController.navigate(AppDestinations.CATALOGS) { launchSingleTop = true }
    }

    override fun navigateToCampeonatoList() {
        navController.navigate(AppDestinations.CAMPEONATO_LIST) { launchSingleTop = true }
    }

    override fun navigateToGrupoList() {
        navController.navigate(AppDestinations.GRUPO_LIST) { launchSingleTop = true }
    }

    override fun navigateToEquipoList() {
        navController.navigate(AppDestinations.EQUIPO_LIST) { launchSingleTop = true }
    }

    override fun navigateToPartidoList() {
        navController.navigate(AppDestinations.PARTIDO_LIST) { launchSingleTop = true }
    }

    override fun navigateToCampeonatoForm(codigoCampeonato: String?) {
        val route = if (codigoCampeonato != null) "${AppDestinations.CAMPEONATO_FORM}/$codigoCampeonato" else AppDestinations.CAMPEONATO_FORM
        navController.navigate(route) { launchSingleTop = true }
    }

    override fun navigateToGrupoForm(codigoGrupo: String?) {
        val route = if (codigoGrupo != null) "${AppDestinations.GRUPO_FORM}/$codigoGrupo" else AppDestinations.GRUPO_FORM
        navController.navigate(route) { launchSingleTop = true }
    }

    override fun navigateToEquipoForm(codigoEquipo: String?) {
        val route = if (codigoEquipo != null) "${AppDestinations.EQUIPO_FORM}/$codigoEquipo" else AppDestinations.EQUIPO_FORM
        navController.navigate(route) { launchSingleTop = true }
    }

    override fun navigateToPartidoForm(codigoPartido: String?) {
        val route = if (codigoPartido != null) "${AppDestinations.PARTIDO_FORM}/$codigoPartido" else AppDestinations.PARTIDO_FORM
        navController.navigate(route) { launchSingleTop = true }
    }

    override fun navigateToSerieList(campeonatoId: String) {
        navController.navigate("serie_list/$campeonatoId") { launchSingleTop = true }
    }

    override fun navigateToSerieForm(campeonatoId: String, serieId: String?) {
        val route = if (serieId != null) "serie_form/$campeonatoId/$serieId" else "serie_form/$campeonatoId"
        navController.navigate(route) { launchSingleTop = true }
    }

    override fun navigateToGestionAudio() {
        navController.navigate(AppDestinations.GESTION_AUDIO) { launchSingleTop = true }
    }

    override fun navigateToGestionBanner() {
        navController.navigate(AppDestinations.GESTION_BANNER) { launchSingleTop = true }
    }

    override fun navigateToMenciones() {
        navController.navigate(AppDestinations.MENCIONES) { launchSingleTop = true }
    }

    override fun navigateToEquipoProduccion() {
        navController.navigate(AppDestinations.EQUIPO_PRODUCCION) { launchSingleTop = true }
    }

    override fun navigateToSettings() {
        navController.navigate(AppDestinations.SETTINGS) { launchSingleTop = true }
    }
}

@Composable
fun rememberAppNavigator(navController: NavHostController): AppNavigator {
    return remember(navController) { DefaultAppNavigator(navController) }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    navigator: AppNavigator,
    loginRoute: @Composable (AppNavigator) -> Unit,
    hybridHomeRoute: @Composable (AppNavigator) -> Unit,
    realTimeRoute: @Composable (AppNavigator, String, String) -> Unit,
    partidosEnVivoRoute: @Composable (AppNavigator) -> Unit,
    monitorNarradorRoute: @Composable (AppNavigator, String, String) -> Unit,
    catalogsRoute: @Composable (AppNavigator) -> Unit,
    mencionesRoute: @Composable (AppNavigator) -> Unit,
    equipoProduccionRoute: @Composable (AppNavigator) -> Unit,
    settingsRoute: @Composable (AppNavigator) -> Unit,
    campeonatoFormRoute: @Composable (AppNavigator, String?) -> Unit,
    campeonatoListRoute: @Composable (AppNavigator) -> Unit,
    grupoListRoute: @Composable (AppNavigator) -> Unit,
    equipoListRoute: @Composable (AppNavigator) -> Unit,
    partidoListRoute: @Composable (AppNavigator) -> Unit,
    grupoFormRoute: @Composable (AppNavigator, String?) -> Unit,
    equipoFormRoute: @Composable (AppNavigator, String?) -> Unit,
    partidoFormRoute: @Composable (AppNavigator, String?) -> Unit,
    gestionAudioRoute: @Composable (AppNavigator) -> Unit = {},
    gestionBannerRoute: @Composable (AppNavigator) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.START
    ) {
        composable(AppDestinations.LOGIN) { loginRoute(navigator) }
        composable(AppDestinations.HYBRID_HOME) { hybridHomeRoute(navigator) }

        composable(AppDestinations.PARTIDOS_EN_VIVO) {
            partidosEnVivoRoute(navigator)
        }

        composable(
            route = "tiempo_real/{campeonatoId}/{partidoId}",
            arguments = listOf(
                navArgument("campeonatoId") { type = NavType.StringType },
                navArgument("partidoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val campeonatoId = backStackEntry.arguments?.getString("campeonatoId").orEmpty()
            val partidoId = backStackEntry.arguments?.getString("partidoId").orEmpty()
            realTimeRoute(navigator, campeonatoId, partidoId)
        }

        composable(
            route = "monitor_narrador/{campeonatoId}/{partidoId}",
            arguments = listOf(
                navArgument("campeonatoId") { type = NavType.StringType },
                navArgument("partidoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cId = backStackEntry.arguments?.getString("campeonatoId").orEmpty()
            val pId = backStackEntry.arguments?.getString("partidoId").orEmpty()
            monitorNarradorRoute(navigator, cId, pId)
        }

        composable(AppDestinations.CATALOGS) { catalogsRoute(navigator) }
        composable(AppDestinations.MENCIONES) { mencionesRoute(navigator) }
        composable(AppDestinations.EQUIPO_PRODUCCION) { equipoProduccionRoute(navigator) }
        composable(AppDestinations.SETTINGS) { settingsRoute(navigator) }
        composable(AppDestinations.CAMPEONATO_LIST) { campeonatoListRoute(navigator) }
        composable(AppDestinations.GRUPO_LIST) { grupoListRoute(navigator) }
        composable(AppDestinations.EQUIPO_LIST) { equipoListRoute(navigator) }
        composable(AppDestinations.PARTIDO_LIST) { partidoListRoute(navigator) }
        
        // GESTIÓN DE PRODUCCIÓN
        composable(AppDestinations.GESTION_AUDIO) { gestionAudioRoute(navigator) }
        composable(AppDestinations.GESTION_BANNER) { gestionBannerRoute(navigator) }

        composable(AppDestinations.CAMPEONATO_FORM) { campeonatoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.CAMPEONATO_FORM}/{codigoCampeonato}",
            arguments = listOf(navArgument("codigoCampeonato") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoCampeonato")
            campeonatoFormRoute(navigator, codigo)
        }

        // RUTAS DE SERIES
        composable(
            route = "serie_list/{campeonatoId}",
            arguments = listOf(navArgument("campeonatoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campeonatoId = backStackEntry.arguments?.getString("campeonatoId") ?: ""
            SerieListScreen(
                campeonatoId = campeonatoId,
                onAddSerie = { navigator.navigateToSerieForm(campeonatoId) },
                onEditSerie = { serieId -> navigator.navigateToSerieForm(campeonatoId, serieId) },
                onManageGroups = { serieId ->
                    // Navegamos a la lista de grupos. 
                    // El ViewModel de grupos ya está suscrito al CampeonatoContext, 
                    // por lo que cargará los grupos de este campeonato.
                    navigator.navigateToGrupoList()
                },
                onBack = { navigator.navigateBack() }
            )
        }

        composable(
            route = "serie_form/{campeonatoId}",
            arguments = listOf(navArgument("campeonatoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campeonatoId = backStackEntry.arguments?.getString("campeonatoId") ?: ""
            SerieFormScreen(
                campeonatoId = campeonatoId,
                onBack = { navigator.navigateBack() }
            )
        }

        composable(
            route = "serie_form/{campeonatoId}/{serieId}",
            arguments = listOf(
                navArgument("campeonatoId") { type = NavType.StringType },
                navArgument("serieId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val campeonatoId = backStackEntry.arguments?.getString("campeonatoId") ?: ""
            val serieId = backStackEntry.arguments?.getString("serieId") ?: ""
            SerieFormScreen(
                campeonatoId = campeonatoId,
                serieId = serieId,
                onBack = { navigator.navigateBack() }
            )
        }

        // GRUPOS, EQUIPOS, PARTIDOS...
        composable(AppDestinations.GRUPO_FORM) { grupoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.GRUPO_FORM}/{codigoGrupo}",
            arguments = listOf(navArgument("codigoGrupo") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoGrupo")
            // CAMBIAR groupFormRoute por grupoFormRoute
            grupoFormRoute(navigator, codigo)
        }

        composable(AppDestinations.EQUIPO_FORM) { equipoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.EQUIPO_FORM}/{codigoEquipo}",
            arguments = listOf(navArgument("codigoEquipo") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoEquipo")
            equipoFormRoute(navigator, codigo)
        }

        composable(AppDestinations.PARTIDO_FORM) { partidoFormRoute(navigator, null) }
        composable(
            route = "${AppDestinations.PARTIDO_FORM}/{codigoPartido}",
            arguments = listOf(navArgument("codigoPartido") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoPartido")
            partidoFormRoute(navigator, codigo)
        }
    }
}
