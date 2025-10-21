package com.example.arki_deportes.ui.catalogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.utils.SportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topAppBarState = rememberTopAppBarState()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Catálogos") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Regresar")
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
            )
        }
    ) { paddingValues ->
        CatalogsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogsScreenContent(modifier: Modifier = Modifier) {
    var campeonatoNombre by rememberSaveable { mutableStateOf("") }
    var campeonatoProvincia by rememberSaveable { mutableStateOf("") }
    var campeonatoSportId by rememberSaveable { mutableStateOf(SportType.FUTBOL.id) }

    var partidoEquipo1 by rememberSaveable { mutableStateOf("") }
    var partidoEquipo2 by rememberSaveable { mutableStateOf("") }
    var partidoAnotaciones1 by rememberSaveable { mutableStateOf("") }
    var partidoAnotaciones2 by rememberSaveable { mutableStateOf("") }
    var partidoDuracion by rememberSaveable { mutableStateOf("") }
    var partidoSportId by rememberSaveable { mutableStateOf(SportType.FUTBOL.id) }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            CampeonatoForm(
                nombre = campeonatoNombre,
                onNombreChange = { campeonatoNombre = it },
                provincia = campeonatoProvincia,
                onProvinciaChange = { campeonatoProvincia = it },
                selectedSport = SportType.fromId(campeonatoSportId),
                onSportSelected = { campeonatoSportId = it.id }
            )
        }

        item {
            PartidoForm(
                equipo1 = partidoEquipo1,
                onEquipo1Change = { partidoEquipo1 = it },
                equipo2 = partidoEquipo2,
                onEquipo2Change = { partidoEquipo2 = it },
                anotaciones1 = partidoAnotaciones1,
                onAnotaciones1Change = { partidoAnotaciones1 = it },
                anotaciones2 = partidoAnotaciones2,
                onAnotaciones2Change = { partidoAnotaciones2 = it },
                duracion = partidoDuracion,
                onDuracionChange = { partidoDuracion = it },
                selectedSport = SportType.fromId(partidoSportId),
                onSportSelected = { partidoSportId = it.id }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampeonatoForm(
    nombre: String,
    onNombreChange: (String) -> Unit,
    provincia: String,
    onProvinciaChange: (String) -> Unit,
    selectedSport: SportType,
    onSportSelected: (SportType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nuevo campeonato",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona el deporte del campeonato para personalizar los campos relacionados con los partidos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SportDropdown(
                label = "Deporte",
                selectedSport = selectedSport,
                onSportSelected = onSportSelected
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre del campeonato") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = provincia,
                onValueChange = onProvinciaChange,
                label = { Text("Provincia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank() && provincia.isNotBlank()
            ) {
                Text("Guardar campeonato")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartidoForm(
    equipo1: String,
    onEquipo1Change: (String) -> Unit,
    equipo2: String,
    onEquipo2Change: (String) -> Unit,
    anotaciones1: String,
    onAnotaciones1Change: (String) -> Unit,
    anotaciones2: String,
    onAnotaciones2Change: (String) -> Unit,
    duracion: String,
    onDuracionChange: (String) -> Unit,
    selectedSport: SportType,
    onSportSelected: (SportType) -> Unit,
    modifier: Modifier = Modifier
) {
    val anotacionesLabel = remember(selectedSport) { selectedSport.teamScoreLabel }
    val duracionLabel = remember(selectedSport) { selectedSport.scheduleDurationLabel }
    val unidadDuracion = remember(selectedSport) { selectedSport.durationUnitSuffix }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nuevo partido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Elige el deporte para adaptar los campos de anotaciones y duración del partido.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SportDropdown(
                label = "Deporte",
                selectedSport = selectedSport,
                onSportSelected = onSportSelected
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = equipo1,
                    onValueChange = onEquipo1Change,
                    label = { Text("Equipo local") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = equipo2,
                    onValueChange = onEquipo2Change,
                    label = { Text("Equipo visitante") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = anotaciones1,
                    onValueChange = onAnotaciones1Change,
                    label = { Text("$anotacionesLabel equipo 1") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default
                )
                OutlinedTextField(
                    value = anotaciones2,
                    onValueChange = onAnotaciones2Change,
                    label = { Text("$anotacionesLabel equipo 2") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default
                )
            }

            OutlinedTextField(
                value = duracion,
                onValueChange = onDuracionChange,
                label = { Text(duracionLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    Text(
                        text = "Ejemplo: ${if (unidadDuracion == "min") "90" else "4"} ${unidadDuracion}",
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = equipo1.isNotBlank() && equipo2.isNotBlank()
            ) {
                Text("Guardar partido")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportDropdown(
    label: String,
    selectedSport: SportType,
    onSportSelected: (SportType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember { SportType.options() }
    val textFieldValue = remember(selectedSport) { selectedSport.displayName }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = textFieldValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { sport ->
                DropdownMenuItem(
                    text = { Text(sport.displayName) },
                    onClick = {
                        onSportSelected(sport)
                        expanded = false
                    }
                )
            }
        }
    }
}
