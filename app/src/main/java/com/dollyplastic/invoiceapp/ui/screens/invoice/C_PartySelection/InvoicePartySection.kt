package com.dollyplastic.invoiceapp.ui.screens.invoice.C_PartySelection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sip
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.ui.common.Cards.ExpandableSelectionCard
import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSearchableDropdown
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceFormState
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun InvoicePartySection(
    state: InvoiceFormState,
    parties: List<Party>,
    onCashSaleToggle: (Boolean) -> Unit,
    onBillToSelected: (Party?) -> Unit,
    onShipToSelected: (Party?) -> Unit,
    onShipToSameToggle: (Boolean) -> Unit
) {
    InvoiceScreenCommonCard(
        title = "Buyer Details",
        icon = Icons.Default.Person,
        iconColor = AppColors.PrimaryBlue,
        iconBgColor = AppColors.PrimaryBlue.copy(alpha = 0.1f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Cash Sale Checkbox Card
            CheckboxCard(
                text = "Cash Sale",
                checked = state.isCashSale,
                onCheckedChange = onCashSaleToggle,
                checkboxColor = Color(0xFFF59E0B), // Amber
                uncheckedColor = Color(0xFFF59E0B),
                cardBackgroundColor = Color(0xFFF59E0B).copy(alpha = 0.05f) // Always Amber tint
            )

            if (!state.isCashSale) {
                // Bill To Dropdown or Card

                InvoiceSearchableDropdown(
                    label = "Bill To (Buyer)*",
                    items = parties,
                    selectedItem = state.billToParty,
                    onItemSelected = onBillToSelected,
                    itemLabel = { it.nickName.ifBlank { it.tradeName } },
                    isError = state.getVisibleError("billToParty") != null,
                    errorMessage = state.getVisibleError("billToParty"),
                    placeholder = "Search Buyer..."
                )
                if (state.billToParty != null) {

                    ExpandableSelectionCard(
                        title = state.billToParty.nickName.ifBlank { state.billToParty.tradeName },
                        subtitle = state.billToParty.city,
                        icon = Icons.Filled.CheckCircle,
                        containerColor = AppColors.BillToBackground,
                        iconColor = AppColors.BillToIcon,
                        content = {
                            Column {
                                // GSTIN
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Store, // Building/Store icon looks more like reference
                                        contentDescription = "GSTIN",
                                        tint = AppColors.BillToIcon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "GSTIN",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                        Text(
                                            text = state.billToParty.gstin,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Address
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Filled.Place,
                                        contentDescription = "Address",
                                        tint = AppColors.BillToIcon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Address",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                        Text(
                                            text = "${state.billToParty.addressLine1}, ${state.billToParty.city}, ${state.billToParty.state} - ${state.billToParty.pincode}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                // Ship To Same Checkbox Card
                CheckboxCard(
                    text = "Ship To same as Bill To",
                    checked = state.shipToSameAsBillTo,
                    onCheckedChange = onShipToSameToggle,
                    checkboxColor = AppColors.PrimaryBlue,
                    cardBackgroundColor = AppColors.textFieldGrey // Always Greyish
                )

                if (!state.shipToSameAsBillTo) {
                    // Ship To Dropdown or Card

                    InvoiceSearchableDropdown(
                        label = "Ship To (Consignee)",
                        items = parties,
                        selectedItem = state.shipToParty,
                        onItemSelected = onShipToSelected,
                        itemLabel = { it.nickName.ifBlank { it.tradeName } },
                        isError = state.getVisibleError("shipToParty") != null,
                        errorMessage = state.getVisibleError("shipToParty"),
                        placeholder = "Search Consignee..."
                    )
                    if (state.shipToParty != null){
                        ExpandableSelectionCard(
                            title = state.shipToParty.nickName.ifBlank { state.shipToParty.tradeName },
                            subtitle = state.shipToParty.city,
                            icon = Icons.Filled.CheckCircle,
                            containerColor = AppColors.ShipToBackground,
                            iconColor = AppColors.ShipToIcon,
                            content = {
                                Column {
                                    // GSTIN (Optional for Consignee but good to show)
                                    if (state.shipToParty.gstin.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Store,
                                                contentDescription = "GSTIN",
                                                tint = AppColors.ShipToIcon,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "GSTIN",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                                )
                                                Text(
                                                    text = state.shipToParty.gstin,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.Black
                                                    )
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    // Address
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Filled.Place,
                                            contentDescription = "Address",
                                            tint = AppColors.ShipToIcon,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Address",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                            Text(
                                                text = "${state.shipToParty.addressLine1}, ${state.shipToParty.city}, ${state.shipToParty.state} - ${state.shipToParty.pincode}",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckboxCard(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkboxColor: Color,
    cardBackgroundColor: Color,
    uncheckedColor: Color = AppColors.TextSecondary
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, cardBackgroundColor),
        color = cardBackgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null, // Handled by row click
                colors = CheckboxDefaults.colors(

                    checkedColor = checkboxColor,
                    checkmarkColor = Color.White,
                    uncheckedColor = uncheckedColor
                )
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}



