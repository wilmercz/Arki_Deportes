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
import android.util.Log
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
    val context = LocalContext.current

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
        Log.d("InformacionTab", "Contenido de TEXTOFACEBOOK: '${partido.TEXTOFACEBOOK}'")
// Cambiamos la condición para manejar nulos y espacios en blanco de forma más robusta
        val tieneTexto = !partido.TEXTOFACEBOOK.isNullOrBlank()

        if (tieneTexto) {
            // ... el resto de tu código de la sección de Redes Sociales
        } else {
            // 💡 OPCIONAL: Mostrar un mensaje si está vacío para confirmar que el TAB funciona
            Text("Sin resumen disponible", style = MaterialTheme.typography.bodySmall)
        }


        // --- SECCIÓN: TEXTO PARA REDES SOCIALES ---
        if (partido.TEXTOFACEBOOK.isNotBlank()) {
            Text(
                text = "Resumen para Redes Sociales",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = partido.TEXTOFACEBOOK,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Resumen Partido", partido.TEXTOFACEBOOK)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "¡Copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar texto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

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