package com.dollyplastic.invoiceapp.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.dollyplastic.invoiceapp.ui.common.TextFields.InvoiceScreenTextField
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = true // Default to true
) {
    var showPicker by remember { mutableStateOf(false) }

    var fieldHeight by remember { mutableIntStateOf(0) }
    
    Box(modifier = modifier.onGloballyPositioned { fieldHeight = it.size.height }) {
        InvoiceScreenTextField(
            value = value,
            onValueChange = {},
            label = label,
            isReadOnly = true,
            containerColor = Color.White,
            trailingIcon = {
                IconButton(onClick = { showPicker = !showPicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = !showPicker }
        )
        
        // Transparent overlay to capture clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = !showPicker }
        )

        if (showPicker) {
            val pickerState = rememberDatePickerState()
            
            Popup(
                onDismissRequest = { showPicker = false },
                alignment = Alignment.TopStart,
                offset = androidx.compose.ui.unit.IntOffset(0, fieldHeight)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    tonalElevation = 6.dp,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(300.dp) // Slightly narrower default
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Compact the DatePicker using scale
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp) // constrain height
                                .scale(0.9f) // Scale down to 90%
                        ) {
                            DatePicker(
                                state = pickerState,
                                showModeToggle = false,
                                title = null,
                                headline = null,
                                colors = DatePickerDefaults.colors(
                                    containerColor = Color.White,
                                    titleContentColor = Color.Black,
                                    headlineContentColor = Color.Black,
                                    weekdayContentColor = Color.Black,
                                    subheadContentColor = Color.Black,
                                    yearContentColor = Color.Black,
                                    currentYearContentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    todayContentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    todayDateBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    dayContentColor = Color.Black,
                                    selectedDayContentColor = Color.White,
                                    selectedDayContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        
                        // Buttons Row - Compact
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp), // Reduced padding
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (allowClear) {
                                Text(
                                    text = "Clear",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium, // Smaller font
                                    modifier = Modifier
                                        .clickable {
                                            onDateSelected("")
                                            showPicker = false
                                        }
                                        .padding(8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(8.dp)) // Placeholder to keep spacing alignment or just omit
                            }
                            
                            Text(
                                text = "Today",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium, // Smaller font
                                modifier = Modifier
                                    .clickable {
                                        val today = java.time.LocalDate.now()
                                        onDateSelected(DateUtils.format(today))
                                        showPicker = false
                                    }
                                    .padding(8.dp)
                            )
                        }
                        
                        LaunchedEffect(pickerState.selectedDateMillis) {
                             pickerState.selectedDateMillis?.let {
                                val date = Instant
                                    .ofEpochMilli(it)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                onDateSelected(DateUtils.format(date))
                                showPicker = false
                            }
                        }
                    }
                }
            }
        }
    }
}
