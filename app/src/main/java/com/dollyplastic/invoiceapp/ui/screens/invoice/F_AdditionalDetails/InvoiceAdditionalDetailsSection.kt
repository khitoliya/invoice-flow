package com.dollyplastic.invoiceapp.ui.screens.invoice.F_AdditionalDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.AdditionalDetails
import com.dollyplastic.invoiceapp.data.models.PaymentMode
import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSimpleDropdown
import com.dollyplastic.invoiceapp.ui.common.TextFields.InvoiceScreenTextField
import com.dollyplastic.invoiceapp.ui.components.DatePickerField
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun InvoiceAdditionalDetailsSection(
    details: AdditionalDetails,
    onUpdate: (AdditionalDetails) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    InvoiceScreenCommonCard(
        title = "Additional Details",
        icon = Icons.Default.Description,
        iconColor = Color(0xFF9C27B0), // Purple-ish as per image hint or just distinct
        iconBgColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
        isExpandable = true,
        expanded = expanded,
        onExpandChange = { expanded = it }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            /* -------- PAYMENT MODE -------- */
            InvoiceSimpleDropdown(
                label = "Payment Mode",
                items = PaymentMode.entries,
                selectedItem = details.paymentMode,
                onItemSelected = { onUpdate(details.copy(paymentMode = it)) },
                itemLabel = { it.displayName() },
                onClear = { onUpdate(details.copy(paymentMode = null)) },
                modifier = Modifier.fillMaxWidth()
            )

            /* -------- DELIVERY NOTE & DATE -------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InvoiceScreenTextField(
                    value = details.deliveryNoteNo.orEmpty(),
                    onValueChange = { onUpdate(details.copy(deliveryNoteNo = it)) },
                    label = "Delivery Note No",
                    modifier = Modifier.weight(1f)
                )

                DatePickerField(
                    value = details.deliveryNoteDate.orEmpty(),
                    label = "Date",
                    onDateSelected = { onUpdate(details.copy(deliveryNoteDate = it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            /* -------- BUYER ORDER NO -------- */
            InvoiceScreenTextField(
                value = details.buyerOrderNo.orEmpty(),
                onValueChange = { onUpdate(details.copy(buyerOrderNo = it)) },
                label = "Buyer Order No",
                modifier = Modifier.fillMaxWidth()
            )

            /* -------- REFERENCE NO & DATE -------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InvoiceScreenTextField(
                    value = details.referenceNo.orEmpty(),
                    onValueChange = { onUpdate(details.copy(referenceNo = it)) },
                    label = "Reference No",
                    modifier = Modifier.weight(1f)
                )

                DatePickerField(
                    value = details.referenceDate.orEmpty(),
                    label = "Date",
                    onDateSelected = { onUpdate(details.copy(referenceDate = it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            /* -------- OTHER REFERENCES -------- */
            InvoiceScreenTextField(
                value = details.otherReferences.orEmpty(),
                onValueChange = { onUpdate(details.copy(otherReferences = it)) },
                label = "Other References",
                modifier = Modifier.fillMaxWidth()
            )

            /* -------- TERMS OF DELIVERY -------- */
            InvoiceScreenTextField(
                value = details.termsOfDelivery.orEmpty(),
                onValueChange = { onUpdate(details.copy(termsOfDelivery = it)) },
                label = "Terms of Delivery",
                modifier = Modifier.fillMaxWidth(),
                
            )
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
        PaymentMode.OTHER -> "Other"
    }



