package com.dollyplastic.invoiceapp.ui.screens.invoice.E_TaxSummary
import androidx.compose.foundation.background
import com.dollyplastic.invoiceapp.ui.common.Utils.formatIndianCurrency
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.TaxSummary
import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun InvoiceTaxSummarySection(
    taxable: Double,
    taxSummary: TaxSummary,
    totalTax: Double,
    grandTotal: Double
) {
    InvoiceScreenCommonCard(
        title = "Tax Summary",
        icon = Icons.Default.ReceiptLong,
        iconColor = AppColors.PrimaryBlue,
        iconBgColor = AppColors.PrimaryBlue.copy(alpha = 0.1f),
        cardContainerColor = AppColors.PrimaryBlue.copy(alpha = 0.05f), // Light Blue background
        elevation = 0.dp, // Remove shadow/outline for colored card
        useSoftShadow = false
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Taxable Value
            SummaryRow(
                label = "Taxable Value",
                value = taxable,
                valueColor = AppColors.TextPrimary // Dark
            )

            // CGST
            if (taxSummary.cgst > 0) {
                TaxRowWithPill(label = "CGST", percentage = "9%", value = taxSummary.cgst)
            }

            // SGST
            if (taxSummary.sgst > 0) {
                TaxRowWithPill(label = "SGST", percentage = "9%", value = taxSummary.sgst)
            }

            // IGST
            if (taxSummary.igst > 0) {
                TaxRowWithPill(label = "IGST", percentage = "18%", value = taxSummary.igst)
            }

            Divider(color = AppColors.Border.copy(alpha = 0.5f))

            // Total Tax
            SummaryRow(
                label = "Total Tax",
                value = totalTax,
                valueColor = AppColors.TextPrimary,
                bold = true // Bold Value only
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Invoice Total Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Invoice Total",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    )

                    val formattedGrandTotal = formatIndianCurrency(grandTotal)
                    Text(
                        text = formattedGrandTotal,
                        style = MaterialTheme.typography.headlineSmall.copy( // Larger, 24sp
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryBlue,
                            fontSize = com.dollyplastic.invoiceapp.ui.common.Utils.getResponsiveFontSize(formattedGrandTotal.length, MaterialTheme.typography.headlineSmall.fontSize)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: Double,
    valueColor: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label is always Regular Grey, unless specifically overridden (but here we want uniformity)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium // Slightly clearer than regular
            )
        )



        val formattedValue = formatIndianCurrency(value)
        Text(
            text = formattedValue,
            style = if (bold)
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, 
                    color = valueColor,
                    fontSize = com.dollyplastic.invoiceapp.ui.common.Utils.getResponsiveFontSize(formattedValue.length, MaterialTheme.typography.titleMedium.fontSize)
                )
            else
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold, 
                    color = valueColor,
                    fontSize = com.dollyplastic.invoiceapp.ui.common.Utils.getResponsiveFontSize(formattedValue.length, MaterialTheme.typography.bodyMedium.fontSize)
                )
        )
    }
}

@Composable
private fun TaxRowWithPill(
    label: String,
    percentage: String,
    value: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(color = AppColors.TextSecondary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = AppColors.Border.copy(alpha = 0.5f) // Light grey pill
            ) {
                Text(
                    text = percentage,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AppColors.TextPrimary),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = formatIndianCurrency(value),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        )
    }
}

