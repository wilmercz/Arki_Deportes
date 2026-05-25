package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.EquipoProduccion
import com.example.arki_deportes.data.model.Partido

/**
 * Tab de Producción Rediseñado
 * Permite ingresar manualmente nombres y roles para tercios inferiores.
 * Genera automáticamente textos de campeones al finalizar el partido.
 */
@Composable
fun ProduccionTab(
    equipo: EquipoProduccion,
    partido: Partido?,
    nombreCampeonato: String,
    onUpdateProduccion: (String, String) -> Unit,
    onSendText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customLinea1 by remember { mutableStateOf("") }
    var customLinea2 by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🎬 SECCIÓN 1: EQUIPO DE PRODUCCIÓN (CRÉDITOS RÁPIDOS)
        item {
            Text(
                "EQUIPO DE PRODUCCIÓN",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProduccionInputField("Narrador", equipo.narrador, { onUpdateProduccion("narrador", it) }) {
                    if (equipo.narrador.isNotBlank()) onSendText("NARRACIÓN | ${equipo.narrador}")
                }
                ProduccionInputField("Comentarista", equipo.comentarista, { onUpdateProduccion("comentarista", it) }) {
                    if (equipo.comentarista.isNotBlank()) onSendText("COMENTARIOS | ${equipo.comentarista}")
                }
                ProduccionInputField("Borde de Campo", equipo.bordeCampo, { onUpdateProduccion("bordeCampo", it) }) {
                    if (equipo.bordeCampo.isNotBlank()) onSendText("BORDE DE CAMPO | ${equipo.bordeCampo}")
                }
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // ✍️ SECCIÓN 2: GENERADOR DE TERCIOS PERSONALIZADO (JUGADORES, ETC)
        item {
            Text(
                "TERCIO PERSONALIZADO",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customLinea1,
                        onValueChange = { customLinea1 = it },
                        label = { Text("Línea 1 (Nombre)") },
                        placeholder = { Text("Ej: Jose Peralta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customLinea2,
                        onValueChange = { customLinea2 = it },
                        label = { Text("Línea 2 (Rol/Equipo)") },
                        placeholder = { Text("Ej: Jugador de Equipo A") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (customLinea1.isNotBlank()) {
                                // Formato exacto para la web: Línea 1 | Línea 2
                                val textoFinal = if (customLinea2.isNotBlank()) "$customLinea1 | $customLinea2" else customLinea1
                                onSendText(textoFinal)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = customLinea1.isNotBlank()
                    ) {
                        Text("ENVIAR TERCIO")
                    }
                }
            }
        }

        // --- SECCIÓN PREMIACIÓN (Solo si el partido terminó) ---
        if (partido?.estaFinalizado() == true) {
            item { Text("PREMIACIÓN", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFF9800)) }
            item {
                val ganador = when {
                    partido.GOLES1 > partido.GOLES2 -> partido.EQUIPO1
                    partido.GOLES2 > partido.GOLES1 -> partido.EQUIPO2
                    else -> null
                }
                if (ganador != null) {
                    Button(
                        onClick = { onSendText("¡FELICIDADES CAMPEÓN ${ganador.uppercase()}! | $nombreCampeonato") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                    ) {
                        Text("ENVIAR CINTILLO DE CAMPEÓN")
                    }
                }
            }
        }

        /* 
        // SECCIÓN COMENTADA: Ya no usamos los textos del catálogo aquí
        item { Text("MENSAJES RÁPIDOS", style = MaterialTheme.typography.titleMedium) }
        */
        
        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
fun ProduccionInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        IconButton(
            onClick = onSend,
            modifier = Modifier.padding(top = 8.dp),
            colors = IconButtonDefaults.filledIconButtonColors()
        ) {
            Icon(Icons.Default.Send, contentDescription = "Enviar a Overlay")
        }
    }
}
