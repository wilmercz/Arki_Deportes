// ui/realtime/components/CronometroPanel.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CRONÓMETRO PANEL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Panel superior que muestra el cronómetro y controles
 *
 * Botones:
 * - INICIAR: Inicia el partido (0T→1T o 2T→3T)
 * - DETENER: Detiene el partido (1T→2T o 3T→4T)
 * - Ajustes: +/- tiempo (1s, 10s, 30s, 1m, 5m)
 */
@Composable
fun CronometroPanel(
    tiempoActual: String,
    numeroTiempo: String,
    onIniciar: () -> Unit,
    onDetener: () -> Unit,
    onAjustar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ═══════════════════════════════════════════════════════════
            // DISPLAY DEL CRONÓMETRO
            // ═══════════════════════════════════════════════════════════
            Text(
                text = tiempoActual,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = when (numeroTiempo) {
                    "0T" -> "No iniciado"
                    "1T" -> "Primer Tiempo"
                    "2T" -> "Descanso"
                    "3T" -> "Segundo Tiempo"
                    "4T" -> "Finalizado"
                    else -> numeroTiempo
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // ═══════════════════════════════════════════════════════════
            // BOTONES PRINCIPALES
            // ═══════════════════════════════════════════════════════════
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón INICIAR (solo si no está jugando)
                if (numeroTiempo == "0T" || numeroTiempo == "2T") {
                    Button(
                        onClick = onIniciar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (numeroTiempo == "0T") "INICIAR" else "SEGUNDO TIEMPO")
                    }
                }

                // Botón DETENER (solo si está jugando)
                if (numeroTiempo == "1T" || numeroTiempo == "3T") {
                    Button(
                        onClick = onDetener,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(if (numeroTiempo == "1T") "FIN 1ER TIEMPO" else "FINALIZAR")
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════
            // BOTONES DE AJUSTE (solo si está jugando)
            // ═══════════════════════════════════════════════════════════
            if (numeroTiempo == "1T" || numeroTiempo == "3T") {
                Divider()

                Text(
                    text = "Ajustes de Tiempo",
                    style = MaterialTheme.typography.labelMedium
                )

                // Botones POSITIVOS (+)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AjusteButton("+1s", 1, onAjustar, Modifier.weight(1f))
                    AjusteButton("+10s", 10, onAjustar, Modifier.weight(1f))
                    AjusteButton("+30s", 30, onAjustar, Modifier.weight(1f))
                    AjusteButton("+1m", 60, onAjustar, Modifier.weight(1f))
                    AjusteButton("+5m", 300, onAjustar, Modifier.weight(1f))
                }

                // Botones NEGATIVOS (-)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AjusteButton("-1s", -1, onAjustar, Modifier.weight(1f))
                    AjusteButton("-10s", -10, onAjustar, Modifier.weight(1f))
                    AjusteButton("-30s", -30, onAjustar, Modifier.weight(1f))
                    AjusteButton("-1m", -60, onAjustar, Modifier.weight(1f))
                    AjusteButton("-5m", -300, onAjustar, Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Botón de ajuste de tiempo
 */
@Composable
private fun AjusteButton(
    texto: String,
    segundos: Int,
    onAjustar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { onAjustar(segundos) },
        modifier = modifier,
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = texto,
            fontSize = 10.sp
        )
    }
}