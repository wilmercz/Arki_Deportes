// ui/realtime/components/InformacionTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.Partido
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.getSystemService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.graphics.Color

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
    onGenerateSocialText: () -> Unit,
    mostrarRedes: Boolean, // 👈 NUEVO
    onToggleRedes: (Boolean) -> Unit, // 👈 NUEVO
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tieneTexto = !partido.TEXTOFACEBOOK.isNullOrBlank()

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

        HorizontalDivider()

        InfoRow("Campeonato", nombreCampeonato.ifBlank { "No especificado" }) {
            onSendInfo(nombreCampeonato)
        }
        InfoRow("Estadio", partido.ESTADIO.ifBlank { "No especificado" }) {
            onSendInfo(partido.ESTADIO)
        }
        InfoRow("Lugar", partido.LUGAR.ifBlank { "No especificado" }) {
            onSendInfo(partido.LUGAR)
        }
        InfoRow("Fecha", partido.FECHA_PARTIDO.ifBlank { "No especificada" })
        InfoRow("Hora", partido.HORA_PARTIDO.ifBlank { "No especificada" })
        InfoRow("Etapa", getEtapaTexto(partido.ETAPA))

        HorizontalDivider()

        // 🌐 PANEL REDES SOCIALES OVERLAY (NUEVO)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (mostrarRedes) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (mostrarRedes) Icons.Default.Public else Icons.Default.PublicOff,
                        contentDescription = null,
                        tint = if (mostrarRedes) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("PANEL REDES EN OVERLAY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (mostrarRedes) "Visible en Web Overlay" else "Oculto en Web Overlay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = mostrarRedes,
                    onCheckedChange = { onToggleRedes(it) }
                )
            }
        }

        HorizontalDivider()

        // 📱 SECCIÓN: REDES SOCIALES (MEJORADA)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resumen para Redes Sociales",
                style = MaterialTheme.typography.titleSmall,
                color = if (tieneTexto) MaterialTheme.colorScheme.primary else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            // Botón para GENERAR si está vacío o se quiere actualizar
            TextButton(onClick = onGenerateSocialText) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (tieneTexto) "Regenerar" else "Generar ahora")
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = if (tieneTexto) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
            else 
                Color.LightGray.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                if (tieneTexto) MaterialTheme.colorScheme.outlineVariant else Color.LightGray
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (tieneTexto) partido.TEXTOFACEBOOK else "No hay resumen generado para este partido.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (tieneTexto) MaterialTheme.colorScheme.onSurface else Color.Gray,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    enabled = tieneTexto,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Resumen Partido", partido.TEXTOFACEBOOK)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "¡Copiado!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar texto",
                        tint = if (tieneTexto) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }

        HorizontalDivider()

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
        if (onSend != null && valor != "No especificado") {
            IconButton(onClick = onSend, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Tv, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
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
