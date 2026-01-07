package com.dollyplastic.invoiceapp.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val pickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        val date = Instant
                            .ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        onDateSelected(DateUtils.format(date))
                    }
                    showPicker = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        }
    )
}
