package com.example.arki_deportes.ui.tablaposiciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.TablaPosicionesItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablaPosicionesFormScreen(
    viewModel: TablaPosicionesFormViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val azulArki = Color(0xFF2E5BBA)
    val naranjaArki = Color(0xFFFF8A3D)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Clasificación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = azulArki,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.guardar() },
                containerColor = azulArki,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar Todo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No hay datos en la tabla", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.importarEquipos() }) {
                            Text("Importar Equipos del Campeonato")
                        }
                    }
                }
            } else {
                // Encabezado fijo
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(azulArki)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Equipo", Modifier.weight(1f), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    listOf("PJ", "PG", "PE", "PP", "GF", "GC", "DG", "PTS").forEach {
                        Text(
                            text = it,
                            modifier = Modifier.width(35.dp),
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.EQUIPO_NOMBRE,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )

                            // Campos editables
                            CeldaEditable(item.PJ) { viewModel.actualizarCampo(item.EQUIPO_CODIGO, "PJ", it) }
                            CeldaEditable(item.PG) { viewModel.actualizarCampo(item.EQUIPO_CODIGO, "PG", it) }
                            CeldaEditable(item.PE) { viewModel.actualizarCampo(item.EQUIPO_CODIGO, "PE", it) }
                            CeldaEditable(item.PP) { viewModel.actualizarCampo(item.EQUIPO_CODIGO, "PP", it) }
                            CeldaEditable(item.GF) { viewModel.actualizarCampo(item.EQUIPO_CODIGO, "GF", it) }
                            CeldaEditable(item.GC) { viewModel.actualizarCampo(item.EQUIPO_CODIGO, "GC", it) }

                            // Valores calculados (No editables)
                            Text(
                                text = "${item.DG}",
                                modifier = Modifier.width(35.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${item.PTS}",
                                modifier = Modifier.width(35.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = naranjaArki,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

/**
 * Componente de celda editable ultra-compacto para valores numéricos
 */
@Composable
fun CeldaEditable(valor: Int, onValueChange: (String) -> Unit) {
    var textValue by remember(valor) { mutableStateOf(valor.toString()) }

    Box(
        modifier = Modifier
            .width(35.dp)
            .height(30.dp)
            .padding(2.dp)
            .background(Color.White, MaterialTheme.shapes.extraSmall)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    textValue = it
                    onValueChange(it)
                }
            },
            textStyle = MaterialTheme.typography.bodySmall.copy(
                textAlign = TextAlign.Center,
                color = Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}
