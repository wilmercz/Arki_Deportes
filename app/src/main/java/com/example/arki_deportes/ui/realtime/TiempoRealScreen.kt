// ui/realtime/TiempoRealScreen.kt

package com.example.arki_deportes.ui.realtime

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.ui.realtime.components.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiempoRealScreen(
    viewModel: TiempoRealViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Control de Partido") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.partido?.let { partido ->
                // ═══════════════════════════════════════════════════════
                // PANEL 1: CRONÓMETRO (Compacto con Tabs)
                // ═══════════════════════════════════════════════════════
                CronometroPanel(
                    tiempoActual = state.tiempoActual,
                    partido = partido,
                    onIniciar = viewModel::iniciarPartido,
                    onDetener = viewModel::detenerCronometro,
                    onReiniciar = viewModel::reiniciarPartido,
                    onAjustar = viewModel::ajustarTiempo,
                    modifier = Modifier.fillMaxWidth()
                )

                // ═══════════════════════════════════════════════════════
                // PANEL 2: MARCADOR (Siempre visible, minimalista)
                // ═══════════════════════════════════════════════════════
                MarcadorPanel(
                    equipo1 = partido.EQUIPO1,
                    equipo2 = partido.EQUIPO2,
                    goles1 = partido.getGoles1Int(),
                    goles2 = partido.getGoles2Int(),
                    modifier = Modifier.fillMaxWidth()
                )

                // ═══════════════════════════════════════════════════════
                // PANEL 3: TABS COMBINADAS (Resto del espacio)
                // ═══════════════════════════════════════════════════════
                var selectedTab by remember { mutableStateOf(0) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("⚽ Controles") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("🎯 Penales") }  // ← NUEVO
                        )

                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("ℹ️ Info") }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("🎵 Audio") }
                        )
                        Tab(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            text = { Text("📢 Pub") }
                        )
                    }

                    when (selectedTab) {
                        0 -> ControlPartidoTab(
                            equipo1 = partido.EQUIPO1,
                            equipo2 = partido.EQUIPO2,
                            goles1 = partido.getGoles1Int(),
                            goles2 = partido.getGoles2Int(),
                            amarillas1 = partido.getAmarillas1Int(),
                            amarillas2 = partido.getAmarillas2Int(),
                            rojas1 = partido.getRojas1Int(),
                            rojas2 = partido.getRojas2Int(),
                            esquinas1 = partido.getEsquinas1Int(),
                            esquinas2 = partido.getEsquinas2Int(),
                            onAgregarGol1 = viewModel::agregarGolEquipo1,
                            onRestarGol1 = viewModel::restarGolEquipo1,
                            onAgregarGol2 = viewModel::agregarGolEquipo2,
                            onRestarGol2 = viewModel::restarGolEquipo2,
                            onAgregarAmarilla1 = viewModel::agregarAmarillaEquipo1,
                            onRestarAmarilla1 = viewModel::restarAmarillaEquipo1,
                            onAgregarAmarilla2 = viewModel::agregarAmarillaEquipo2,
                            onRestarAmarilla2 = viewModel::restarAmarillaEquipo2,
                            onAgregarRoja1 = viewModel::agregarRojaEquipo1,
                            onRestarRoja1 = viewModel::restarRojaEquipo1,
                            onAgregarRoja2 = viewModel::agregarRojaEquipo2,
                            onRestarRoja2 = viewModel::restarRojaEquipo2,
                            onAgregarEsquina1 = viewModel::agregarEsquinaEquipo1,
                            onRestarEsquina1 = viewModel::restarEsquinaEquipo1,
                            onAgregarEsquina2 = viewModel::agregarEsquinaEquipo2,
                            onRestarEsquina2 = viewModel::restarEsquinaEquipo2,
                            modifier = Modifier.fillMaxSize()
                        )
                        1 -> PenalesTab(
                            partido = partido,

                            // Estado de penales
                            penalesActivos = state.penalesActivos,
                            equipoQueInicia = state.equipoQueInicia,
                            equipoEnTurno = state.equipoEnTurno,
                            tandaActual = state.tandaActual,
                            historiaPenales1 = state.historiaPenales1,
                            historiaPenales2 = state.historiaPenales2,

                            // Callbacks de activación/desactivación
                            onActivarPenales = viewModel::activarPenales,  // ← Recibe equipoInicia: Int
                            onDesactivarPenales = viewModel::desactivarPenales,

                            // Callbacks de configuración
                            onCambiarEquipoInicia = viewModel::cambiarEquipoInicia,
                            onCambiarTurno = viewModel::cambiarTurno,

                            // Callbacks de registro de tiros
                            onAnotarGol = viewModel::anotarGolPenal,
                            onAnotarFallo = viewModel::anotarFalloPenal,

                            // Callbacks de corrección manual
                            onAgregarPenalEquipo1 = viewModel::agregarPenalManualEquipo1,
                            onRestarPenalEquipo1 = viewModel::restarPenalManualEquipo1,
                            onAgregarPenalEquipo2 = viewModel::agregarPenalManualEquipo2,
                            onRestarPenalEquipo2 = viewModel::restarPenalManualEquipo2,

                            // Callback de nueva tanda
                            onNuevaTanda = viewModel::nuevaTandaPenales,
                            onFinalizarPenales = viewModel::finalizarYResetearPenales,
                            modifier = Modifier.fillMaxSize()
                        )

                        2 -> InformacionTab(
                            partido = partido,
                            modoTransmision = state.modoTransmision,
                            onToggleTransmision = viewModel::toggleModoTransmision,
                            modifier = Modifier.fillMaxSize()
                        )
                        3 -> BotoneraTab(
                            modifier = Modifier.fillMaxSize()
                        )
                        4 -> PublicidadTab(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Loading/Error
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            state.error?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}