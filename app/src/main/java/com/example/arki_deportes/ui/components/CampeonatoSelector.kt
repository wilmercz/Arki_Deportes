package com.example.arki_deportes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.ui.campeonatos.CampeonatoListViewModel

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CAMPEONATO SELECTOR - SELECTOR DE CAMPEONATO ACTIVO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Componente que muestra el campeonato actualmente seleccionado y permite
 * cambiarlo. Se muestra típicamente en el Drawer o TopBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampeonatoSelector(
    campeonatos: List<Campeonato>? = null,                 // 👈 NUEVO: lista externa opcional
    campeonatoSeleccionado: String? = null,                // (opcional) si ya usas una clave activa
    onCampeonatoSeleccionado: (Campeonato) -> Unit = {},   // callback al seleccionar
    modifier: Modifier = Modifier,
    viewModel: CampeonatoListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val campeonatoActivo by CampeonatoContext.campeonatoActivo.collectAsState()

    // 👇 Unificamos la fuente: externa si viene, interna si no
    val lista = remember(campeonatos, uiState.campeonatos) {
        campeonatos ?: uiState.campeonatos
    }

    var showDialog by remember { mutableStateOf(false) }

    // Si no hay campeonatos, muestra estado vacío (puedes usar tu EmptyState actual)
    if (lista.isEmpty()) {
        Text(
            text = "No hay campeonatos disponibles",
            modifier = modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // Selección inicial: por contexto activo o por parámetro, o primer item
    var seleccionado by remember(lista, campeonatoActivo, campeonatoSeleccionado) {
        mutableStateOf(
            lista.find { it.CODIGO == (campeonatoSeleccionado ?: campeonatoActivo?.CODIGO) }
                ?: lista.first()
        )
    }

    // === tu UI existente del selector ===
    ExposedDropdownMenuBox(
        expanded = showDialog,
        onExpandedChange = { showDialog = !showDialog },
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        TextField(
            readOnly = true,
            value = seleccionado.CAMPEONATO,
            onValueChange = {},
            label = { Text("Campeonato") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDialog) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = showDialog,
            onDismissRequest = { showDialog = false }
        ) {
            lista.forEach { camp ->
                DropdownMenuItem(
                    text = { Text(camp.CAMPEONATO) },
                    onClick = {
                        seleccionado = camp
                        showDialog = false
                        // (opcional) actualizar contexto global si tienes API:
                        // CampeonatoContext.seleccionar(camp)
                        onCampeonatoSeleccionado(camp)
                    }
                )
            }
        }
    }
}


/**
 * Diálogo para seleccionar un campeonato
 */
@Composable
private fun CampeonatoSelectorDialog(
    campeonatos: List<Campeonato>,
    campeonatoActivo: Campeonato?,
    onCampeonatoSelected: (Campeonato?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Título
                Text(
                    text = "Seleccionar Campeonato",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Lista de campeonatos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    // Opción "Todos"
                    item {
                        CampeonatoItem(
                            nombre = "Todos los campeonatos",
                            anio = null,
                            isSelected = campeonatoActivo == null,
                            onClick = { onCampeonatoSelected(null) }
                        )
                        Divider()
                    }
                    
                    // Lista de campeonatos
                    items(campeonatos) { campeonato ->
                        CampeonatoItem(
                            nombre = campeonato.CAMPEONATO,
                            anio = campeonato.ANIO,
                            isSelected = campeonato.CODIGO == campeonatoActivo?.CODIGO,
                            onClick = { onCampeonatoSelected(campeonato) }
                        )
                        if (campeonato != campeonatos.last()) {
                            Divider()
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón cerrar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

/**
 * Item individual de campeonato en el diálogo
 */
@Composable
private fun CampeonatoItem(
    nombre: String,
    anio: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (anio != null) {
                    Text(
                        text = "Año $anio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
