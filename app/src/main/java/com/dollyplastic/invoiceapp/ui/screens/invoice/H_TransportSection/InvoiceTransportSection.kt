package com.dollyplastic.invoiceapp.ui.screens.invoice.H_TransportSection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.DeliveryType
import com.dollyplastic.invoiceapp.data.models.TransportDetails
import com.dollyplastic.invoiceapp.data.models.TransportMode
import com.dollyplastic.invoiceapp.ui.components.DatePickerField



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

import com.dollyplastic.invoiceapp.data.models.*

@Composable
fun InvoiceTransportSection(
    transport: TransportDetails,
    onUpdate: (TransportDetails) -> Unit,
    errors: Map<String, String>
) {
    val t = transport

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = "Transport Details",
            style = MaterialTheme.typography.titleMedium
        )

        /* ---------- DELIVERY TYPE ---------- */

        DeliveryTypeSelector(
            selected = t.deliveryType,
            error = errors["deliveryType"],
            onSelect = { selectedType ->
                onUpdate(
                    TransportDetails(
                        deliveryType = selectedType,
                        mode = TransportMode.ROAD,
                        vehicleType =
                            if (selectedType != DeliveryType.OWN_VEHICLE )
                                VehicleType.REGULAR
                            else null,
                        vehicleNumber = null,
                        transporterName = null,
                        transporterId = null,
                        transporterDocNo = null,
                        transporterDocDate = null
                    )
                )
            }

        )

        /* ---------- TRANSPORT MODE ---------- */

        if (t.deliveryType == DeliveryType.TRANSPORTER) {

            ModeSelector(
                selected = t.mode,
                enabled = t.deliveryType == DeliveryType.TRANSPORTER,
                error = errors["mode"],
                onSelect = { mode ->
                    onUpdate(
                        t.copy(
                            mode = mode,

                            // vehicle type only valid for ROAD
                            vehicleType =
                                if (mode == TransportMode.ROAD)
                                    t.vehicleType ?: VehicleType.REGULAR
                                else
                                    null,

                            // reset dependent fields
                            vehicleNumber = null,
                            transporterDocNo = null,
                            transporterDocDate = null,

                            // default transporter name for non-road modes
                            transporterName =
                                if (mode != TransportMode.ROAD)
                                    mode.name
                                else
                                    t.transporterName
                        )
                    )
                }
            )

        }

        /* ---------- VEHICLE TYPE (ROAD ONLY) ---------- */

        if (
            t.mode == TransportMode.ROAD &&
            t.deliveryType != DeliveryType.BUYER_PICKUP
        ) {
            VehicleTypeSelector(
                selected = t.vehicleType,
                onSelect = {
                    onUpdate(t.copy(vehicleType = it))
                },
                error = errors["vehicleType"]
            )
        }

        /* ---------- OWN VEHICLE ---------- */

        if (t.deliveryType == DeliveryType.OWN_VEHICLE) {

            OutlinedTextField(
                value = t.vehicleNumber ?: "",
                onValueChange = {
                    onUpdate(t.copy(vehicleNumber = it))
                },
                label = { Text("Vehicle Number*") },
                modifier = Modifier.fillMaxWidth(),
                isError = errors["vehicleNumber"] != null,
                supportingText = {
                    errors["vehicleNumber"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }

        /* ---------- TRANSPORTER ---------- */

        if (t.deliveryType == DeliveryType.TRANSPORTER) {

            OutlinedTextField(
                value = t.transporterName ?: "",
                onValueChange = {
                    onUpdate(t.copy(transporterName = it))
                },
                label = { Text("Transporter Name*") },
                isError = errors["transporterName"] != null,
                supportingText = {
                    errors["transporterName"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = t.transporterId ?: "",
                onValueChange = {
                    onUpdate(t.copy(transporterId = it))
                },
                label = { Text("Transporter ID*") },
                isError = errors["transporterId"] != null,
                supportingText = {
                    errors["transporterId"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = t.transporterDocNo ?: "",
                onValueChange = {
                    onUpdate(t.copy(transporterDocNo = it))
                },
                label = {
                    Text(transporterDocLabel(t.mode))
                },
                isError = errors["transporterDocNo"] != null,
                supportingText = {
                    errors["transporterDocNo"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                value = t.transporterDocDate ?: "",
                label = "Document Date*",
                onDateSelected = {
                    onUpdate(t.copy(transporterDocDate = it))
                },
                modifier = Modifier.fillMaxWidth(),
            )
            errors["transporterDocDate"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (t.mode == TransportMode.ROAD) {
                OutlinedTextField(
                    value = t.vehicleNumber ?: "",
                    onValueChange = {
                        onUpdate(t.copy(vehicleNumber = it))
                    },
                    label = { Text("Vehicle Number*") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeliveryTypeSelector(
    selected: DeliveryType,
    error: String?,
    onSelect: (DeliveryType) -> Unit
) {
    Column {
        Text("Delivery Type")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeliveryType.entries.forEach { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == type,
                        onClick = { onSelect(type) }
                    )
                    Text(type.name.replace("_", " "))
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeSelector(
    selected: TransportMode,
    enabled: Boolean,
    error: String?,
    onSelect: (TransportMode) -> Unit
) {
    Column {
        Text("Transport Mode")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransportMode.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == mode,
                        enabled = enabled,
                        onClick = { onSelect(mode) }
                    )
                    Text(mode.name)
                }
            }
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun VehicleTypeSelector(
    selected: VehicleType?,
    error: String?,
    onSelect: (VehicleType) -> Unit
) {
    Column {
        Text("Vehicle Type")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            VehicleType.entries.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selected == it,
                        onClick = { onSelect(it) }
                    )
                    Text(it.name)
                }
            }
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
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

private fun defaultTransporterName(mode: TransportMode): String =
    when (mode) {
        TransportMode.ROAD -> ""
        TransportMode.RAIL -> "Indian Railways"
        TransportMode.AIR  -> "Air Cargo"
        TransportMode.SHIP -> "Shipping Line"
    }



