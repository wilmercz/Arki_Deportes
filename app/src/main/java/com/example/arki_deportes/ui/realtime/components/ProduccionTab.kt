package com.example.arki_deportes.ui.realtime.components

import androidx.compose.animation.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onForzarGanador: () -> Unit, // 👈 NUEVO PARÁMETRO
    modifier: Modifier = Modifier
) {
    var customLinea1 by remember { mutableStateOf("") }
    var customLinea2 by remember { mutableStateOf("") }

    // Estado para colapsar/expandir el equipo de producción
    var isEquipoExpanded by remember { mutableStateOf(false) }
// 👩 Estado para cambiar el género de las sugerencias rápidamente
    var isFemenino by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // 🎬 SECCIÓN 1: EQUIPO DE PRODUCCIÓN (COLAPSABLE)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Cabecera
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEquipoExpanded = !isEquipoExpanded }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icono estándar: Person (en lugar de Groups)
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "EQUIPO DE PRODUCCIÓN",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Iconos estándar: KeyboardArrowUp/Down
                        Icon(
                            imageVector = if (isEquipoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    // VISTA COLAPSADA: BOTONES RÁPIDOS
                    AnimatedVisibility(visible = !isEquipoExpanded) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSendText("NARRACIÓN | ${equipo.narrador}") },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp),
                                enabled = equipo.narrador.isNotBlank()
                            ) {
                                Text("🎙️ Narrador", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = { onSendText("COMENTARIOS | ${equipo.comentarista}") },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp),
                                enabled = equipo.comentarista.isNotBlank()
                            ) {
                                Text("📝 Coment.", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // VISTA EXPANDIDA: EDICIÓN E INTERCAMBIO
                    AnimatedVisibility(
                        visible = isEquipoExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProduccionInputField("Narrador", equipo.narrador, { onUpdateProduccion("narrador", it) }) {
                                if (equipo.narrador.isNotBlank()) onSendText("NARRACIÓN | ${equipo.narrador}")
                            }

                            // Botón Intercambiar usando icono estándar: Sync
                            IconButton(
                                onClick = {
                                    val n = equipo.narrador
                                    val c = equipo.comentarista
                                    onUpdateProduccion("narrador", c)
                                    onUpdateProduccion("comentarista", n)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Sync, "Intercambiar", tint = MaterialTheme.colorScheme.secondary)
                            }

                            ProduccionInputField("Comentarista", equipo.comentarista, { onUpdateProduccion("comentarista", it) }) {
                                if (equipo.comentarista.isNotBlank()) onSendText("COMENTARIOS | ${equipo.comentarista}")
                            }

                            Spacer(Modifier.height(8.dp))

                            ProduccionInputField("Borde de Campo", equipo.bordeCampo, { onUpdateProduccion("bordeCampo", it) }) {
                                if (equipo.bordeCampo.isNotBlank()) onSendText("BORDE DE CAMPO | ${equipo.bordeCampo}")
                            }
                        }
                    }
                }
            }
        }


       // item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // ✍️ SECCIÓN 2: TERCIO PERSONALIZADO (ETIQUETAS INTELIGENTES MULTINIVEL)
        item {
            // Categorización de sugerencias
            val sugerenciasData = remember(partido, isFemenino) {
                val sugFila1 = mutableListOf<String>()
                val sugFila2 = mutableListOf<String>()
                val sugOtros = mutableListOf<String>()

                val prefJug = if (isFemenino) "Jugadora" else "Jugador"
                val prefDT = if (isFemenino) "Técnica" else "DT"
                val prefFan = if (isFemenino) "Aficionada" else "Aficionado"

                partido?.let { p ->
                    val ganador = p.calcularGanadorFinal()
                    if (ganador != null) {
                        val perdedor = if (ganador.nombre == p.EQUIPO1) p.EQUIPO2 else p.EQUIPO1
                        // Fila 1: El que va ganando
                        sugFila1.add("$prefJug de ${ganador.nombre}")
                        sugFila1.add("$prefDT ${ganador.nombre}")
                        // Fila 2: El que va perdiendo
                        sugFila2.add("$prefJug de $perdedor")
                        sugFila2.add("$prefDT $perdedor")
                    } else {
                        // Sin ganador: Fila 1 = Equipo 1, Fila 2 = Equipo 2
                        sugFila1.add("$prefJug de ${p.EQUIPO1}")
                        sugFila1.add("$prefDT ${p.EQUIPO1}")
                        sugFila2.add("$prefJug de ${p.EQUIPO2}")
                        sugFila2.add("$prefDT ${p.EQUIPO2}")
                    }
                }
                sugOtros.add("Dirigente Deportivo")
                sugOtros.add(prefFan)
                sugOtros.add("Protagonista")

                Triple(sugFila1, sugFila2, sugOtros)
            }
            val (fila1, fila2, otros) = sugerenciasData

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TERCIO PERSONALIZADO", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isFemenino = !isFemenino }
                        ) {
                            Text(if (isFemenino) "👩 Femenino" else "👨 Masculino", style = MaterialTheme.typography.labelSmall)
                            Switch(
                                checked = isFemenino,
                                onCheckedChange = { isFemenino = it },
                                modifier = Modifier.height(24.dp).padding(start = 4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customLinea1,
                        onValueChange = { customLinea1 = it },
                        label = { Text("Línea 1 (Nombre)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customLinea2,
                        onValueChange = { customLinea2 = it },
                        label = { Text("Línea 2 (Rol/Equipo)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 🏷️ Sugerencias multinivel
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Fila 1: Equipo Ganador / Superior
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(fila1) { sug ->
                                SuggestionChip(
                                    onClick = { customLinea2 = sug },
                                    label = { Text(sug, fontSize = 10.sp) } // 👈 Letra reducida
                                )
                            }
                        }
                        // Fila 2: Equipo Perdedor / Inferior
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(fila2) { sug ->
                                SuggestionChip(
                                    onClick = { customLinea2 = sug },
                                    label = { Text(sug, fontSize = 10.sp) }
                                )
                            }
                        }
                        // Fila 3: Otros roles
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(otros) { sug ->
                                SuggestionChip(
                                    onClick = { customLinea2 = sug },
                                    label = { Text(sug, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (customLinea1.isNotBlank()) {
                                // 💡 Aplicamos .uppercase() a ambas líneas para normalizar
                                val linea1Mayus = customLinea1.uppercase().trim()
                                val linea2Mayus = customLinea2.uppercase().trim()

                                val txt = if (linea2Mayus.isNotBlank()) "$linea1Mayus | $linea2Mayus" else linea1Mayus

                                onSendText(txt)

                                // Limpiamos cajas
                                customLinea1 = ""
                                customLinea2 = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = customLinea1.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ENVIAR Y GUARDAR")
                    }
                }
            }
        }

        // --- SECCIÓN CONTROL DE RESULTADOS (Siempre visible para emergencias) ---
        item {
            Text("CONTROL DE RESULTADOS", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFF9800))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Solo mostramos el botón de cintillo si ya hay un ganador calculado
                    val ganador = partido?.calcularGanadorFinal()
                    if (ganador != null) {
                        Button(
                            onClick = {
                                val texto = if (partido.ETAPA == 3) "¡FELICIDADES CAMPEÓN ${ganador.nombre.uppercase()}! | $nombreCampeonato"
                                else "¡FELICIDADES ${ganador.nombre.uppercase()}! | Marcador Final"
                                onSendText(texto)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                        ) {
                            Text("🏆 ENVIAR CINTILLO DE GANADOR")
                        }
                    }

                    // Botón para FORZAR el cálculo (Útil si el partido no se cerró bien)
                    OutlinedButton(
                        onClick = onForzarGanador,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Calculate, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("FORZAR CÁLCULO DE GANADOR")
                    }
                    Text(
                        "Usa esto si el partido terminó pero el sistema no asignó al ganador en la base de datos.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 📜 SECCIÓN 3: LISTA DE TEXTOS (HISTORIAL Y SUGERIDOS)
        val historial = partido?.HISTORIAL_TEXTOS ?: emptyList()
        if (historial.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "HISTORIAL / SUGERIDOS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            items(historial) { texto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    onClick = { onSendText(texto) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val partes = texto.split("|")
                        Column(modifier = Modifier.weight(1f)) {
                            Text(partes[0].trim(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (partes.size > 1) {
                                Text(partes[1].trim(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Lanzar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

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
