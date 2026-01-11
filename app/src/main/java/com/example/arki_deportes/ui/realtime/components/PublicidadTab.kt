// ui/realtime/components/PublicidadTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gestión de Publicidad",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Esta funcionalidad estará disponible en una futura actualización.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Descripción de funcionalidades futuras
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Funcionalidades planeadas:",
                        style = MaterialTheme.typography.titleSmall
                    )

                    FuncionalidadItem("📢 Gestión de banners publicitarios")
                    FuncionalidadItem("⏰ Programación de spots")
                    FuncionalidadItem("▶️ Control de reproducción automática")
                    FuncionalidadItem("📊 Estadísticas de reproducciones")
                    FuncionalidadItem("🎯 Integración con sponsors")
                }
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