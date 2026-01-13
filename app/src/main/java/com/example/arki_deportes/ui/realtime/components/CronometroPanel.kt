// ui/realtime/components/CronometroPanel.kt


package com.example.arki_deportes.ui.realtime.components
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import com.example.arki_deportes.data.model.Partido
/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CRONÓMETRO PANEL - CON TABS PARA AHORRAR ESPACIO
 * ═══════════════════════════════════════════════════════════════════════════
 */
// ui/realtime/components/CronometroPanel.kt

@Composable
fun CronometroPanel(
    tiempoActual: String,
    partido: Partido,  // ← Recibir el partido completo
    onIniciar: () -> Unit,
    onDetener: () -> Unit,
    onReiniciar: () -> Unit,
    onAjustar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Log cuando se recompone
    Log.d("CronometroPanel", "🔄 Recomposición - Tiempo: $tiempoActual")

    // ✅ Usar el método efectivo
    val numeroTiempo = partido.getNumeroDeTiempoEfectivo()

    var selectedTab by remember { mutableStateOf(0) }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("⏱️ Cronómetro") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🎮 Controles") }
                )

                // Solo mostrar tab de ajustes si está jugando
                if (numeroTiempo == "1T" || numeroTiempo == "3T") {
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("⚙️ Ajustes") }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                when (selectedTab) {
                    0 -> TabCronometro(tiempoActual, numeroTiempo)
                    1 -> TabControles(numeroTiempo, onIniciar, onDetener, onReiniciar)
                    2 -> TabAjustes(onAjustar)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TAB 1: VISUALIZACIÓN DEL CRONÓMETRO
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun TabCronometro(tiempoActual: String, numeroTiempo: String) {
    // ✅ Determinar si está corriendo
    val estaCorriendo = numeroTiempo == "1T" || numeroTiempo == "3T"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ═══════════════════════════════════════════════════════════
        // TIEMPO GRANDE CON INDICADOR DE ESTADO
        // ═══════════════════════════════════════════════════════════
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // ✅ Indicador pulsante cuando está corriendo
            if (estaCorriendo) {
                IndicadorPulsante()
                Spacer(Modifier.width(16.dp))
            }

            Text(
                text = tiempoActual,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = if (estaCorriendo)
                    MaterialTheme.colorScheme.primary // Verde/Azul cuando corre
                else
                    MaterialTheme.colorScheme.onSurfaceVariant // Gris cuando está detenido
            )
        }

        // ═══════════════════════════════════════════════════════════
        // ESTADO DEL PARTIDO - MINIMALISTA (NO PARECE BOTÓN)
        // ═══════════════════════════════════════════════════════════
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Solo texto simple con icono
            Text(
                text = when (numeroTiempo) {
                    "0T" -> "⏸️"
                    "1T" -> "▶️"
                    "2T" -> "☕"
                    "3T" -> "▶️"
                    "4T" -> "✅"
                    else -> "•"
                },
                fontSize = 12.sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = when (numeroTiempo) {
                    "0T" -> "No iniciado"
                    "1T" -> "Primer Tiempo"
                    "2T" -> "Descanso"
                    "3T" -> "Segundo Tiempo"
                    "4T" -> "Finalizado"
                    else -> numeroTiempo
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal // ← Menos peso = menos parecido a botón
            )
        }
    }
}

/**
 * Indicador visual pulsante para cuando el cronómetro está corriendo
 */
@Composable
private fun IndicadorPulsante() {
    // ✅ Animación infinita de pulso
    val infiniteTransition = rememberInfiniteTransition(label = "pulso")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    Box(
        modifier = Modifier.size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Círculo pulsante
        Canvas(modifier = Modifier.size(16.dp)) {
            drawCircle(
                color = Color(0xFF4CAF50), // Verde success
                radius = size.minDimension / 2 * scale
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// TAB 2: CONTROLES (INICIAR/DETENER)
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun TabControles(
    numeroTiempo: String,
    onIniciar: () -> Unit,
    onDetener: () -> Unit,
    onReiniciar: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("¿Reiniciar partido?") },
            text = {
                Text("Esto dejará el partido como NO INICIADO y borrará FECHA_PLAY/Cronometro. ¿Seguro?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onReiniciar()
                }) { Text("Sí, reiniciar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ✅ Botón pequeño visible cuando NO es 0T y NO es 4T
        //if (numeroTiempo != "0T" && numeroTiempo != "4T") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showConfirm = true }) {
                    Text("🔄 Reiniciar", fontSize = 12.sp)
                }
            }
        //}

        when (numeroTiempo) {
            "0T" -> {
                // No iniciado
                Text(
                    text = "Partido listo para iniciar",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onIniciar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("▶️ INICIAR PRIMER TIEMPO", fontSize = 16.sp)
                }
            }

            "1T" -> {
                // Primer tiempo jugando
                Text(
                    text = "Primer tiempo en curso",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onDetener,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("⏸️ FINALIZAR PRIMER TIEMPO", fontSize = 16.sp)
                }
            }

            "2T" -> {
                // Descanso
                Text(
                    text = "Medio tiempo - Descanso",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Button(
                    onClick = onIniciar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("▶️ INICIAR SEGUNDO TIEMPO", fontSize = 16.sp)
                }
            }

            "3T" -> {
                // Segundo tiempo jugando
                Text(
                    text = "Segundo tiempo en curso",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onDetener,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("🏁 FINALIZAR PARTIDO", fontSize = 16.sp)
                }
            }

            "4T" -> {
                // Finalizado
                Text(
                    text = "✅ Partido finalizado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TAB 3: AJUSTES MANUALES
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun TabAjustes(onAjustar: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Advertencia
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚠️", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ajustes Manuales",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Usar solo para corregir errores del cronómetro",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Text(
            text = "➕ Añadir tiempo",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        // Botones POSITIVOS
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AjusteButton("+1s", 1, onAjustar, Modifier.weight(1f), isPositive = true)
            AjusteButton("+10s", 10, onAjustar, Modifier.weight(1f), isPositive = true)
            AjusteButton("+30s", 30, onAjustar, Modifier.weight(1f), isPositive = true)
            AjusteButton("+1m", 60, onAjustar, Modifier.weight(1f), isPositive = true)
            AjusteButton("+5m", 300, onAjustar, Modifier.weight(1f), isPositive = true)
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "➖ Restar tiempo",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        // Botones NEGATIVOS
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AjusteButton("-1s", -1, onAjustar, Modifier.weight(1f), isPositive = false)
            AjusteButton("-10s", -10, onAjustar, Modifier.weight(1f), isPositive = false)
            AjusteButton("-30s", -30, onAjustar, Modifier.weight(1f), isPositive = false)
            AjusteButton("-1m", -60, onAjustar, Modifier.weight(1f), isPositive = false)
            AjusteButton("-5m", -300, onAjustar, Modifier.weight(1f), isPositive = false)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// BOTÓN DE AJUSTE MEJORADO
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun AjusteButton(
    texto: String,
    segundos: Int,
    onAjustar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isPositive: Boolean
) {
    Button(
        onClick = { onAjustar(segundos) },
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPositive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

