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

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * TIEMPO REAL SCREEN - PANTALLA PRINCIPAL DE CONTROL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Pantalla dividida en 3 paneles:
 * 1. Panel Superior: Cronómetro y controles
 * 2. Panel Medio: Equipos (izquierda/derecha) con botones
 * 3. Panel Inferior: Tabs (Información, Botonera, Publicidad)
 *
 * @author ARKI SISTEMAS
 */
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
                title = {
                    Column {
                        Text("Control de Partido")
                        state.partido?.let {
                            Text(
                                text = it.getEstadoTexto(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
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
                // PANEL 1: CRONÓMETRO (Superior)
                // ═══════════════════════════════════════════════════════
                CronometroPanel(
                    tiempoActual = state.tiempoActual,
                    numeroTiempo = partido.NumeroDeTiempo,
                    onIniciar = viewModel::iniciarPartido,
                    onDetener = viewModel::detenerCronometro,
                    onAjustar = viewModel::ajustarTiempo,
                    modifier = Modifier.fillMaxWidth()
                )

                // ═══════════════════════════════════════════════════════
                // PANEL 2: EQUIPOS (Medio - dividido en 2)
                // ═══════════════════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PANEL 2A: EQUIPO 1 (Izquierda)
                    EquipoCard(
                        nombreEquipo = partido.Equipo1,
                        goles = partido.getGoles1Int(),
                        amarillas = partido.getAmarillas1Int(),
                        rojas = partido.getRojas1Int(),
                        esquinas = partido.getEsquinas1Int(),
                        onAgregarGol = viewModel::agregarGolEquipo1,
                        onRestarGol = viewModel::restarGolEquipo1,
                        onAgregarAmarilla = viewModel::agregarAmarillaEquipo1,
                        onRestarAmarilla = viewModel::restarAmarillaEquipo1,
                        onAgregarRoja = viewModel::agregarRojaEquipo1,
                        onRestarRoja = viewModel::restarRojaEquipo1,
                        onAgregarEsquina = viewModel::agregarEsquinaEquipo1,
                        onRestarEsquina = viewModel::restarEsquinaEquipo1,
                        modifier = Modifier.weight(1f)
                    )

                    // PANEL 2B: EQUIPO 2 (Derecha)
                    EquipoCard(
                        nombreEquipo = partido.Equipo2,
                        goles = partido.getGoles2Int(),
                        amarillas = partido.getAmarillas2Int(),
                        rojas = partido.getRojas2Int(),
                        esquinas = partido.getEsquinas2Int(),
                        onAgregarGol = viewModel::agregarGolEquipo2,
                        onRestarGol = viewModel::restarGolEquipo2,
                        onAgregarAmarilla = viewModel::agregarAmarillaEquipo2,
                        onRestarAmarilla = viewModel::restarAmarillaEquipo2,
                        onAgregarRoja = viewModel::agregarRojaEquipo2,
                        onRestarRoja = viewModel::restarRojaEquipo2,
                        onAgregarEsquina = viewModel::agregarEsquinaEquipo2,
                        onRestarEsquina = viewModel::restarEsquinaEquipo2,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ═══════════════════════════════════════════════════════
                // PANEL 3: TABS (Inferior)
                // ═══════════════════════════════════════════════════════
                var selectedTab by remember { mutableStateOf(0) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                ) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Información") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Botonera") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Publicidad") }
                        )
                    }

                    when (selectedTab) {
                        0 -> InformacionTab(
                            partido = partido,
                            modoTransmision = state.modoTransmision,
                            onToggleTransmision = viewModel::toggleModoTransmision,
                            modifier = Modifier.fillMaxSize()
                        )
                        1 -> BotoneraTab(
                            modifier = Modifier.fillMaxSize()
                        )
                        2 -> PublicidadTab(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Loading/Error states
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
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