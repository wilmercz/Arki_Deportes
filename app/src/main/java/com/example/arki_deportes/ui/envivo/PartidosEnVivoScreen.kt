package com.example.arki_deportes.ui.envivo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.partidos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay partidos jugándose en este momento.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.partidos) { partido ->
                    PartidoVivoItem(partido, onClick = {
                        onPartidoClick(partido.CAMPEONATOCODIGO, partido.CODIGOPARTIDO)
                    })
                }
            }
        }
    }
}

@Composable
private fun PartidoVivoItem(partido: Partido, onClick: () -> Unit) {
    // Determinar estados según campos originales
    val esFinalizado = partido.ESTADO == 1
    val enJuego = partido.ESTADO == 0 && partido.TIEMPOSJUGADOS > 0

    // Color de fondo dinámico: Gris si terminó, Blanco/Surface si está activo
    val cardColor = if (esFinalizado) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esFinalizado) 1.dp else 4.dp),
        border = if (enJuego) androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) else null
    ) {
        Column(Modifier.padding(16.dp)) {
            // Fila Superior: Deporte y ETIQUETA DE ESTADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FÚTBOL",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (esFinalizado) Color.Gray else MaterialTheme.colorScheme.primary
                )

                // Etiqueta de Estado Dinámica
                Surface(
                    color = when {
                        esFinalizado -> Color.Gray
                        enJuego -> Color(0xFFD32F2F) // Rojo para "En Vivo"
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when {
                            esFinalizado -> "FINALIZADO"
                            partido.TIEMPOSJUGADOS == 1 -> "1er TIEMPO"
                            partido.TIEMPOSJUGADOS == 2 -> "2do TIEMPO"
                            partido.TIEMPOSJUGADOS == 3 -> "2do TIEMPO" // Fallback para 3T (2do tiempo real)
                            else -> "POR EMPEZAR"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila Central: Equipos y Marcador Real
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = partido.EQUIPO1,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (esFinalizado) Color.Gray else Color.Unspecified
                )

                // MARCADOR REAL (Leído de la ruta original del partido)
                Text(
                    text = "${partido.GOLES1} - ${partido.GOLES2}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (esFinalizado) Color.Gray else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = partido.EQUIPO2,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (esFinalizado) Color.Gray else Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila Inferior: Tiempo de juego
            Text(
                text = if (esFinalizado) "Partido Terminado" else "⏱️ ${partido.TIEMPOJUEGO}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
