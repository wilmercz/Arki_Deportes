package com.example.arki_deportes.ui.envivo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.arki_deportes.data.model.Partido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidosEnVivoScreen(
    viewModel: PartidosEnVivoViewModel,
    onPartidoClick: (String, String) -> Unit, // campeonatoId, partidoId
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partidos En Vivo") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                },
                actions = {
                    // Botón para eliminar todos los finalizados
                    if (state.partidos.any { it.ESTADO == 1 }) {
                        IconButton(onClick = { viewModel.eliminarFinalizados() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpiar finalizados",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.partidos.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏁", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No hay partidos activos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val partidosOrdenados = state.partidos.sortedBy { it.ESTADO }

                    items(partidosOrdenados) { partido ->
                        PartidoVivoItem(
                            partido = partido,
                            onClick = {
                                onPartidoClick(partido.CAMPEONATOCODIGO, partido.CODIGOPARTIDO)
                            },
                            onDelete = {
                                viewModel.eliminarPartidoDeEnVivo(partido.CODIGOPARTIDO)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PartidoVivoItem(
    partido: Partido,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val esFinalizado = partido.ESTADO == 1
    val enJuego = partido.ESTADO == 0 && partido.TIEMPOSJUGADOS > 0

    // Colores minimalistas
    val statusColor = when {
        esFinalizado -> Color(0xFF424242) // Gris Oscuro para finalizado
        enJuego -> Color(0xFFD32F2F)      // Rojo para En Vivo
        else -> Color(0xFF1976D2)         // Azul para programado
    }

    val cardAlpha = if (esFinalizado) 0.6f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esFinalizado) 0.dp else 2.dp),
        border = if (enJuego) BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)) else null
    ) {
        Box {
            // Botón de eliminar individual (arriba a la derecha)
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar de la lista",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray.copy(alpha = 0.7f)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // 1. CABECERA MINI (Campeonato y Etiqueta)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 24.dp), // Espacio para el botón X
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = partido.CAMPEONATOTXT.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = statusColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = when {
                                esFinalizado -> "FINALIZADO"
                                enJuego -> "• EN VIVO"
                                else -> "PENDIENTE"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. FILA PRINCIPAL (LOGO - EQUIPO - MARCADOR - EQUIPO - LOGO)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Equipo 1
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = partido.EQUIPO1,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.End,
                            color = if (esFinalizado) Color.Gray else Color.Unspecified
                        )
                        Spacer(Modifier.width(8.dp))
                        AsyncImage(
                            model = partido.BANDERAEQUIPO1,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Marcador
                    Box(
                        modifier = Modifier.width(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${partido.GOLES1} - ${partido.GOLES2}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (esFinalizado) Color.Gray else Color.Unspecified
                            )
                            if (partido.MARCADOR_PENALES) {
                                Text(
                                    text = "(${partido.PENALES1}) PEN (${partido.PENALES2})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = Color.Red
                                )
                            }
                        }
                    }

                    // Equipo 2
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        AsyncImage(
                            model = partido.BANDERAEQUIPO2,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = partido.EQUIPO2,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = if (esFinalizado) Color.Gray else Color.Unspecified
                        )
                    }
                }

                // 3. TIEMPO (Solo si está en juego o hay info relevante)
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            esFinalizado -> "Encuentro terminado"
                            enJuego -> "⏱️ ${partido.TIEMPOJUEGO}"
                            else -> "Inicio: ${partido.HORA_PARTIDO}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (enJuego) statusColor else Color.Gray
                    )
                }
            }
        }
    }
}
