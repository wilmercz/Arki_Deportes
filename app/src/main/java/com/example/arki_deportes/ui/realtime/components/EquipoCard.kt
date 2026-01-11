// ui/realtime/components/EquipoCard.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EQUIPO CARD
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tarjeta que muestra un equipo y sus estadísticas con botones +/-
 *
 * VB.NET Equivalente: Panel izquierdo/derecho de FrmControl
 */
@Composable
fun EquipoCard(
    nombreEquipo: String,
    goles: Int,
    amarillas: Int,
    rojas: Int,
    esquinas: Int,
    onAgregarGol: () -> Unit,
    onRestarGol: () -> Unit,
    onAgregarAmarilla: () -> Unit,
    onRestarAmarilla: () -> Unit,
    onAgregarRoja: () -> Unit,
    onRestarRoja: () -> Unit,
    onAgregarEsquina: () -> Unit,
    onRestarEsquina: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre del equipo
            Text(
                text = nombreEquipo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // ⚽ GOLES
            EstadisticaRow(
                icono = "⚽",
                label = "GOLES",
                valor = goles,
                onAgregar = onAgregarGol,
                onRestar = onRestarGol
            )

            // 🟨 AMARILLAS
            EstadisticaRow(
                icono = "🟨",
                label = "AMARILLAS",
                valor = amarillas,
                onAgregar = onAgregarAmarilla,
                onRestar = onRestarAmarilla
            )

            // 🟥 ROJAS
            EstadisticaRow(
                icono = "🟥",
                label = "ROJAS",
                valor = rojas,
                onAgregar = onAgregarRoja,
                onRestar = onRestarRoja
            )

            // 📐 ESQUINAS
            EstadisticaRow(
                icono = "📐",
                label = "ESQUINAS",
                valor = esquinas,
                onAgregar = onAgregarEsquina,
                onRestar = onRestarEsquina
            )
        }
    }
}

/**
 * Fila de estadística con botones +/-
 */
@Composable
private fun EstadisticaRow(
    icono: String,
    label: String,
    valor: Int,
    onAgregar: () -> Unit,
    onRestar: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label
        Text(
            text = "$icono $label",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Valor y botones
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Valor grande
            Text(
                text = valor.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Botones +/-
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onAgregar,
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.size(50.dp)
                ) {
                    Text("+", fontSize = 18.sp)
                }

                OutlinedButton(
                    onClick = onRestar,
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.size(50.dp),
                    enabled = valor > 0
                ) {
                    Text("-", fontSize = 18.sp)
                }
            }
        }
    }
}