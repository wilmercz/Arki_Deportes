// ui/realtime/components/PublicidadTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.arki_deportes.data.model.BannerResource

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PUBLICIDAD TAB
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tab para gestión de publicidad (futuro)
 *
 * FUNCIONALIDADES FUTURAS:
 * - Lista de banners publicitarios
 * - Programación de spots
 * - Control de reproducción automática
 * - Estadísticas de reproducciones
 */
@Composable
fun PublicidadTab(
    banners: List<BannerResource>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    onSendSingle: (BannerResource) -> Unit,
    onSendSequential: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Cabecera de Acciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Banners Disponibles (${banners.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Botón Enviar Secuencia
                if (selectedIds.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = onSendSequential,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lanzar (${selectedIds.size})")
                    }
                }

                // Botón Ocultar Overlay
                IconButton(onClick = onHide) {
                    Icon(Icons.Default.VisibilityOff, "Ocultar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Divider()

        if (banners.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay banners registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(banners, key = { it.id }) { banner ->
                    BannerLiveItem(
                        banner = banner,
                        isSelected = selectedIds.contains(banner.id),
                        onToggle = { onToggle(banner.id) },
                        onTransmit = { onSendSingle(banner) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerLiveItem(
    banner: BannerResource,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onTransmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })

            // Miniatura
            Card(modifier = Modifier.size(50.dp), shape = MaterialTheme.shapes.small) {
                if (banner.urlImagen.isNotBlank()) {
                    AsyncImage(
                        model = banner.urlImagen,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)) {
                Text(banner.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(banner.tipo, style = MaterialTheme.typography.bodySmall)
            }

            // Botón TV para lanzar una sola
            IconButton(onClick = onTransmit) {
                Icon(Icons.Default.Tv, "Al Aire", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * Item de funcionalidad futura
 */
@Composable
private fun FuncionalidadItem(texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}