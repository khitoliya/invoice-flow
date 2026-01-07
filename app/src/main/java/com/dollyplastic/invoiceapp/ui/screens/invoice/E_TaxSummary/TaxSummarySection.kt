package com.dollyplastic.invoiceapp.ui.screens.invoice.E_TaxSummary



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.TaxSummary

@Composable
fun InvoiceTaxSummarySection(
    taxable: Double,
    taxSummary: TaxSummary,
    totalTax: Double,
    grandTotal: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Tax Summary",
            style = MaterialTheme.typography.titleMedium
        )

        SummaryRow("Taxable Value", taxable)

        if (taxSummary.cgst > 0) {
            SummaryRow("CGST", taxSummary.cgst)
            SummaryRow("SGST", taxSummary.sgst)
        }

        if (taxSummary.igst > 0) {
            SummaryRow("IGST", taxSummary.igst)
        }

        Divider()

        SummaryRow(
            label = "Total Tax",
            value = totalTax,
            bold = true
        )

        SummaryRow(
            label = "Invoice Total",
            value = grandTotal,
            bold = true
        )
    }
}
@Composable
private fun SummaryRow(
    label: String,
    value: Double,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (bold)
                MaterialTheme.typography.bodyLarge
            else
                MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "₹%.2f".format(value),
            style = if (bold)
                MaterialTheme.typography.bodyLarge
            else
                MaterialTheme.typography.bodyMedium
        )
    }
}

