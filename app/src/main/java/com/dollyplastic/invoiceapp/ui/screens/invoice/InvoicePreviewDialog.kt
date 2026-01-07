package com.dollyplastic.invoiceapp.ui.screens.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.DeliveryType
import com.dollyplastic.invoiceapp.data.models.TransportMode
import com.dollyplastic.invoiceapp.ui.screens.invoice.F_AdditionalDetails.displayName
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceFormState

@Composable
fun InvoicePreviewDialog(
    state: InvoiceFormState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Invoice") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                /* ---------- HEADER ---------- */
                PreviewSection("Invoice Details") {
                    PreviewRow("Invoice No", state.invoiceNumber)
                    PreviewRow("Date", state.invoiceDate)
                    PreviewRow("Financial Year", state.financialYear)
                }

                /* ---------- FIRM ---------- */
                PreviewSection("Seller (Firm)") {
                    state.firm?.let {
                        PreviewRow("Name", it.tradeName)
                        PreviewRow("GSTIN", it.gstin)
                        PreviewRow(
                            "Address",
                            "${it.addressLine1}, ${it.city}, ${it.state}"
                        )
                    }
                }

                /* ---------- PARTY ---------- */
                if (!state.isCashSale) {
                    PreviewSection("Buyer (Bill To)") {
                        state.billToParty?.let {
                            PreviewRow(
                                "Name",
                                it.nickName.ifBlank { it.tradeName }
                            )
                            PreviewRow("GSTIN", it.gstin)
                            PreviewRow(
                                "Address",
                                "${it.addressLine1}, ${it.city}, ${it.state}"
                            )
                        }
                    }

                    PreviewSection("Consignee (Ship To)") {
                        val shipTo =
                            if (state.shipToSameAsBillTo)
                                state.billToParty
                            else
                                state.shipToParty

                        shipTo?.let {
                            PreviewRow(
                                "Name",
                                it.nickName.ifBlank { it.tradeName }
                            )
                            PreviewRow(
                                "Address",
                                "${it.addressLine1}, ${it.city}, ${it.state}"
                            )
                        }
                    }
                } else {
                    PreviewSection("Sale Type") {
                        PreviewRow("Type", "Cash Sale")
                    }
                }

                /* ---------- ITEMS ---------- */
                PreviewSection("Items") {
                    state.items.forEachIndexed { index, line ->
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text =
                                    "${index + 1}. ${line.item.name} (${line.item.hsnCode})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Qty ${line.quantity} × ₹${line.rate} = ₹${line.taxableValue}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                /* ---------- TAX SUMMARY ---------- */
                PreviewSection("Tax Summary") {
                    PreviewRow(
                        "Taxable Value",
                        "₹%.2f".format(state.totalTaxableValue)
                    )

                    if (state.taxSummary.cgst > 0) {
                        PreviewRow(
                            "CGST",
                            "₹%.2f".format(state.taxSummary.cgst)
                        )
                        PreviewRow(
                            "SGST",
                            "₹%.2f".format(state.taxSummary.sgst)
                        )
                    }

                    if (state.taxSummary.igst > 0) {
                        PreviewRow(
                            "IGST",
                            "₹%.2f".format(state.taxSummary.igst)
                        )
                    }

                    Divider()

                    PreviewRow(
                        "Invoice Total",
                        "₹%.2f".format(state.totalInvoiceValue),
                        bold = true
                    )
                }

                /* ---------- TRANSPORT ---------- */
                PreviewSection("Transport Details") {

                    val t = state.transportDetails

                    PreviewRow(
                        "Delivery Type",
                        t.deliveryType.name.replace("_", " ")
                    )

                    when (t.deliveryType) {

                        /* ---------- OWN VEHICLE ---------- */
                        DeliveryType.OWN_VEHICLE -> {

                            PreviewRow("Mode", "Road")
                            t.vehicleType?.let {
                                PreviewRow("Vehicle Type", it.name)
                            }
                            t.vehicleNumber?.let {
                                PreviewRow("Vehicle Number", it)
                            }
                        }

                        /* ---------- TRANSPORTER ---------- */
                        DeliveryType.TRANSPORTER -> {

                            PreviewRow(
                                "Mode",
                                t.mode.name
                            )

                            t.transporterName?.let {
                                PreviewRow("Transporter Name", it)
                            }

                            t.transporterId?.let {
                                PreviewRow("Transporter ID", it)
                            }

                            t.transporterDocNo?.let {
                                PreviewRow(
                                    transporterDocLabel(t.mode),
                                    it
                                )
                            }

                            t.transporterDocDate?.let {
                                PreviewRow("Document Date", it)
                            }

                            if (t.mode == TransportMode.ROAD) {
                                t.vehicleType?.let {
                                    PreviewRow("Vehicle Type", it.name)
                                }
                                t.vehicleNumber?.let {
                                    PreviewRow("Vehicle Number", it)
                                }
                            }

                            if (t.mode == TransportMode.AIR || t.mode == TransportMode.SHIP) {
                                t.portOfLoading?.let {
                                    PreviewRow("Port of Loading", it)
                                }
                                t.portOfDischarge?.let {
                                    PreviewRow("Port of Discharge", it)
                                }
                            }
                        }

                        /* ---------- BUYER PICKUP ---------- */
                        DeliveryType.BUYER_PICKUP -> {
                            PreviewRow("Handled By", "Buyer Pickup")
                        }
                    }




                }

                /* ---------- ADDITIONAL DETAILS ---------- */
                if (
                    state.additionalDetails?.paymentMode != null ||
                    state.additionalDetails?.deliveryNoteNo != null ||
                    state.additionalDetails?.buyerOrderNo != null ||
                    state.additionalDetails?.referenceNo != null ||
                    state.additionalDetails?.otherReferences != null ||
                    state.additionalDetails?.termsOfDelivery != null
                ) {
                    PreviewSection("Additional Details") {

                        state.additionalDetails.paymentMode?.let {
                            PreviewRow(
                                "Mode / Terms of Payment",
                                it.displayName()
                            )
                        }

                        state.additionalDetails.deliveryNoteNo?.let {
                            PreviewRow("Delivery Note No", it)
                        }

                        state.additionalDetails.deliveryNoteDate?.let {
                            PreviewRow("Delivery Note Date", it)
                        }

                        state.additionalDetails.buyerOrderNo?.let {
                            PreviewRow("Buyer's Order No", it)
                        }

                        state.additionalDetails.referenceNo?.let {
                            PreviewRow("Reference No", it)
                        }

                        state.additionalDetails.referenceDate?.let {
                            PreviewRow("Reference Date", it)
                        }

                        state.additionalDetails.otherReferences?.let {
                            PreviewRow("Other References", it)
                        }

                        state.additionalDetails.termsOfDelivery?.let {
                            PreviewRow("Terms of Delivery", it)
                        }
                    }
                }



                /* ---------- COMPLIANCE ---------- */
                if (state.generateEInvoice || state.generateEWayBill) {
                    PreviewSection("Compliance") {
                        if (state.generateEInvoice)
                            PreviewRow("e-Invoice", "Yes")
                        if (state.generateEWayBill)
                            PreviewRow("e-Way Bill", "Yes")
                    }
                }


            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PreviewSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        content()
    }
}

@Composable
private fun PreviewRow(
    label: String,
    value: String,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style =
                if (bold)
                    MaterialTheme.typography.bodyLarge
                else
                    MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            style =
                if (bold)
                    MaterialTheme.typography.bodyLarge
                else
                    MaterialTheme.typography.bodyMedium
        )
    }
}

private fun transporterDocLabel(mode: TransportMode): String =
    when (mode) {
        TransportMode.ROAD -> "LR / GR Number"
        TransportMode.RAIL -> "Railway Receipt No"
        TransportMode.AIR  -> "Airway Bill No"
        TransportMode.SHIP -> "Bill of Lading No"
    }

