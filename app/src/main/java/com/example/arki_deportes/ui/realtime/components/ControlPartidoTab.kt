// ui/realtime/components/ControlPartidoTab.kt

package com.example.arki_deportes.ui.realtime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONTROL PARTIDO TAB - TODOS LOS CONTROLES +/-
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
fun ControlPartidoTab(
    equipo1: String,
    equipo2: String,
    goles1: Int,
    goles2: Int,
    amarillas1: Int,
    amarillas2: Int,
    rojas1: Int,
    rojas2: Int,
    esquinas1: Int,
    esquinas2: Int,
    onAgregarGol1: () -> Unit,
    onRestarGol1: () -> Unit,
    onAgregarGol2: () -> Unit,
    onRestarGol2: () -> Unit,
    onAgregarAmarilla1: () -> Unit,
    onRestarAmarilla1: () -> Unit,
    onAgregarAmarilla2: () -> Unit,
    onRestarAmarilla2: () -> Unit,
    onAgregarRoja1: () -> Unit,
    onRestarRoja1: () -> Unit,
    onAgregarRoja2: () -> Unit,
    onRestarRoja2: () -> Unit,
    onAgregarEsquina1: () -> Unit,
    onRestarEsquina1: () -> Unit,
    onAgregarEsquina2: () -> Unit,
    onRestarEsquina2: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ═══════════════════════════════════════════════════════════
        // HEADER CON NOMBRES DE EQUIPOS
        // ═══════════════════════════════════════════════════════════
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = equipo1.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = equipo2.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Divider()

        // ═══════════════════════════════════════════════════════════
        // ⚽ GOLES
        // ═══════════════════════════════════════════════════════════
        ControlRow(
            icono = "⚽",
            label = "GOLES",
            valor1 = goles1,
            valor2 = goles2,
            onMenos1 = onRestarGol1,
            onMas1 = onAgregarGol1,
            onMenos2 = onRestarGol2,
            onMas2 = onAgregarGol2,
            colorFondo = MaterialTheme.colorScheme.primaryContainer
        )

        // ═══════════════════════════════════════════════════════════
        // 🟨 TARJETAS AMARILLAS
        // ═══════════════════════════════════════════════════════════
        ControlRow(
            icono = "🟨",
            label = "AMARILLAS",
            valor1 = amarillas1,
            valor2 = amarillas2,
            onMenos1 = onRestarAmarilla1,
            onMas1 = onAgregarAmarilla1,
            onMenos2 = onRestarAmarilla2,
            onMas2 = onAgregarAmarilla2,
            colorFondo = MaterialTheme.colorScheme.tertiaryContainer
        )

        // ═══════════════════════════════════════════════════════════
        // 🟥 TARJETAS ROJAS
        // ═══════════════════════════════════════════════════════════
        ControlRow(
            icono = "🟥",
            label = "ROJAS",
            valor1 = rojas1,
            valor2 = rojas2,
            onMenos1 = onRestarRoja1,
            onMas1 = onAgregarRoja1,
            onMenos2 = onRestarRoja2,
            onMas2 = onAgregarRoja2,
            colorFondo = MaterialTheme.colorScheme.errorContainer
        )

        // ═══════════════════════════════════════════════════════════
        // 📐 ESQUINAS (CORNERS)
        // ═══════════════════════════════════════════════════════════
        ControlRow(
            icono = "📐",
            label = "ESQUINAS",
            valor1 = esquinas1,
            valor2 = esquinas2,
            onMenos1 = onRestarEsquina1,
            onMas1 = onAgregarEsquina1,
            onMenos2 = onRestarEsquina2,
            onMas2 = onAgregarEsquina2,
            colorFondo = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

/**
 * Fila de control horizontal optimizada
 * Layout: [Icono Label] | [-] [Valor] [+] | [-] [Valor] [+]
 */
@Composable
private fun ControlRow(
    icono: String,
    label: String,
    valor1: Int,
    valor2: Int,
    onMenos1: () -> Unit,
    onMas1: () -> Unit,
    onMenos2: () -> Unit,
    onMas2: () -> Unit,
    colorFondo: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LABEL
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(100.dp)
            ) {
                Text(text = icono, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // CONTROLES EQUIPO 1
            ControlesCompactos(
                valor = valor1,
                onMenos = onMenos1,
                onMas = onMas1
            )

            // CONTROLES EQUIPO 2
            ControlesCompactos(
                valor = valor2,
                onMenos = onMenos2,
                onMas = onMas2
            )
        }
    }
}

/**
 * Controles compactos: [-] [3] [+]
 */
@Composable
private fun ControlesCompactos(
    valor: Int,
    onMenos: () -> Unit,
    onMas: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Botón MENOS
        IconButton(
            onClick = onMenos,
            enabled = valor > 0,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Restar",
                modifier = Modifier.size(20.dp)
            )
        }

        // VALOR
        Text(
            text = valor.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )

        // Botón MÁS
        IconButton(
            onClick = onMas,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}