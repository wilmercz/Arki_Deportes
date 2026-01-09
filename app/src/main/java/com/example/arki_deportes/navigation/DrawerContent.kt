// app/src/main/java/com/example/arki_deportes/navigation/DrawerContent.kt

package com.example.arki_deportes.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.arki_deportes.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.example.arki_deportes.ui.components.CampeonatoSelector

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.arki_deportes.data.context.UsuarioContext
import com.example.arki_deportes.data.context.PartidoContext
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.foundation.shape.RoundedCornerShape

private fun String?.matchesRoute(route: String): Boolean {
    return this == route || this?.startsWith("$route/") == true
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DRAWER CONTENT - CONTENIDO DEL MENÚ LATERAL
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
fun DrawerContent(
    navigator: AppNavigator,
    onCloseDrawer: () -> Unit,
    currentRoute: String? = null,
    onLogout: () -> Unit
) {
    val drawerVM: DrawerViewModel = viewModel()
    val campeonatos by drawerVM.campeonatos.collectAsState()


    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 0.dp
    ) {
        // Header
        DrawerHeader()

        Divider()

        // ═══════════════════════════════════════════════════════════════════
        // SELECTOR DE CAMPEONATO - ¡NUEVO!
        // ═══════════════════════════════════════════════════════════════════
        CampeonatoSelector(
            campeonatos = campeonatos,   // ✅ AHORA sí usa la lista en tiempo real
            modifier = Modifier.padding(top = 8.dp)
        )

        // Si es corresponsal con partido, mostrar opción destacada
        if (UsuarioContext.esCorresponsal()) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null) },
                label = { Text("Mi Partido", fontWeight = FontWeight.Bold) },
                selected = currentRoute == AppDestinations.TiempoReal.route,
                onClick = {
                    onCloseDrawer()
                    navigator.navigateToTiempoReal(clearBackStack = false)
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            HorizontalDivider()
        }

// Opción para cambiar de partido (solo corresponsales)
        if (UsuarioContext.esCorresponsal()) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                label = { Text("Cambiar Partido") },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    navigator.navigateToPartidoSeleccion(clearBackStack = false)
                }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Menú principal
        DrawerMenuItem(
            icon = Icons.Default.Home,
            label = "Inicio",
            isSelected = currentRoute.matchesRoute(AppDestinations.HYBRID_HOME),
            onClick = {
                navigator.navigateToHybridHome()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            icon = Icons.Default.VideoLibrary,
            label = "Tiempo Real",
            isSelected = currentRoute.matchesRoute(AppDestinations.REAL_TIME),
            onClick = {
                navigator.navigateToRealTime()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            icon = Icons.Default.Category,
            label = "Catálogos",
            isSelected = currentRoute.matchesRoute(AppDestinations.CATALOGS),
            onClick = {
                navigator.navigateToCatalogs()
                onCloseDrawer()
            }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // SECCIÓN DE GESTIÓN
        Text(
            text = "GESTIÓN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DrawerMenuItem(
            icon = Icons.Default.EmojiEvents,
            label = "Campeonatos",
            isSelected = currentRoute.matchesRoute(AppDestinations.CAMPEONATO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.CAMPEONATO_FORM),
            onClick = {
                navigator.navigateToCampeonatoList()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Shield,
            label = "Equipos",
            isSelected = currentRoute.matchesRoute(AppDestinations.EQUIPO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.EQUIPO_FORM),
            onClick = {
                navigator.navigateToEquipoList()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Group,
            label = "Grupos",
            isSelected = currentRoute.matchesRoute(AppDestinations.GRUPO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.GRUPO_FORM),
            onClick = {
                navigator.navigateToGrupoList()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.SportsScore,
            label = "Partidos",
            isSelected = currentRoute.matchesRoute(AppDestinations.PARTIDO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.PARTIDO_FORM),
            onClick = {
                navigator.navigateToPartidoList()
                onCloseDrawer()
            }
        )


        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "OTRAS OPCIONES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DrawerMenuItem(
            icon = Icons.Default.Mic,
            label = "Menciones",
            isSelected = currentRoute.matchesRoute(AppDestinations.MENCIONES),
            onClick = {
                navigator.navigateToMenciones()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            icon = Icons.Default.People,
            label = "Equipo Producción",
            isSelected = currentRoute.matchesRoute(AppDestinations.EQUIPO_PRODUCCION),
            onClick = {
                navigator.navigateToEquipoProduccion()
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        DrawerMenuItem(
            icon = Icons.Default.Settings,
            label = "Configuración",
            isSelected = currentRoute.matchesRoute(AppDestinations.SETTINGS),
            onClick = {
                navigator.navigateToSettings()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            icon = Icons.Default.ExitToApp,
            label = "Cerrar Sesión",
            onClick = {
                onLogout()
                navigator.navigateToLogin(clearBackStack = true)
                onCloseDrawer()
            }
        )
    }
}

/**
 * Header del Drawer con información de la app
 */
@Composable
fun DrawerHeader() {
    // ✅ Obtener información del usuario
    val nombreCompleto = UsuarioContext.getNombreCompleto()
    val rol = UsuarioContext.getDescripcionRol()
    val partidoAsignado = UsuarioContext.getPartidoAsignado()
    val esCorresponsal = UsuarioContext.esCorresponsal()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        // Nombre del usuario
        Text(
            text = nombreCompleto,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // Rol
        Text(
            text = rol,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )

        // Si es corresponsal, mostrar partido asignado
        if (esCorresponsal && partidoAsignado != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "🎯 Partido Asignado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    val partidoNombre = PartidoContext.getNombrePartido() ?: partidoAsignado
                    Text(
                        text = partidoNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Item individual del menú
 */
@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor
        )
    }
}