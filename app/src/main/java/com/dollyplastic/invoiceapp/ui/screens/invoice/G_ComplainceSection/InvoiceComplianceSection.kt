package com.dollyplastic.invoiceapp.ui.screens.invoice.G_ComplainceSection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun InvoiceComplianceSection(
    generateEInvoice: Boolean,
    generateEWayBill: Boolean,
    onEInvoiceChange: (Boolean) -> Unit,
    onEWayChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            text = "Compliance (Optional)",
            style = MaterialTheme.typography.titleMedium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = generateEInvoice,
                onCheckedChange = onEInvoiceChange
            )
            Text("Generate e-Invoice")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = generateEWayBill,
                onCheckedChange = onEWayChange
            )
            Text("Generate e-Way Bill")
        }
    }
}
