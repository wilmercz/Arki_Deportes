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
@Composable
fun CampeonatoSelector(
    modifier: Modifier = Modifier,
    viewModel: CampeonatoListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val campeonatoActivo by CampeonatoContext.campeonatoActivo.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Cargar campeonatos si aún no se han cargado
    LaunchedEffect(Unit) {
        // El ViewModel ya carga los campeonatos en init
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Campeonato Activo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = campeonatoActivo?.CAMPEONATO ?: "Todos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (campeonatoActivo != null) {
                        Text(
                            text = "Año ${campeonatoActivo?.ANIO}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Cambiar campeonato",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showDialog) {
        CampeonatoSelectorDialog(
            campeonatos = uiState.campeonatos,
            campeonatoActivo = campeonatoActivo,
            onCampeonatoSelected = { campeonato ->
                CampeonatoContext.seleccionarCampeonato(campeonato)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
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
