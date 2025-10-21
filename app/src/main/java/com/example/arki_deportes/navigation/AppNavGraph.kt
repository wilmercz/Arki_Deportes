package com.example.arki_deportes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Grafo principal de navegación de la aplicación.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AppDestinations.START,
    loginRoute: @Composable (AppNavigator) -> Unit,
    hybridHomeRoute: @Composable (AppNavigator) -> Unit,
    realTimeRoute: @Composable (AppNavigator) -> Unit,
    catalogsRoute: @Composable (AppNavigator) -> Unit
) {
    val navigator = rememberAppNavigator(navController)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppDestinations.LOGIN) {
            loginRoute(navigator)
        }
        composable(AppDestinations.HYBRID_HOME) {
            hybridHomeRoute(navigator)
        }
        composable(AppDestinations.REAL_TIME) {
            realTimeRoute(navigator)
        }
        composable(AppDestinations.CATALOGS) {
            catalogsRoute(navigator)
        }
    }
}

/**
 * Contrato que ViewModels pueden utilizar para solicitar navegación.
 */
interface AppNavigator {
    fun navigateTo(route: String, builder: NavOptionsBuilder.() -> Unit = {})
    fun navigateBack(): Boolean
    fun navigateToLogin(clearBackStack: Boolean = false)
    fun navigateToHybridHome(clearBackStack: Boolean = false)
    fun navigateToRealTime()
    fun navigateToCatalogs()
}

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
}

@Composable
fun rememberAppNavigator(navController: NavHostController): AppNavigator {
    return remember(navController) { DefaultAppNavigator(navController) }
}
