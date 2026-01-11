// ui/realtime/components/BotoneraTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Botonera de Audios",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "6 columnas x N filas (escalable)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            // ═══════════════════════════════════════════════════════════
            // GRID DE BOTONES - 6 COLUMNAS
            // ═══════════════════════════════════════════════════════════
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // ═══════════════════════════════════════════════════════
                // FILA 1: AUDIOS PRINCIPALES
                // ═══════════════════════════════════════════════════════
                item { BotonAudio("Presentación", ::reproducirPresentacion) }
                item { BotonAudio("Cortina", ::reproducirCortina) }
                item { BotonAudio("T. Esquina", ::reproducirTiroEsquina) }
                item { BotonAudio("T. Juego", ::reproducirTiempoJuego) }
                item { BotonAudio("Marcador", ::reproducirMarcador) }
                item { BotonAudio("Extra 1", ::reproducirExtra1) }

                // ═══════════════════════════════════════════════════════
                // FILA 2: AVANCES 1-6
                // ═══════════════════════════════════════════════════════
                item { BotonAudio("Avance 1", ::reproducirAvance1) }
                item { BotonAudio("Avance 2", ::reproducirAvance2) }
                item { BotonAudio("Avance 3", ::reproducirAvance3) }
                item { BotonAudio("Avance 4", ::reproducirAvance4) }
                item { BotonAudio("Avance 5", ::reproducirAvance5) }
                item { BotonAudio("Avance 6", ::reproducirAvance6) }

                // ═══════════════════════════════════════════════════════
                // FILA 3: BOTONES LIBRES (Escalable para futuro)
                // ═══════════════════════════════════════════════════════
                item { BotonAudio("Libre 1", ::botonLibre) }
                item { BotonAudio("Libre 2", ::botonLibre) }
                item { BotonAudio("Libre 3", ::botonLibre) }
                item { BotonAudio("Libre 4", ::botonLibre) }
                item { BotonAudio("Libre 5", ::botonLibre) }
                item { BotonAudio("Libre 6", ::botonLibre) }

                // ═══════════════════════════════════════════════════════
                // FILA 4+: Más botones escalables
                // ═══════════════════════════════════════════════════════
                // Agregar más items aquí cuando sea necesario
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