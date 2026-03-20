// ui/realtime/components/InformacionTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.Partido
import androidx.compose.material.icons.filled.*
/**
 * ═══════════════════════════════════════════════════════════════════════════
 * INFORMACIÓN TAB - SOLO INFO DEL PARTIDO
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
fun InformacionTab(
    partido: Partido,
    nombreCampeonato: String,
    modoTransmision: Boolean,
    onToggleTransmision: () -> Unit,
    onSendInfo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Información del Partido",
            style = MaterialTheme.typography.titleMedium
        )

        Divider()
// Usamos nombreCampeonato que viene directo del ViewModel (leído de Firebase)
        InfoRow("Campeonato", nombreCampeonato.ifBlank { "No especificado" }) {
            onSendInfo(nombreCampeonato)
        }
        InfoRow("Estadio", partido.ESTADIO.ifBlank { "No especificado" }) {
            onSendInfo(partido.ESTADIO)
        }
        InfoRow("Lugar", partido.LUGAR.ifBlank { "No especificado" }) {
            onSendInfo(partido.LUGAR)
        }
        InfoRow("Fecha", partido.FECHA_PARTIDO ?: "No especificada")
        InfoRow("Hora", partido.HORA_PARTIDO ?: "No especificada")
        InfoRow("Etapa", getEtapaTexto(partido.ETAPA))

        Divider()

        // SWITCH DE OVERLAY
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

        // Nota
        Text(
            text = "💡 Al activar el overlay, los datos se copiarán al nodo PARTIDOACTUAL para overlays web",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    valor: String,
    onSend: (() -> Unit)? = null // ← Agregar parámetro
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically // Alinear con el botón
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // BOTÓN DE ENVÍO (Solo si hay valor y función)
        if (onSend != null && valor != "No especificado") {
            IconButton(onClick = onSend, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Tv,
                    contentDescription = "Enviar a Overlay",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun getEtapaTexto(etapa: Int): String {
    return when (etapa) {
        0 -> "Grupos / Fase Regular"
        1 -> "Cuartos de Final"
        2 -> "Semifinal"
        3 -> "Final"
        else -> "Etapa $etapa"
    }
}