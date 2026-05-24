package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.TablaPosicionesItem

@Composable
fun TablaPosicionesTab(
    tabla: List<TablaPosicionesItem>,
    mostrarEnWeb: Boolean,
    onToggleWeb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val azulArki = Color(0xFF2E5BBA)
    val naranjaArki = Color(0xFFFF8A3D)

    Column(modifier = modifier.fillMaxSize().padding(8.dp)) {
        // --- CONTROL DE VISIBILIDAD ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = azulArki.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Producción de Tabla", color = azulArki, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (mostrarEnWeb) "EN AIRE (Overlay Web)" else "OCULTO (Solo App)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (mostrarEnWeb) naranjaArki else Color.Gray
                    )
                }
                Switch(
                    checked = mostrarEnWeb, 
                    onCheckedChange = { onToggleWeb() },
                    colors = SwitchDefaults.colors(checkedThumbColor = naranjaArki, checkedTrackColor = naranjaArki.copy(alpha = 0.5f))
                )
            }
        }

        // --- ENCABEZADO ESTILO PGM ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(azulArki)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", Modifier.width(25.dp), color = Color.White, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("EQUIPO", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            
            // Stats headers
            listOf("PJ", "GF", "GC", "DG").forEach {
                Text(it, Modifier.width(35.dp), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            }
            // PTS destacado
            Surface(color = naranjaArki, shape = MaterialTheme.shapes.extraSmall) {
                Text("PTS", Modifier.width(40.dp).padding(vertical = 2.dp), color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
            }
        }

        // --- CUERPO DE LA TABLA ---
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(tabla) { index, item ->
                val bgColor = if (index % 2 == 0) Color.White else Color(0xFFF5F7FA)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}", Modifier.width(25.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    Text(
                        text = item.EQUIPO_NOMBRE.uppercase(),
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                        color = azulArki,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text("${item.PJ}", Modifier.width(35.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Text("${item.GF}", Modifier.width(35.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Text("${item.GC}", Modifier.width(35.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    
                    Text(
                        text = (if(item.DG > 0) "+" else "") + "${item.DG}",
                        modifier = Modifier.width(35.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.DG < 0) Color.Red else if(item.DG > 0) Color(0xFF2E7D32) else Color.Unspecified
                    )
                    
                    // Columna de Puntos con fondo naranja suave o texto naranja
                    Text(
                        text = "${item.PTS}",
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = naranjaArki
                    )
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}
