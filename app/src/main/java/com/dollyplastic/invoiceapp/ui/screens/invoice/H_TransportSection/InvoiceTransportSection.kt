package com.dollyplastic.invoiceapp.ui.screens.invoice.H_TransportSection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.DeliveryType
import com.dollyplastic.invoiceapp.data.models.TransportDetails
import com.dollyplastic.invoiceapp.data.models.TransportMode
import com.dollyplastic.invoiceapp.data.models.VehicleType
import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSimpleDropdown
import com.dollyplastic.invoiceapp.ui.common.TextFields.InvoiceScreenTextField
import com.dollyplastic.invoiceapp.ui.components.DatePickerField
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun InvoiceTransportSection(
    transport: TransportDetails,
    onUpdate: (TransportDetails) -> Unit,
    getErrorMessage: (String) -> String?,
    onBlur: (String) -> Unit,
    generateEInvoice: Boolean,
    generateEWayBill: Boolean,
    isDistanceReadOnly: Boolean
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val summaryText = when (transport.deliveryType) {
        DeliveryType.BUYER_PICKUP -> "Buyer Pickup"
        DeliveryType.OWN_VEHICLE -> "Own Vehicle - ${transport.vehicleNumber ?: ""}"
        DeliveryType.TRANSPORTER -> "Via Transporter - ${transport.mode.name}"
    }

    InvoiceScreenCommonCard(
        title = "Transport Details",
        subtitle = summaryText,
        icon = Icons.Default.LocalShipping,
        iconColor = Color(0xFF32A852), // Green
        iconBgColor = Color(0xFF32A852).copy(alpha = 0.1f),
        isExpandable = true,
        expanded = expanded,
        onExpandChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ---------- DISTANCE (Conditional) ---------- */
            if (generateEInvoice || generateEWayBill) {
                InvoiceScreenTextField(
                    value = if (transport.distance > 0) transport.distance.toString() else "",
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            val dist = it.toIntOrNull() ?: 0
                            onUpdate(transport.copy(distance = dist))
                        }
                    },
                    label = "Distance (Km)",
                    isReadOnly = isDistanceReadOnly,
                    isError = getErrorMessage("distance") != null,
                    errorMessage = getErrorMessage("distance"),
                    onBlur = { onBlur("distance") },
                    trailingIcon = if (isDistanceReadOnly) {
                        { Icon(Icons.Default.Lock, contentDescription = "Auto-calculated", tint = AppColors.TextSecondary) }
                    } else null
                )
            }

            Text("Delivery Type", style = MaterialTheme.typography.labelLarge, color = AppColors.TextSecondary)

            /* ---------- DELIVERY TYPE RADIOS ---------- */
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val availableTypes = if (generateEInvoice || generateEWayBill) {
                    DeliveryType.entries.filter { it != DeliveryType.BUYER_PICKUP }
                } else {
                    DeliveryType.entries.toList()
                }

                availableTypes.forEach { type ->
                    TransportModeRadioCard(
                        label = type.name.replace("_", " "),
                        selected = transport.deliveryType == type,
                        onClick = {
                            onUpdate(
                                TransportDetails(
                                    deliveryType = type,
                                    mode = TransportMode.ROAD,
                                    vehicleType = if (type == DeliveryType.OWN_VEHICLE) VehicleType.REGULAR else if (type == DeliveryType.TRANSPORTER) VehicleType.REGULAR else null,
                                    vehicleNumber = null,
                                    transporterName = null,
                                    transporterId = null,
                                    transporterDocNo = null,
                                    transporterDocDate = null,
                                    distance = transport.distance // Preserve distance
                                )
                            )
                        }
                    )
                }
            }

            /* ---------- CONDITIONAL FIELDS ---------- */
            
            // OWN VEHICLE FIELDS
            if (transport.deliveryType == DeliveryType.OWN_VEHICLE) {
                
                // Vehicle Type Dropdown
                InvoiceSimpleDropdown(
                    label = "Vehicle Type",
                    items = VehicleType.entries,
                    selectedItem = transport.vehicleType,
                    onItemSelected = { type ->
                        onUpdate(transport.copy(vehicleType = type))
                    },
                    itemLabel = { it.name },
                    isError = getErrorMessage("vehicleType") != null,
                    errorMessage = getErrorMessage("vehicleType")
                )

                InvoiceScreenTextField(
                    value = transport.vehicleNumber ?: "",
                    onValueChange = { onUpdate(transport.copy(vehicleNumber = it)) },
                    label = "Vehicle Number*",
                    isError = getErrorMessage("vehicleNumber") != null,
                    errorMessage = getErrorMessage("vehicleNumber"),
                    onBlur = { onBlur("vehicleNumber") }
                )
            }

            // TRANSPORTER FIELDS
            if (transport.deliveryType == DeliveryType.TRANSPORTER) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    // Transport Mode Dropdown
                    InvoiceSimpleDropdown(
                        label = "Transport Mode",
                        items = TransportMode.entries,
                        selectedItem = transport.mode,
                        onItemSelected = { mode ->
                            onUpdate(
                                transport.copy(
                                    mode = mode,
                                    vehicleType = if (mode == TransportMode.ROAD) transport.vehicleType ?: VehicleType.REGULAR else null,
                                    vehicleNumber = null,
                                    transporterDocNo = null,
                                    transporterDocDate = null,
                                    transporterName = if (mode != TransportMode.ROAD) mode.name else transport.transporterName
                                )
                            )
                        },
                        itemLabel = { it.name },
                        isRequired = true,
                        isError = getErrorMessage("mode") != null,
                        errorMessage = getErrorMessage("mode")
                    )

                    // Vehicle Type Dropdown (Road Only)
                    if (transport.mode == TransportMode.ROAD) {
                         InvoiceSimpleDropdown(
                            label = "Vehicle Type",
                            items = VehicleType.entries,
                            selectedItem = transport.vehicleType,
                            onItemSelected = { type ->
                                onUpdate(transport.copy(vehicleType = type))
                            },
                            itemLabel = { it.name },
                            isError = getErrorMessage("vehicleType") != null,
                            errorMessage = getErrorMessage("vehicleType")
                        )
                    }

                    // Transporter Name & ID
                    InvoiceScreenTextField(
                        value = transport.transporterName ?: "",
                        onValueChange = { onUpdate(transport.copy(transporterName = it)) },
                        label = "Transporter Name*",
                        isError = getErrorMessage("transporterName") != null,
                        errorMessage = getErrorMessage("transporterName"),
                        onBlur = { onBlur("transporterName") }
                    )

                    InvoiceScreenTextField(
                        value = transport.transporterId ?: "",
                        onValueChange = { onUpdate(transport.copy(transporterId = it)) },
                        label = "Transporter ID*",
                        isError = getErrorMessage("transporterId") != null,
                        errorMessage = getErrorMessage("transporterId"),
                        onBlur = { onBlur("transporterId") }
                    )

                    // Doc No & Date
                    InvoiceScreenTextField(
                        value = transport.transporterDocNo ?: "",
                        onValueChange = { onUpdate(transport.copy(transporterDocNo = it)) },
                        label = transporterDocLabel(transport.mode),
                        isError = getErrorMessage("transporterDocNo") != null,
                        errorMessage = getErrorMessage("transporterDocNo"),
                        onBlur = { onBlur("transporterDocNo") }
                    )

                    // Date Picker (Custom param needed in InvoiceScreenTextField or keep custom?)
                    // The existing code used DatePickerField. I'll stick to DatePickerField for now but maybe wrap it ?
                    // Actually, DatePickerField is a separate component. I should check if it fits the style.
                    // Assuming DatePickerField matches reasonably well or I'll just use it as is.
                     DatePickerField(
                        value = transport.transporterDocDate ?: "",
                        label = "Document Date*",
                        onDateSelected = { onUpdate(transport.copy(transporterDocDate = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                     // Error for date
                    getErrorMessage("transporterDocDate")?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp))
                    }

                    // Vehicle Number (Road Only)
                    if (transport.mode == TransportMode.ROAD) {
                        InvoiceScreenTextField(
                            value = transport.vehicleNumber ?: "",
                            onValueChange = { onUpdate(transport.copy(vehicleNumber = it)) },
                            label = "Vehicle Number*",
                            isError = getErrorMessage("vehicleNumber") != null,
                            errorMessage = getErrorMessage("vehicleNumber"),
                            onBlur = { onBlur("vehicleNumber") }
                        )
                    }
                }
            }
        }
    }
}

private fun transporterDocLabel(mode: TransportMode): String =
    when (mode) {
        TransportMode.ROAD -> "LR / GR Number*"
        TransportMode.RAIL -> "Railway Receipt No*"
        TransportMode.AIR  -> "Airway Bill No*"
        TransportMode.SHIP -> "Bill of Lading No*"
    }



