package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.EquipoProduccion

/**
 * Tab de Producción
 * Gestión de créditos de personal y mensajes rápidos (Textos predefinidos)
 */
@Composable
fun ProduccionTab(
    equipo: EquipoProduccion,
    textosPredefinidos: List<String>,
    onUpdateProduccion: (String, String) -> Unit,
    onSendText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECCIÓN: EQUIPO DE PRODUCCIÓN ---
        item {
            Text(
                "EQUIPO DE PRODUCCIÓN (TERCIOS)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProduccionInputField(
                    label = "Narrador",
                    value = equipo.narrador,
                    onValueChange = { onUpdateProduccion("narrador", it) },
                    onSend = { onSendText("NARRACIÓN: ${equipo.narrador.uppercase()}") }
                )
                ProduccionInputField(
                    label = "Comentarista",
                    value = equipo.comentarista,
                    onValueChange = { onUpdateProduccion("comentarista", it) },
                    onSend = { onSendText("COMENTARIOS: ${equipo.comentarista.uppercase()}") }
                )
                ProduccionInputField(
                    label = "Borde de Campo",
                    value = equipo.bordeCampo,
                    onValueChange = { onUpdateProduccion("bordeCampo", it) },
                    onSend = { onSendText("BORDE DE CAMPO: ${equipo.bordeCampo.uppercase()}") }
                )
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- SECCIÓN: TEXTOS PREDEFINIDOS ---
        item {
            Text(
                "MENSAJES RÁPIDOS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(textosPredefinidos) { texto ->
            OutlinedCard(
                onClick = { onSendText(texto) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(texto, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
