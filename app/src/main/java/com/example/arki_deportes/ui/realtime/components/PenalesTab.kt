// ui/realtime/components/PenalesTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.data.model.Partido

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PENALES TAB - CONTROL DE TANDA DE PENALES (VERSIÓN MEJORADA V2.0)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Implementación completa del módulo de tanda de penales según VB.NET
 *
 * MEJORAS V2.0:
 * - ✅ Usa List<Int> en vez de List<String> (1=gol, 0=fallo)
 * - ✅ Separa "equipo que INICIA" de "turno actual"
 * - ✅ AlertDialog para todas las acciones críticas
 * - ✅ Mejor estructura en Firebase (MARCADOR_PENALES explícito)
 * - ✅ Contador de tandas para muerte súbita
 * - ✅ Confirmación antes de activar/desactivar/nueva tanda
 *
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
@Composable
fun PenalesTab(
    partido: Partido,
    penalesActivos: Boolean,
    equipoQueInicia: Int, // 1 o 2 - ¿Quién inició la tanda? (permanente)
    equipoEnTurno: Int, // 1 o 2 - ¿Quién cobra AHORA? (alterna)
    tandaActual: Int, // Número de tanda (1, 2, 3... para muerte súbita)
    historiaPenales1: List<Int>, // [1, 0, 1, ...] donde 1=gol, 0=fallo
    historiaPenales2: List<Int>, // [1, 1, 0, ...] donde 1=gol, 0=fallo
    onActivarPenales: (equipoInicia: Int) -> Unit, // Requiere elegir quién inicia
    onDesactivarPenales: () -> Unit,
    onCambiarEquipoInicia: (Int) -> Unit, // Cambiar quién inició (corrección)
    onCambiarTurno: (Int) -> Unit, // Cambiar turno manual (corrección)
    onAnotarGol: () -> Unit,
    onAnotarFallo: () -> Unit,
    onAgregarPenalEquipo1: () -> Unit,
    onRestarPenalEquipo1: () -> Unit,
    onAgregarPenalEquipo2: () -> Unit,
    onRestarPenalEquipo2: () -> Unit,
    onNuevaTanda: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ═══════════════════════════════════════════════════════════════════════
    // ESTADOS LOCALES
    // ═══════════════════════════════════════════════════════════════════════
    var mostrarDialogActivar by remember { mutableStateOf(false) }
    var mostrarDialogDesactivar by remember { mutableStateOf(false) }
    var mostrarDialogNuevaTanda by remember { mutableStateOf(false) }
    var equipoSeleccionadoParaIniciar by remember { mutableStateOf(equipoQueInicia.coerceIn(1, 2)) }

    // ═══════════════════════════════════════════════════════════════════════
    // DIÁLOGOS DE CONFIRMACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    // Dialog: Activar penales (con selector de equipo que inicia)
    if (mostrarDialogActivar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogActivar = false },
            title = { Text("🎯 Activar Tanda de Penales") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("¿Seguro que deseas activar el modo tanda de penales?")
                    
                    Text(
                        text = "⚠️ El overlay web cambiará automáticamente para mostrar el panel de penales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Divider()
                    
                    Text(
                        text = "Selecciona qué equipo INICIA la tanda:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    // Radio buttons dentro del diálogo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Opción Equipo 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = equipoSeleccionadoParaIniciar == 1,
                                    onClick = { equipoSeleccionadoParaIniciar = 1 }
                                )
                                .background(
                                    if (equipoSeleccionadoParaIniciar == 1)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else
                                        Color.Transparent
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = equipoSeleccionadoParaIniciar == 1,
                                onClick = { equipoSeleccionadoParaIniciar = 1 }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = partido.EQUIPO1.ifBlank { "Equipo 1" },
                                fontWeight = if (equipoSeleccionadoParaIniciar == 1) 
                                    FontWeight.Bold 
                                else 
                                    FontWeight.Normal
                            )
                        }

                        // Opción Equipo 2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = equipoSeleccionadoParaIniciar == 2,
                                    onClick = { equipoSeleccionadoParaIniciar = 2 }
                                )
                                .background(
                                    if (equipoSeleccionadoParaIniciar == 2)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else
                                        Color.Transparent
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = equipoSeleccionadoParaIniciar == 2,
                                onClick = { equipoSeleccionadoParaIniciar = 2 }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = partido.EQUIPO2.ifBlank { "Equipo 2" },
                                fontWeight = if (equipoSeleccionadoParaIniciar == 2) 
                                    FontWeight.Bold 
                                else 
                                    FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onActivarPenales(equipoSeleccionadoParaIniciar)
                        mostrarDialogActivar = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("✅ ACTIVAR PENALES")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogActivar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Desactivar penales
    if (mostrarDialogDesactivar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogDesactivar = false },
            title = { Text("⚠️ Desactivar Penales") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("¿Seguro que deseas desactivar el modo penales?")
                    
                    Text(
                        text = "⚠️ El overlay volverá a mostrar el marcador normal del partido.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Text(
                        text = "💡 Los datos de penales se mantendrán guardados en Firebase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDesactivarPenales()
                        mostrarDialogDesactivar = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("SÍ, DESACTIVAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogDesactivar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Nueva tanda (muerte súbita)
    if (mostrarDialogNuevaTanda) {
        AlertDialog(
            onDismissRequest = { mostrarDialogNuevaTanda = false },
            title = { Text("🔄 Nueva Tanda (Muerte Súbita)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "El marcador está empatado en penales.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = "¿Deseas iniciar una nueva tanda de muerte súbita?",
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                    
                    Text("✅ Los contadores (${partido.PENALES1}-${partido.PENALES2}) NO se resetearán")
                    Text("✅ Solo se limpiará el historial visual de esta tanda")
                    Text("✅ Comenzará el equipo que inició la primera tanda")
                    
                    Text(
                        text = "📊 Tanda actual: #$tandaActual → Nueva tanda: #${tandaActual + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onNuevaTanda()
                        mostrarDialogNuevaTanda = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("🔄 INICIAR TANDA #${tandaActual + 1}")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogNuevaTanda = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI PRINCIPAL
    // ═══════════════════════════════════════════════════════════════════════
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ═══════════════════════════════════════════════════════════
        // HEADER
        // ═══════════════════════════════════════════════════════════
        Text(
            text = "🎯 TANDA DE PENALES",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Divider()

        // ═══════════════════════════════════════════════════════════
        // ACTIVACIÓN DE MODO PENALES
        // ═══════════════════════════════════════════════════════════
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (penalesActivos)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (penalesActivos) "✅ Modo Penales ACTIVO" else "⚪ Modo Penales INACTIVO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (penalesActivos)
                                "El overlay está mostrando el panel de penales"
                            else
                                "Activa este modo cuando el partido vaya a penales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = penalesActivos,
                        onCheckedChange = {
                            if (it) {
                                // Mostrar diálogo para activar
                                mostrarDialogActivar = true
                            } else {
                                // Mostrar diálogo para desactivar
                                mostrarDialogDesactivar = true
                            }
                        }
                    )
                }

                if (penalesActivos && tandaActual > 1) {
                    Divider()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "MUERTE SÚBITA - Tanda #$tandaActual",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (!penalesActivos) {
                    Divider()
                    Text(
                        text = "💡 Al activar, seleccionarás qué equipo inicia la tanda y el overlay web cambiará automáticamente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // CONTROLES SOLO VISIBLES SI PENALES ESTÁN ACTIVOS
        // ═══════════════════════════════════════════════════════════
        if (penalesActivos) {
            // ═══════════════════════════════════════════════════════
            // INFORMACIÓN: EQUIPO QUE INICIÓ
            // ═══════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📋 Equipo que INICIÓ la tanda:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = if (equipoQueInicia == 1)
                            "⚽ ${partido.EQUIPO1.ifBlank { "Equipo 1" }}"
                        else
                            "⚽ ${partido.EQUIPO2.ifBlank { "Equipo 2" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Botón para cambiar (corrección)
                    TextButton(
                        onClick = {
                            val nuevoEquipo = if (equipoQueInicia == 1) 2 else 1
                            onCambiarEquipoInicia(nuevoEquipo)
                        }
                    ) {
                        Text("🔄 Cambiar equipo inicial (si fue error)")
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // TURNO ACTUAL + BOTONES GOL/FALLO
            // ═══════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "TURNO ACTUAL:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Text(
                        text = if (equipoEnTurno == 1)
                            partido.EQUIPO1.ifBlank { "Equipo 1" }.uppercase()
                        else
                            partido.EQUIPO2.ifBlank { "Equipo 2" }.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Divider()

                    Text(
                        text = "Registra el resultado del penal:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Botones GOL y FALLO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Botón GOL (Verde oscuro)
                        Button(
                            onClick = onAnotarGol,
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B5E20) // Verde oscuro
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "✅",
                                    fontSize = 32.sp
                                )
                                Text(
                                    text = "GOL",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Botón FALLO (Rojo oscuro)
                        Button(
                            onClick = onAnotarFallo,
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB71C1C) // Rojo oscuro
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "❌",
                                    fontSize = 32.sp
                                )
                                Text(
                                    text = "FALLO",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "⚠️ El turno cambiará automáticamente al otro equipo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    // Botón cambiar turno manualmente (corrección)
                    TextButton(
                        onClick = {
                            val nuevoTurno = if (equipoEnTurno == 1) 2 else 1
                            onCambiarTurno(nuevoTurno)
                        }
                    ) {
                        Text("🔄 Cambiar turno manualmente")
                    }
                }
            }

            Divider()

            // ═══════════════════════════════════════════════════════
            // MARCADOR EQUIPO 1
            // ═══════════════════════════════════════════════════════
            MarcadorPenalesEquipo(
                nombreEquipo = partido.EQUIPO1.ifBlank { "Equipo 1" },
                penales = partido.PENALES1,
                historia = historiaPenales1,
                onAgregar = onAgregarPenalEquipo1,
                onRestar = onRestarPenalEquipo1,
                colorFondo = MaterialTheme.colorScheme.primaryContainer
            )

            // ═══════════════════════════════════════════════════════
            // MARCADOR EQUIPO 2
            // ═══════════════════════════════════════════════════════
            MarcadorPenalesEquipo(
                nombreEquipo = partido.EQUIPO2.ifBlank { "Equipo 2" },
                penales = partido.PENALES2,
                historia = historiaPenales2,
                onAgregar = onAgregarPenalEquipo2,
                onRestar = onRestarPenalEquipo2,
                colorFondo = MaterialTheme.colorScheme.secondaryContainer
            )

            // ═══════════════════════════════════════════════════════
            // BOTÓN NUEVA TANDA
            // ═══════════════════════════════════════════════════════
            val tirosEquipo1 = historiaPenales1.size
            val tirosEquipo2 = historiaPenales2.size

            // Mostrar botón solo si ambos completaron 5 tiros
            if (tirosEquipo1 >= 5 && tirosEquipo2 >= 5) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠️ TANDA COMPLETA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (partido.PENALES1 == partido.PENALES2) {
                            Text(
                                text = "Empate ${partido.PENALES1} - ${partido.PENALES2}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "Deben continuar con muerte súbita",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )

                            Button(
                                onClick = { mostrarDialogNuevaTanda = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(
                                    text = "🔄 NUEVA TANDA (Muerte Súbita)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            val ganador = if (partido.PENALES1 > partido.PENALES2)
                                partido.EQUIPO1.ifBlank { "Equipo 1" }
                            else
                                partido.EQUIPO2.ifBlank { "Equipo 2" }

                            Text(
                                text = "🏆 GANADOR: $ganador",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Marcador: ${partido.PENALES1} - ${partido.PENALES2}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // INFORMACIÓN ADICIONAL
            // ═══════════════════════════════════════════════════════
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ℹ️ Instrucciones:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    InfoItem("1. El equipo en turno está marcado arriba")
                    InfoItem("2. Presiona GOL ✅ o FALLO ❌ según el resultado")
                    InfoItem("3. El turno cambiará automáticamente")
                    InfoItem("4. Usa los botones +/- para corregir errores")
                    InfoItem("5. Al completar 5 tiros, se decidirá el ganador")
                    InfoItem("6. Si hay empate, continúa con muerte súbita")
                }
            }
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MARCADOR DE PENALES POR EQUIPO (VERSIÓN MEJORADA)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun MarcadorPenalesEquipo(
    nombreEquipo: String,
    penales: Int,
    historia: List<Int>, // 1=gol, 0=fallo
    onAgregar: () -> Unit,
    onRestar: () -> Unit,
    colorFondo: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con nombre y marcador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombreEquipo.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "$penales",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "${historia.size} tiros cobrados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // Visualización gráfica de los tiros
            Text(
                text = "Historial de tiros:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            VisualizacionTiros(historia)

            Divider()

            // Controles de corrección manual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Corrección manual:",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onRestar,
                        enabled = penales > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Restar penal",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "$penales",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = onAgregar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar penal",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * VISUALIZACIÓN GRÁFICA DE LOS TIROS (VERSIÓN MEJORADA CON List<Int>)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun VisualizacionTiros(historia: List<Int>) {
    // Mostrar hasta 10 tiros (5 regulares + 5 muerte súbita)
    val maxTiros = 10

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxTiros) {
            val resultado = historia.getOrNull(i)

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (resultado) {
                    1 -> {
                        // GOL
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(
                                color = Color(0xFF1B5E20) // Verde oscuro
                            )
                        }
                    }
                    0 -> {
                        // FALLO
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(
                                color = Color(0xFFB71C1C) // Rojo oscuro
                            )
                        }
                    }
                    null -> {
                        // Pendiente
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(
                                color = Color.LightGray.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Separador cada 5 tiros
            if (i == 4 && historia.size > 5) {
                Divider(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // Leyenda
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeyendaItem("🟢 Gol", Color(0xFF1B5E20))
        LeyendaItem("🔴 Fallo", Color(0xFFB71C1C))
        LeyendaItem("⚪ Pendiente", Color.LightGray)
    }
}

/**
 * Item de leyenda
 */
@Composable
private fun LeyendaItem(texto: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Item de información
 */
@Composable
private fun InfoItem(texto: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
