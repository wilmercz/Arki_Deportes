// ui/realtime/components/MarcadorPanel.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MARCADOR PANEL - MINIMALISTA CON FALLBACK
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
fun MarcadorPanel(
    equipo1: String,
    equipo2: String,
    goles1: Int,
    goles2: Int,
    modifier: Modifier = Modifier
) {
    // ✅ Fallback si los nombres están vacíos
    val nombreEquipo1 = equipo1.ifBlank { "Equipo 1" }
    val nombreEquipo2 = equipo2.ifBlank { "Equipo 2" }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // ← CAMBIADO: superficie neutra
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // EQUIPO 1
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = nombreEquipo1.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface // ← CAMBIADO
                )
                Text(
                    text = "⚽ $goles1",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // MARCADOR CENTRAL (MANTENER COLOR AZUL)
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "$goles1 - $goles2",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            // EQUIPO 2
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = nombreEquipo2.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurface // ← CAMBIADO
                )
                Text(
                    text = "⚽ $goles2",
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}