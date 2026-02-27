package com.example.arki_deportes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.ui.campeonatos.CampeonatoListViewModel

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CAMPEONATO SELECTOR - SELECTOR DE CAMPEONATO ACTIVO
 * ═══════════════════════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampeonatoSelector(
    campeonatos: List<Campeonato>? = null,
    campeonatoSeleccionado: String? = null,
    onCampeonatoSeleccionado: (Campeonato) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CampeonatoListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val campeonatoActivo by CampeonatoContext.campeonatoActivo.collectAsState()

    val lista = remember(campeonatos, uiState.campeonatos) {
        campeonatos ?: uiState.campeonatos
    }

    var expanded by remember { mutableStateOf(false) }

    if (lista.isEmpty()) {
        Text(
            text = "Cargando campeonatos...",
            modifier = modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // ✅ LÓGICA CORREGIDA: No usar .first() por defecto.
    // Si no hay coincidencia, 'seleccionado' será null.
    val seleccionado = remember(lista, campeonatoActivo, campeonatoSeleccionado) {
        lista.find { it.CODIGO == (campeonatoSeleccionado ?: campeonatoActivo?.CODIGO) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        TextField(
            readOnly = true,
            // Si es null, mostramos invitación a seleccionar
            value = seleccionado?.CAMPEONATO ?: "Seleccionar Campeonato...",
            onValueChange = {},
            label = { Text("Campeonato") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            lista.forEach { camp ->
                DropdownMenuItem(
                    text = { Text(camp.CAMPEONATO) },
                    onClick = {
                        expanded = false
                        onCampeonatoSeleccionado(camp)
                    }
                )
            }
        }
    }
}
