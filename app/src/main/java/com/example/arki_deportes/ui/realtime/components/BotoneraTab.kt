// ui/realtime/components/BotoneraTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.AudioResource
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.lazy.items // Para el LazyRow (Música)
import androidx.compose.foundation.lazy.grid.items // Para el LazyVerticalGrid (FX) - ESTE ES EL QUE TE FALTA

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * BOTONERA TAB
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tab con botonera de audios organizados en grid de 6 columnas
 *
 * VB.NET Equivalente: Botonera de audios de FrmControl
 *
 * ESTRUCTURA:
 * - 6 columnas (botones por fila)
 * - N filas (escalable)
 * - Fila 1: Audios principales
 * - Fila 2: Avances 1-6
 * - Fila 3+: Libres para futuro
 *
 * NOTA: Las funciones de reproducción están vacías por ahora.
 * En el futuro leerán desde Firebase Storage.
 */

@Composable
fun BotoneraTab(
    audios: List<AudioResource>,
    volumen: Int,
    estado: String,
    deporteActual: String,
    onPlay: (AudioResource) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 🔍 Filtramos por deporte y tipo
    val audiosDelDeporte = audios.filter {
        it.deporte.equals(deporteActual, ignoreCase = true) || it.deporte == "GENERAL"
    }

    val fxAudios = audios.filter { it.tipo == "FX" }
    val musicaAudios = audios.filter { it.tipo == "MUSICA" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // --- SECCIÓN MÚSICA (Controlador) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Control de Música",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, "Stop", tint = Color.Red)
                    }

                    IconButton(onClick = {
                        if (estado == "PLAY") onPause()
                        else if (musicaAudios.isNotEmpty()) onPlay(musicaAudios.first())
                    }) {
                        Icon(
                            imageVector = if (estado == "PLAY") Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }

                    // Slider de Volumen
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(16.dp))
                        Slider(
                            value = volumen.toFloat(),
                            onValueChange = { onVolumeChange(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text("${volumen}%", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // --- SECCIÓN MÚSICA (Grid 2 columnas) ---
                Text(
                    text = "🎵 MÚSICA ($deporteActual)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 150.dp), // Limitamos altura para dejar espacio a los FX
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(musicaAudios) { musica ->
                        OutlinedButton(
                            onClick = { onPlay(musica) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(musica.nombre, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // --- SECCIÓN FX (Grid 3 columnas) ---
                Text(
                    text = "🎹 EFECTOS (FX)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).padding(top = 8.dp)
                ) {
                    items(items = fxAudios) { fx ->
                        // 🎯 Si el FX tiene un ID de sistema (ej: FUTBOL_FX_ESQUINA), lo destacamos
                        val esSistema = fx.id.startsWith("${deporteActual}_FX_") || fx.id.startsWith("FX_")

                        Button(
                            onClick = { onPlay(fx) },
                            modifier = Modifier.height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (esSistema) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(
                                text = fx.nombre,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                fontWeight = if (esSistema) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

    }
}

/**
 * Botón individual de audio
 */
@Composable
private fun BotonAudio(
    texto: String,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = texto,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// FUNCIONES DE REPRODUCCIÓN DE AUDIO
// ═══════════════════════════════════════════════════════════════════════════
//
// NOTA IMPORTANTE:
// Estas funciones están vacías por ahora.
// En el futuro leerán las rutas de los audios desde Firebase (nodo principal)
// y reproducirán usando MediaPlayer o ExoPlayer.
//
// Estructura sugerida en Firebase:
// /Audios/
//   ├─ Presentacion: "gs://bucket/audios/presentacion.mp3"
//   ├─ Cortina: "gs://bucket/audios/cortina.mp3"
//   ├─ TiroEsquina: "gs://bucket/audios/tiro_esquina.mp3"
//   └─ ...
//
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Reproduce el audio de presentación
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirPresentacion() {
    // TODO: Leer ruta desde Firebase
    // TODO: Reproducir con MediaPlayer/ExoPlayer
    println("🎵 Reproduciendo: Presentación")
}

/**
 * Reproduce el audio de cortina
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirCortina() {
    println("🎵 Reproduciendo: Cortina")
}

/**
 * Reproduce el audio de tiro de esquina
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirTiroEsquina() {
    println("🎵 Reproduciendo: Tiro de Esquina")
}

/**
 * Reproduce el audio de tiempo de juego
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirTiempoJuego() {
    println("🎵 Reproduciendo: Tiempo de Juego")
}

/**
 * Reproduce el audio de marcador
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirMarcador() {
    println("🎵 Reproduciendo: Marcador")
}

/**
 * Reproduce audio extra 1
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirExtra1() {
    println("🎵 Reproduciendo: Extra 1")
}

/**
 * Reproduce avance 1
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirAvance1() {
    println("🎵 Reproduciendo: Avance 1")
}

/**
 * Reproduce avance 2
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirAvance2() {
    println("🎵 Reproduciendo: Avance 2")
}

/**
 * Reproduce avance 3
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirAvance3() {
    println("🎵 Reproduciendo: Avance 3")
}

/**
 * Reproduce avance 4
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirAvance4() {
    println("🎵 Reproduciendo: Avance 4")
}

/**
 * Reproduce avance 5
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirAvance5() {
    println("🎵 Reproduciendo: Avance 5")
}

/**
 * Reproduce avance 6
 * TODO: Implementar reproducción desde Firebase Storage
 */
private fun reproducirAvance6() {
    println("🎵 Reproduciendo: Avance 6")
}

/**
 * Botón libre (sin funcionalidad por ahora)
 * TODO: Asignar funcionalidad según necesidad
 */
private fun botonLibre() {
    println("🔘 Botón libre presionado")
}