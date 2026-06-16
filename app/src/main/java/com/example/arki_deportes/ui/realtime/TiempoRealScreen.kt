// ui/realtime/TiempoRealScreen.kt

package com.example.arki_deportes.ui.realtime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.ui.realtime.components.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiempoRealScreen(
    viewModel: TiempoRealViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showConfirmDesync by remember { mutableStateOf(false) }
    var showConfirmFinalizar by remember { mutableStateOf(false) }

    var isCronometroExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(state.penalesActivos) {
        if (state.penalesActivos) {
            isCronometroExpanded = false
        } else {
            isCronometroExpanded = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Control") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Abrir menú")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::togglePortada) {
                        Icon(
                            imageVector = if (state.mostrarPortada) Icons.Default.CoPresent else Icons.Default.PictureInPicture,
                            contentDescription = "Mostrar Portada",
                            tint = if (state.mostrarPortada) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (state.modoTransmision) {
                                showConfirmDesync = true
                            } else {
                                viewModel.toggleModoTransmision()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (state.modoTransmision) Icons.Default.Sensors else Icons.Default.SensorsOff,
                            contentDescription = "Sincronización Overlay",
                            tint = if (state.modoTransmision) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = { showConfirmFinalizar = true }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Finalizar y Liberar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCronometroExpanded = !isCronometroExpanded }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(4.dp))
                                Text("CONTROL DE TIEMPO", fontWeight = FontWeight.Bold)
                            }

                            if (!isCronometroExpanded) {
                                Text(
                                    text = state.tiempoActual,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 5.dp)
                                )
                            }

                            Icon(
                                imageVector = if (isCronometroExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(
                            visible = isCronometroExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            CronometroPanel(
                                tiempoActual = state.tiempoActual,
                                partido = partido,
                                estaPausado = state.cronoPausado,
                                onIniciar = viewModel::iniciarPartido,
                                onDetener = viewModel::detenerCronometro,
                                onReiniciar = viewModel::reiniciarPartido,
                                onTogglePausa = viewModel::togglePausa,
                                onAjustar = viewModel::ajustarTiempo,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                MarcadorPanel(
                    equipo1 = partido.EQUIPO1,
                    equipo2 = partido.EQUIPO2,
                    goles1 = partido.getGoles1Int(),
                    goles2 = partido.getGoles2Int(),
                    deporte = partido.DEPORTE,
                    modifier = Modifier.fillMaxWidth()
                )

                // ═══════════════════════════════════════════════════════
                // --- SISTEMA DE TABS DINÁMICO ---
                // ═══════════════════════════════════════════════════════
                val isFutbol = partido.DEPORTE.equals("FUTBOL", true)
                val availableTabs = remember(isFutbol) {
                    listOfNotNull(
                        "CONTROLES",
                        if (isFutbol) "PENALES" else null,
                        "INFO",
                        "AUDIO",
                        "PUB",
                        "TABLA",
                        "PRODUCCION",
                        "PARTIDOS"
                    )
                }
                
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                
                LaunchedEffect(availableTabs) {
                    if (selectedTabIndex >= availableTabs.size) {
                        selectedTabIndex = 0
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 4.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {}
                    ) {
                        availableTabs.forEachIndexed { index, tabKey ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    when (tabKey) {
                                        "CONTROLES" -> {
                                            val icon = if (partido.DEPORTE == "BASQUET") "🏀" else "⚽"
                                            val label = if (partido.DEPORTE == "BASQUET") "Puntos" else "Controles"
                                            Text("$icon $label")
                                        }
                                        "PENALES" -> Text("🎯 Penales")
                                        "INFO" -> Text("ℹ️ Info")
                                        "AUDIO" -> Text("🎵 Audio")
                                        "PUB" -> Text("📢 Pub")
                                        "TABLA" -> Text("📊 Tabla")
                                        "PRODUCCION" -> Text("🎬 Producción")
                                        "PARTIDOS" -> Text("⚽ Partidos")
                                    }
                                }
                            )
                        }
                    }

                    val currentTab = availableTabs.getOrNull(selectedTabIndex) ?: "CONTROLES"
                    
                    when (currentTab) {
                        "CONTROLES" -> ControlPartidoTab(
                            equipo1 = partido.EQUIPO1,
                            equipo2 = partido.EQUIPO2,
                            deporte = partido.DEPORTE,
                            goles1 = partido.getGoles1Int(),
                            goles2 = partido.getGoles2Int(),
                            amarillas1 = partido.getAmarillas1Int(),
                            amarillas2 = partido.getAmarillas2Int(),
                            rojas1 = partido.getRojas1Int(),
                            rojas2 = partido.getRojas2Int(),
                            esquinas1 = partido.getEsquinas1Int(),
                            esquinas2 = partido.getEsquinas2Int(),
                            marcadorFutbolVisible = state.marcadorFutbolVisible,
                            onToggleMarcador = viewModel::toggleMarcadorFutbol_Basquet,
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
                            lowerThirdVisible = state.lowerThirdVisible,
                            onToggleLowerThird = viewModel::toggleLowerThird,
                            modifier = Modifier.fillMaxSize()
                        )
                        "PENALES" -> PenalesTab(
                            partido = partido,
                            penalesActivos = state.penalesActivos,
                            equipoQueInicia = state.equipoQueInicia,
                            equipoEnTurno = state.equipoEnTurno,
                            tandaActual = state.tandaActual,
                            historiaPenales1 = state.historiaPenales1,
                            historiaPenales2 = state.historiaPenales2,
                            onActivarPenales = viewModel::activarPenales,
                            onDesactivarPenales = viewModel::desactivarPenales,
                            onCambiarEquipoInicia = viewModel::cambiarEquipoInicia,
                            onCambiarTurno = viewModel::cambiarTurno,
                            onAnotarGol = viewModel::anotarGolPenal,
                            onAnotarFallo = viewModel::anotarFalloPenal,
                            onAgregarPenalEquipo1 = viewModel::agregarPenalManualEquipo1,
                            onRestarPenalEquipo1 = viewModel::restarPenalManualEquipo1,
                            onAgregarPenalEquipo2 = viewModel::agregarPenalManualEquipo2,
                            onRestarPenalEquipo2 = viewModel::restarPenalManualEquipo2,
                            onNuevaTanda = viewModel::nuevaTandaPenales,
                            onFinalizarPenales = viewModel::finalizarYResetearPenales,
                            modifier = Modifier.fillMaxSize()
                        )
                        "INFO" -> InformacionTab(
                            partido = partido,
                            nombreCampeonato = state.nombreCampeonatoReal,
                            modoTransmision = state.modoTransmision,
                            onToggleTransmision = viewModel::toggleModoTransmision,
                            onSendInfo = { texto -> viewModel.enviarInfoAlOverlay("📍 $texto") },
                            onGenerateSocialText = viewModel::generarTextoSocial,
                            mostrarRedes = state.mostrarRedes, // 👈 NUEVO
                            onToggleRedes = viewModel::toggleMostrarRedes, // 👈 NUEVO
                            modifier = Modifier.fillMaxSize()
                        )
                        "AUDIO" -> BotoneraTab(
                            audios = state.audios,
                            volumen = state.volumenAudio,
                            estado = state.audioEstado,
                            deporteActual = partido.DEPORTE,
                            reproduccionLocal = state.reproduccionLocal,
                            onToggleLocal = viewModel::toggleReproduccionLocal,
                            onPlay = viewModel::reproducirAudio,
                            onPause = viewModel::pausarAudio,
                            onStop = viewModel::detenerAudio,
                            onVolumeChange = viewModel::cambiarVolumen,
                            posicionActual = state.audioPosicionActual,
                            duracionTotal = state.audioDuracionTotal,
                            onSeek = viewModel::buscarPosicionAudio,
                            idAudioActual = state.idAudioActual,
                            modoBucle = state.modoBucle,
                            onToggleBucle = viewModel::toggleBucle,
                            onExplorarCarpeta = viewModel::setCarpetaTemporal,
                            modifier = Modifier.fillMaxSize()
                        )
                        "PUB" -> PublicidadTab(
                            banners = state.banners,
                            selectedIds = state.selectedBannerIds,
                            onToggle = viewModel::toggleBannerSelection,
                            onSendSingle = viewModel::enviarPublicidadUnica,
                            onSendSequential = viewModel::enviarListaSecuencial,
                            onHide = viewModel::ocultarPublicidad,
                            modifier = Modifier.fillMaxSize()
                        )
                        "TABLA" -> TablaPosicionesTab(
                            tabla = state.tablaPosiciones,
                            mostrarEnWeb = state.mostrarTablaPosiciones,
                            onToggleWeb = viewModel::toggleTablaPosiciones,
                            onSyncData = viewModel::sincronizarTablaManual,
                            mostrarComparativa = state.mostrarComparativa,
                            onToggleComparativa = viewModel::toggleComparativa,
                            modifier = Modifier.fillMaxSize()
                        )
                        "PRODUCCION" -> ProduccionTab(
                            equipo = state.equipoProduccion,
                            partido = partido,
                            nombreCampeonato = state.nombreCampeonatoReal,
                            onUpdateProduccion = viewModel::actualizarCampoProduccion,
                            onSendText = viewModel::enviarInfoAlOverlay,
                            onForzarGanador = viewModel::forzarCalculoGanador,
                            modifier = Modifier.fillMaxSize()
                        )
                        "PARTIDOS" -> OtrosPartidosTab(
                            otrosPartidos = state.otrosPartidos,
                            onSendMatchResult = viewModel::enviarResultadoOtroPartido,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

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

        if (showConfirmFinalizar) {
            AlertDialog(
                onDismissRequest = { showConfirmFinalizar = false },
                title = { Text("¿Finalizar Partido?") },
                text = { Text("Se liberará el partido de tu perfil para que puedas elegir otro. Asegúrate de haber guardado todos los cambios.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.finalizarPartido { onNavigateBack() }
                            showConfirmFinalizar = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Finalizar y Liberar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmFinalizar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showConfirmDesync) {
            AlertDialog(
                onDismissRequest = { showConfirmDesync = false },
                title = { Text("¿Desactivar Sincronización?") },
                text = { Text("El partido dejará de actualizarse en los overlays web (PARTIDOACTUAL). ¿Estás seguro?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.toggleModoTransmision()
                            showConfirmDesync = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Desactivar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDesync = false }) {
                        Text("Mantener Activo")
                    }
                }
            )
        }
    }
}
