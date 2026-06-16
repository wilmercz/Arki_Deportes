// ui/realtime/components/BotoneraTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.AudioResource

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * BOTONERA TAB - SISTEMA HÍBRIDO (LOCAL / NUBE)
 * ═══════════════════════════════════════════════════════════════════════════
 */

@OptIn(ExperimentalMaterial3Api::class)
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
    reproduccionLocal: Boolean,
    onToggleLocal: (Boolean) -> Unit,
    posicionActual: Long,
    duracionTotal: Long,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // 🔍 Filtramos por deporte y tipo "GENERAL"
    val audiosDelDeporte = audios.filter {
        it.deporte.equals(deporteActual, ignoreCase = true) || it.deporte == "GENERAL"
    }

    val fxAudios = audiosDelDeporte.filter { it.tipo == "FX" }
    val musicaAudios = audiosDelDeporte.filter { it.tipo == "MUSICA" }

    // Determinar si hay audios locales disponibles (vienen de la carpeta vinculada con content://)
    val tieneMusicaLocal = musicaAudios.any { it.url.startsWith("content://") }

    // Estado para el selector LOCAL/NUBE.
    // Por defecto elige LOCAL si hay archivos disponibles, si no, elige NUBE.
    var origenMusicaSeleccionado by remember(tieneMusicaLocal) {
        mutableStateOf(if (tieneMusicaLocal) "LOCAL" else "NUBE")
    }

    // Filtrar la música a mostrar según la elección del usuario
    val musicaFiltrada = musicaAudios.filter {
        if (origenMusicaSeleccionado == "LOCAL") it.url.startsWith("content://")
        else !it.url.startsWith("content://")
    }

    var selectedSubTab by remember { mutableIntStateOf(0) }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    Column(modifier = modifier.fillMaxSize().padding(8.dp)) {

        // --- TABS DE CONTROL (MANDO / AJUSTES) ---
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier.height(48.dp)
        ) {
            Tab(selected = selectedSubTab == 0, onClick = { selectedSubTab = 0 }) {
                Text("Mando", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge)
            }
            Tab(selected = selectedSubTab == 1, onClick = { selectedSubTab = 1 }) {
                Text("Ajustes", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            when (selectedSubTab) {
                0 -> { // PESTAÑA REPRODUCTOR
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = onStop) { Icon(Icons.Default.Stop, "Stop", tint = Color.Red) }
                            IconButton(onClick = {
                                if (estado == "PLAY") onPause()
                                else if (musicaFiltrada.isNotEmpty()) onPlay(musicaFiltrada.first())
                            }) {
                                Icon(
                                    imageVector = if (estado == "PLAY") Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause"
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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

                        if (reproduccionLocal && duracionTotal > 0) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                Slider(
                                    value = posicionActual.toFloat(),
                                    onValueChange = { onSeek(it) },
                                    valueRange = 0f..duracionTotal.toFloat(),
                                    modifier = Modifier.height(20.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.outline,
                                        activeTrackColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(formatTime(posicionActual), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(formatTime(duracionTotal), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
                1 -> { // PESTAÑA AJUSTES
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Producción de Audio", fontWeight = FontWeight.Bold)
                            Text(
                                text = if(reproduccionLocal) "Modo Local (Móvil)" else "Modo Remoto (Web)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Switch(checked = reproduccionLocal, onCheckedChange = onToggleLocal)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- SECCIÓN MÚSICA CON SELECTOR HÍBRIDO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎵 MÚSICA ($deporteActual)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Selector LOCAL / NUBE (Chips)
            Row {
                FilterChip(
                    selected = origenMusicaSeleccionado == "LOCAL",
                    onClick = { origenMusicaSeleccionado = "LOCAL" },
                    label = { Text("LOCAL", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (origenMusicaSeleccionado == "LOCAL") {
                        { Icon(Icons.Default.Folder, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
                Spacer(Modifier.width(4.dp))
                FilterChip(
                    selected = origenMusicaSeleccionado == "NUBE",
                    onClick = { origenMusicaSeleccionado = "NUBE" },
                    label = { Text("NUBE", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (origenMusicaSeleccionado == "NUBE") {
                        { Icon(Icons.Default.Cloud, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        // Advertencia si intenta usar local sin modo local activo
        if (origenMusicaSeleccionado == "LOCAL" && !reproduccionLocal) {
            Text(
                "⚠️ Activa 'Modo Local' en Ajustes para reproducir",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.heightIn(max = 180.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (musicaFiltrada.isEmpty()) {
                item {
                    Text(
                        "No hay audios en ${origenMusicaSeleccionado.lowercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(musicaFiltrada) { musica ->
                val isLocal = musica.url.startsWith("content://")
                OutlinedButton(
                    onClick = { onPlay(musica) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, if(isLocal) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = if (isLocal) Icons.Default.Folder else Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if(isLocal) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(musica.nombre, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // --- SECCIÓN FX (EFECTOS) ---
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
                val esSistema = fx.id.startsWith("${deporteActual}_FX_") || fx.id.startsWith("FX_")
                Button(
                    onClick = { onPlay(fx) },
                    modifier = Modifier.height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esSistema) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(fx.nombre, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, maxLines = 2)
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
