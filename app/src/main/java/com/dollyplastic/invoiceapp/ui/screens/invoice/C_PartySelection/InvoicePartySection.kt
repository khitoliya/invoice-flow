package com.dollyplastic.invoiceapp.ui.screens.invoice.C_PartySelection





import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.ui.components.EntitySelector
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceFormState

@Composable
fun InvoicePartySection(
    state: InvoiceFormState,
    onCashSaleToggle: (Boolean) -> Unit,
    onBillToClick: () -> Unit,
    onShipToClick: () -> Unit,
    onShipToSameToggle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            "Buyer Details",
            style = MaterialTheme.typography.titleMedium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.isCashSale,
                onCheckedChange = onCashSaleToggle
            )
            Text("Cash Sale")
        }

        if (!state.isCashSale) {

            EntitySelector(
                label = "Bill To (Buyer)*",
                value = state.billToParty?.nickName
                    ?: state.billToParty?.tradeName
                    ?: "",
                error = state.errors["billToParty"],
                onClick = onBillToClick
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.shipToSameAsBillTo,
                    onCheckedChange = onShipToSameToggle
                )
                Text("Ship To same as Bill To")
            }

            if (!state.shipToSameAsBillTo) {
                EntitySelector(
                    label = "Ship To (Consignee)",
                    value = state.shipToParty?.nickName
                        ?: state.shipToParty?.tradeName
                        ?: "",
                    error = state.errors["shipToParty"],
                    onClick = onShipToClick
                )
            }
        }
    }
}


