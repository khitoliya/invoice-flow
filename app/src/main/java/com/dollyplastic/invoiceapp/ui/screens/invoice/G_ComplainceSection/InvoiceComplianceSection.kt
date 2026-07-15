package com.dollyplastic.invoiceapp.ui.screens.invoice.G_ComplainceSection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun InvoiceComplianceSection(
    generateEInvoice: Boolean,
    generateEWayBill: Boolean,
    isEInvoiceAllowed: Boolean,
    isEWayBillAllowed: Boolean,
    onEInvoiceChange: (Boolean) -> Unit,
    onEWayChange: (Boolean) -> Unit
) {
    val greenColor = Color(0xFF32A852)
    InvoiceScreenCommonCard(
        title = "Compliance",
        icon = Icons.Default.VerifiedUser,
        iconColor = AppColors.PrimaryBlue,
        iconBgColor = AppColors.PrimaryBlue.copy(alpha = 0.1f),
        isExpandable = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // E-Invoice Checkbox
            ComplianceCheckboxCard(
                text = "Generate e-Invoice",
                checked = generateEInvoice,
                enabled = isEInvoiceAllowed,
                onCheckedChange = onEInvoiceChange,
                checkedColor = greenColor,
                uncheckedColor = AppColors.TextSecondary
            )

            // E-Way Bill Checkbox
            ComplianceCheckboxCard(
                text = "Generate e-Way Bill",
                checked = generateEWayBill,
                enabled = isEWayBillAllowed,
                onCheckedChange = onEWayChange,
                checkedColor = greenColor, // Using Green when checked, as requested/inferred
                uncheckedColor = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ComplianceCheckboxCard(
    text: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedColor: Color,
    uncheckedColor: Color
) {
    val backgroundColor = if (checked) checkedColor.copy(alpha = 0.05f) else AppColors.FieldBackground
    val borderColor = if (checked) checkedColor else AppColors.Border
    val checkboxTint = if (checked) checkedColor else uncheckedColor
    val alpha = if (enabled) 1f else 0.5f

    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = alpha)),
        color = backgroundColor.copy(alpha = alpha),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null, // Handled by row click
                enabled = enabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = checkboxTint,
                    checkmarkColor = Color.White,
                    uncheckedColor = uncheckedColor,
                    disabledCheckedColor = checkboxTint.copy(alpha = 0.5f),
                    disabledUncheckedColor = uncheckedColor.copy(alpha = 0.5f)
                )
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary.copy(alpha = alpha)
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
