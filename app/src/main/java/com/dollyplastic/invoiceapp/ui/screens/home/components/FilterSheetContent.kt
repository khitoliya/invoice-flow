package com.dollyplastic.invoiceapp.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.ui.screens.home.HomeViewModel
import com.dollyplastic.invoiceapp.ui.components.DatePickerField
import com.dollyplastic.invoiceapp.ui.common.TextFields.InvoiceScreenTextField
import com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSearchableDropdown
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheetContent(
    currentFilters: HomeViewModel.FilterState,
    parties: List<Party>,
    items: List<Item>,
    onApply: (HomeViewModel.FilterState) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit = {}
) {
    var status by remember { mutableStateOf(currentFilters.selectedStatus) }
    var minAmount by remember { mutableStateOf(currentFilters.minAmount?.toString() ?: "") }
    var maxAmount by remember { mutableStateOf(currentFilters.maxAmount?.toString() ?: "") }
    
    // Dropdown States
    var selectedPartyId by remember { mutableStateOf(currentFilters.selectedPartyId) }
    var selectedHsn by remember { mutableStateOf(currentFilters.hsnCode ?: "") }
    
    // Date State
    var startDateISO by remember { mutableStateOf(currentFilters.startDateISO) }
    var endDateISO by remember { mutableStateOf(currentFilters.endDateISO) }
    
    var showDateRangePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxHeight(0.9f).background(Color.White)) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = AppColors.TextPrimary)
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = AppColors.PrimaryBlue.copy(alpha = 0.1f),
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = AppColors.PrimaryBlue, modifier = Modifier.padding(6.dp))
            }
        }
        HorizontalDivider(color = AppColors.Border)
        
        // SCROLLABLE CONTENT
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // 1. Date Range Filter
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = AppColors.PrimaryBlue.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DateRange, null, tint = AppColors.PrimaryBlue, modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Date Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatePillField(
                    value = startDateISO?.let { 
                        try { 
                           val sdfIn = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
                           val sdfOut = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                           sdfOut.format(sdfIn.parse(it)!!)
                        } catch(e:Exception) { it } 
                    } ?: "",
                    placeholder = "Start Date",
                    onClick = { showDateRangePicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePillField(
                    value = endDateISO?.let { 
                        try { 
                           val sdfIn = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
                           val sdfOut = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                           sdfOut.format(sdfIn.parse(it)!!)
                        } catch(e:Exception) { it } 
                    } ?: "",
                    placeholder = "End Date",
                    onClick = { showDateRangePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Status Filter
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color(0xFF4CAF50).copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                    Icon(androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_agenda), null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val displayStatuses = listOf(InvoiceStatus.COMPLETED, InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED)
                displayStatuses.forEach { s ->
                    val isSelected = status == s
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        color = if (isSelected) AppColors.PrimaryBlue else AppColors.textFieldGrey,
                        modifier = Modifier.clickable { status = if (status == s) null else s }
                    ) {
                        Text(
                            text = if (s == InvoiceStatus.DRAFT) "PENDING" else s.name,
                            color = if (isSelected) Color.White else AppColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Amount Range
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                 Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color(0xFF9C27B0).copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                     Icon(Icons.Default.AttachMoney, null, tint = Color(0xFF9C27B0), modifier = Modifier.padding(6.dp))
                 }
                 Spacer(modifier = Modifier.width(8.dp))
                 Text("Amount Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterTextField(
                    value = minAmount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) minAmount = it },
                    placeholder = "Min Amount",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                FilterTextField(
                    value = maxAmount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) maxAmount = it },
                    placeholder = "Max Amount",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Party Filter
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color(0xFFFF9800).copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Person, null, tint = Color(0xFFFF9800), modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Party", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            val selectedParty = parties.find { it.partyId == selectedPartyId }
            Box(Modifier.fillMaxWidth()) {
                InvoiceSearchableDropdown(
                    label = "Search party name...",
                    items = parties,
                    selectedItem = selectedParty,
                    onItemSelected = { selectedPartyId = it?.partyId },
                    itemLabel = { it.nickName.ifBlank { it.tradeName } }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. HSN Code
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = AppColors.PrimaryBlue.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Category, null, tint = AppColors.PrimaryBlue, modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("HSN / Item", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            val selectedItem = items.find { it.hsnCode == selectedHsn }
            Box(Modifier.fillMaxWidth()) {
                com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSimpleDropdown(
                    label = "Select Item/HSN...",
                    items = items.filter { it.hsnCode.isNotBlank() },
                    selectedItem = selectedItem,
                    onItemSelected = { selectedHsn = it?.hsnCode ?: "" },
                    itemLabel = { "${it.name} (${it.hsnCode})" },
                    onClear = { selectedHsn = "" }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        HorizontalDivider(color = AppColors.Border)
        
        // ACTIONS (Fixed at bottom)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f).height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border)
            ) { 
                Text("Clear All", color = AppColors.TextPrimary) 
            }
            Button(
                onClick = { 
                    onApply(currentFilters.copy(
                        selectedStatus = status,
                        minAmount = minAmount.toDoubleOrNull(),
                        maxAmount = maxAmount.toDoubleOrNull(),
                        hsnCode = selectedHsn.ifBlank { null },
                        selectedPartyId = selectedPartyId,
                        startDateISO = startDateISO,
                        endDateISO = endDateISO
                    )) 
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue)
            ) { 
                Text("Apply Filters") 
            }
        }
    }

    if (showDateRangePicker) {
        val dateParams = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateParams.selectedStartDateMillis
                    val end = dateParams.selectedEndDateMillis
                    if (start != null) {
                         val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
                         startDateISO = sdf.format(java.util.Date(start))
                         endDateISO = if (end != null) sdf.format(java.util.Date(end)) else startDateISO
                    }
                    showDateRangePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = dateParams)
        }
    }
}

@Composable
fun DatePillField(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = AppColors.textFieldGrey,
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) AppColors.TextSecondary else AppColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.DateRange, null, tint = AppColors.MutedForeground, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = AppColors.TextPrimary),
        modifier = modifier.height(52.dp),
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppColors.textFieldGrey,
            unfocusedContainerColor = AppColors.textFieldGrey,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            disabledContainerColor = AppColors.textFieldGrey
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        singleLine = true
    )
}
