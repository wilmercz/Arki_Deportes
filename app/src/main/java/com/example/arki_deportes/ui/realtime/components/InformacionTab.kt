// ui/realtime/components/InformacionTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.Partido

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * INFORMACIÓN TAB
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tab que muestra información del partido y switch de overlay
 *
 * VB.NET Equivalente: Datos adicionales del partido
 */
@Composable
fun InformacionTab(
    partido: Partido,
    modoTransmision: Boolean,
    onToggleTransmision: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Información del Partido",
                style = MaterialTheme.typography.titleMedium
            )

            Divider()

            // ═══════════════════════════════════════════════════════════
            // INFORMACIÓN BÁSICA
            // ═══════════════════════════════════════════════════════════
            InfoRow("Estadio", partido.ESTADIO ?: "No especificado")
            InfoRow("Lugar", partido.LUGAR ?: "No especificado")
            InfoRow("Fecha", partido.FECHA_PARTIDO ?: "No especificada")
            InfoRow("Hora", partido.HORA_PARTIDO ?: "No especificada")
            InfoRow("Etapa", getEtapaTexto(partido.Etapa))

            Divider()

            // ═══════════════════════════════════════════════════════════
            // SWITCH DE OVERLAY
            // ═══════════════════════════════════════════════════════════
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (modoTransmision)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sincronizar con Overlay",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = if (modoTransmision)
                                "Datos copiándose a PARTIDOACTUAL"
                            else
                                "Overlay desactivado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = modoTransmision,
                        onCheckedChange = { onToggleTransmision() }
                    )
                }
            }

            // Nota explicativa
            Text(
                text = "Nota: Al activar el overlay, los datos se copiarán al nodo " +
                        "PARTIDOACTUAL para que puedan ser leídos por overlays web " +
                        "(GitHub, CameraFi, etc.)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Fila de información
 */
@Composable
private fun InfoRow(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Convierte el número de etapa a texto
 */
private fun getEtapaTexto(etapa: Int): String {
    return when (etapa) {
        0 -> "Grupos / Fase Regular"
        1 -> "Cuartos de Final"
        2 -> "Semifinal"
        3 -> "Final"
        else -> "Etapa $etapa"
    }
}