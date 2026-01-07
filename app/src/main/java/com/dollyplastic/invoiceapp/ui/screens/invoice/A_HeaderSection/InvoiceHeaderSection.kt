package com.dollyplastic.invoiceapp.ui.screens.invoice.A_HeaderSection


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.ui.components.DatePickerField
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceFormState

@Composable
fun InvoiceHeaderSection(
    state: InvoiceFormState,
    onDateChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            "Invoice Details",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = state.invoiceNumber,
            onValueChange = {},
            readOnly = true,
            label = { Text("Invoice Number") },
            modifier = Modifier.fillMaxWidth()
        )

        DatePickerField(
            value = state.invoiceDate,
            label = "Invoice Date",
            onDateSelected = onDateChange,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.financialYear,
            onValueChange = {},
            readOnly = true,
            label = { Text("Financial Year") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
