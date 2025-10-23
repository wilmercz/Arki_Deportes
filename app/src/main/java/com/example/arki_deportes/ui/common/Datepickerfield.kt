package com.example.arki_deportes.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.arki_deportes.utils.millisToLocalDateSafe
import com.example.arki_deportes.utils.localDateToMillisAtStartOfDay
import com.example.arki_deportes.utils.parseLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    val initialDate = remember(value) {
        if (value.isNotBlank()) {
            parseLocalDate(value) ?: LocalDate.now()
        } else {
            LocalDate.now()
        }
    }

    val initialMillis = remember(initialDate) {
        localDateToMillisAtStartOfDay(initialDate)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    val formattedDate by remember {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let { millis ->
                val localDate = millisToLocalDateSafe(millis)
                localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
            } ?: value
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        trailingIcon = {
            IconButton(onClick = { if (enabled) showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Seleccionar fecha"
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(formattedDate)
                        showDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}