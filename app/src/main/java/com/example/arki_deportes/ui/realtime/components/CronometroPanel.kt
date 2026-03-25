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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    partido: Partido,
    estaPausado: Boolean, // 👈 Nuevo parámetro
    onIniciar: () -> Unit,
    onDetener: () -> Unit,
    onReiniciar: () -> Unit,
    onTogglePausa: () -> Unit, // 👈 Nuevo parámetro
    onAjustar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Log cuando se recompone
    Log.d("CronometroPanel", "🔄 Recomposición - Tiempo: $tiempoActual - Pausado: $estaPausado")

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

                // Solo mostrar tab de ajustes si está jugando o pausado
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
                    0 -> TabCronometro(tiempoActual, numeroTiempo, partido.TRANSMISION, estaPausado)
                    1 -> TabControles(numeroTiempo, estaPausado, onIniciar, onDetener, onReiniciar, onTogglePausa, partido)
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
private fun TabCronometro(
    tiempoActual: String,
    numeroTiempo: String,
    modoTransmision: Boolean,
    estaPausado: Boolean
) {

    // ✅ Determinar si está corriendo (activo pero no pausado)
    val estaActivo = numeroTiempo == "1T" || numeroTiempo == "3T"
    val estaCorriendo = estaActivo && !estaPausado

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
            } else if (estaPausado) {
                Icon(Icons.Default.Pause, contentDescription = "Pausado", tint = Color.Yellow, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
            }

            Text(
                text = tiempoActual,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = if (estaCorriendo)
                    MaterialTheme.colorScheme.primary // Verde/Azul cuando corre
                else if (estaPausado)
                    Color.Yellow
                else
                    MaterialTheme.colorScheme.onSurfaceVariant // Gris cuando está detenido
            )
        }

        // ═══════════════════════════════════════════════════════════
        // ESTADO DEL PARTIDO - MINIMALISTA
        // ═══════════════════════════════════════════════════════════
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // ⬅️ ESTADO DEL TIEMPO
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        estaPausado -> "⏸️"
                        numeroTiempo == "0T" -> "⏹️"
                        numeroTiempo == "1T" -> "▶️"
                        numeroTiempo == "2T" -> "☕"
                        numeroTiempo == "3T" -> "▶️"
                        numeroTiempo == "4T" -> "✅"
                        else -> "•"
                    },
                    fontSize = 12.sp
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = when {
                        estaPausado -> "Pausado"
                        numeroTiempo == "0T" -> "No iniciado"
                        numeroTiempo == "1T" -> "Primer Tiempo"
                        numeroTiempo == "2T" -> "Descanso"
                        numeroTiempo == "3T" -> "Segundo Tiempo"
                        numeroTiempo == "4T" -> "Finalizado"
                        else -> numeroTiempo
                    },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }

            // ➡️ ESTADO OVERLAY WEB (VISUAL)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (modoTransmision) "🌐 CameraFi Overlay ON" else "🚫 CameraFi Overlay OFF",
                    fontSize = 10.sp,
                    color = if (modoTransmision)
                        Color(0xFF2E7D32) // verde oscuro
                    else
                        Color(0xFFB71C1C), // rojo oscuro
                    fontWeight = FontWeight.Medium
                )
            }
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
// TAB 2: CONTROLES (INICIAR/DETENER/PAUSA)
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun TabControles(
    numeroTiempo: String,
    estaPausado: Boolean,
    onIniciar: () -> Unit,
    onDetener: () -> Unit,
    onReiniciar: () -> Unit,
    onTogglePausa: () -> Unit,
    partido: Partido
) {
    val esBasquet = partido.DEPORTE.equals("BASQUET", ignoreCase = true)

    // Tiempos donde el cronómetro está DETENIDO (Esperando para INICIAR)
    val tiemposParaIniciar = if (esBasquet) {
        listOf("0T", "2T", "4T", "6T", "8T", "10T", "12T")
    } else {
        listOf("0T", "2T")
    }

    // Tiempos donde el cronómetro está CORRIENDO o PAUSADO
    val tiemposActivos = if (esBasquet) {
        listOf("1T", "3T", "5T", "7T", "9T", "11T", "13T")
    } else {
        listOf("1T", "3T")
    }

    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("¿Reiniciar partido?") },
            text = { Text("Esto dejará el partido como NO INICIADO y borrará FECHA_PLAY/Cronometro. ¿Seguro?") },
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
        // Botón Reiniciar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showConfirm = true }) {
                Text("🔄 Reiniciar", fontSize = 12.sp)
            }
        }

        // LÓGICA DINÁMICA DE BOTONES
        when {
            // CASO 1: PARTIDO FINALIZADO (Fútbol 4T o Estado 1)
            partido.estaFinalizado() -> {
                Text(
                    text = "✅ Partido finalizado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // CASO 2: EL CRONÓMETRO ESTÁ DETENIDO (Mostrar botón INICIAR)
            numeroTiempo in tiemposParaIniciar -> {
                val textoBoton = when (numeroTiempo) {
                    "0T" -> "▶️ INICIAR PARTIDO"
                    "2T" -> if (esBasquet) "▶️ INICIAR 2DO PERIODO" else "▶️ INICIAR 2DO TIEMPO"
                    "4T" -> "▶️ INICIAR 3ER PERIODO"
                    "6T" -> "▶️ INICIAR 4TO PERIODO"
                    "8T" -> "▶️ INICIAR 1ER TIEMPO EXTRA"
                    "10T" -> "▶️ INICIAR 2DO TIEMPO EXTRA"
                    "12T" -> "▶️ INICIAR 3ER TIEMPO EXTRA"
                    else -> "▶️ CONTINUAR PARTIDO"
                }

                Text(
                    text = "Cronómetro detenido",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onIniciar,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(textoBoton, fontSize = 16.sp)
                }
            }

            // CASO 3: EL CRONÓMETRO ESTÁ ACTIVO (Mostrar PAUSA y DETENER)
            numeroTiempo in tiemposActivos -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón PAUSA / REANUDAR
                    Button(
                        onClick = onTogglePausa,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (estaPausado) Color(0xFF4CAF50) else Color(0xFFFFC107)
                        )
                    ) {
                        Icon(if (estaPausado) Icons.Default.PlayArrow else Icons.Default.Pause, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (estaPausado) "REANUDAR" else "PAUSAR")
                    }

                    // Botón DETENER/FINALIZAR PERIODO
                    val textoBotonDetener = when (numeroTiempo) {
                        "1T" -> if (esBasquet) "⏸️ FIN 1ER PER" else "⏸️ FIN 1ER TIEMPO"
                        "3T" -> if (esBasquet) "⏸️ FIN 2DO PER" else "🏁 FINALIZAR"
                        "5T" -> "⏸️ FIN 3ER PER"
                        "7T" -> "⏸️ FIN 4TO PER"
                        else -> "⏸️ DETENER"
                    }

                    Button(
                        onClick = onDetener,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(textoBotonDetener, fontSize = 12.sp)
                    }
                }
            }

            // CASO 4: OTROS ESTADOS
            else -> {
                Text("Estado: $numeroTiempo")
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
