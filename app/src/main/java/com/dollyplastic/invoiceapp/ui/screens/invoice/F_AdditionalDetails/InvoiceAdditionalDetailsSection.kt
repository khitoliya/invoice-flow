package com.dollyplastic.invoiceapp.ui.screens.invoice.F_AdditionalDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.AdditionalDetails
import com.dollyplastic.invoiceapp.data.models.PaymentMode
import com.dollyplastic.invoiceapp.data.models.TransportDetails
import com.dollyplastic.invoiceapp.data.models.TransportMode
import com.dollyplastic.invoiceapp.ui.components.DatePickerField

@Composable
fun InvoiceAdditionalDetailsSection(
    details: AdditionalDetails,
    onUpdate: (AdditionalDetails) -> Unit

) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        TextButton(onClick = { expanded = !expanded }) {
            Text(
                if (expanded)
                    "Hide Additional Details"
                else
                    "Add Additional Details (Optional)"
            )
        }

        if (!expanded) return

        /* -------- PAYMENT MODE -------- */

        PaymentModeDropdown(
            selected = details.paymentMode,
            onSelect = {
                onUpdate(details.copy(paymentMode = it))
            }
        )

        HelperText("How the buyer will pay (Cheque / RTGS / UPI etc.)")

        /* -------- DELIVERY NOTE -------- */

        OutlinedTextField(
            value = details.deliveryNoteNo.orEmpty(),
            onValueChange = {
                onUpdate(details.copy(deliveryNoteNo = it))
            },
            label = { Text("Delivery Note No") },
            modifier = Modifier.fillMaxWidth()
        )

        HelperText("Internal delivery challan number, if any")

        DatePickerField(
            value = details.deliveryNoteDate.orEmpty(),
            label = "Delivery Note Date",
            onDateSelected = {
                onUpdate(details.copy(deliveryNoteDate = it))
            }
        )

        HelperText("Date of delivery challan (optional)")

        /* -------- BUYER ORDER -------- */

        OutlinedTextField(
            value = details.buyerOrderNo.orEmpty(),
            onValueChange = {
                onUpdate(details.copy(buyerOrderNo = it))
            },
            label = { Text("Buyer’s Order No") },
            modifier = Modifier.fillMaxWidth()
        )

        HelperText("Purchase order number provided by buyer")

        /* -------- REFERENCE -------- */

        OutlinedTextField(
            value = details.referenceNo.orEmpty(),
            onValueChange = {
                onUpdate(details.copy(referenceNo = it))
            },
            label = { Text("Reference No") },
            modifier = Modifier.fillMaxWidth()
        )

        DatePickerField(
            value = details.referenceDate.orEmpty(),
            label = "Reference Date",
            onDateSelected = {
                onUpdate(details.copy(referenceDate = it))
            }
        )

        HelperText("Quotation / Proforma / Contract reference")

        /* -------- OTHER REFERENCES -------- */

        OutlinedTextField(
            value = details.otherReferences.orEmpty(),
            onValueChange = {
                onUpdate(details.copy(otherReferences = it))
            },
            label = { Text("Other References") },
            modifier = Modifier.fillMaxWidth()
        )

        HelperText("Any additional commercial reference")

        /* -------- TERMS -------- */

        OutlinedTextField(
            value = details.termsOfDelivery.orEmpty(),
            onValueChange = {
                onUpdate(details.copy(termsOfDelivery = it))
            },
            label = { Text("Terms of Delivery") },
            modifier = Modifier.fillMaxWidth()
        )

        HelperText("FOB, CIF, Door Delivery, or custom terms")
    }
}

@Composable
private fun HelperText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentModeDropdown(
    selected: PaymentMode?,
    onSelect: (PaymentMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected?.displayName() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Mode / Terms of Payment") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PaymentMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.displayName()) },
                    onClick = {
                        onSelect(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}
fun PaymentMode.displayName(): String =
    when (this) {
        PaymentMode.CASH   -> "Cash"
        PaymentMode.CHEQUE -> "Cheque"
        PaymentMode.RTGS   -> "RTGS"
        PaymentMode.NEFT   -> "NEFT"
        PaymentMode.UPI    -> "UPI"
        PaymentMode.OTHER -> "OTHER"
    }



