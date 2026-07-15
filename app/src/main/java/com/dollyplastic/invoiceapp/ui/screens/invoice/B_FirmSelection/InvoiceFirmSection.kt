package com.dollyplastic.invoiceapp.ui.screens.invoice.B_FirmSelection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.ui.common.Cards.ExpandableSelectionCard
import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSearchableDropdown
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun InvoiceFirmSection(
    firm: Firm?,
    firms: List<Firm>,
    onFirmSelected: (Firm?) -> Unit,
    error: String?
) {
    InvoiceScreenCommonCard(
        title = "Seller (Firm)",
        icon = Icons.Outlined.Store,
        iconColor = AppColors.PrimaryBlue,
        iconBgColor = Color(0xFFEFF6FF), // Light blue background
        //modifier = Modifier.padding(bottom = 16.dp) // Removed to avoid double spacing in LazyColumn
    ) {
        Column {
            InvoiceSearchableDropdown(
                label = "Select Firm *",
                items = firms,
                selectedItem = firm,
                onItemSelected = onFirmSelected,
                itemLabel = { it.nickName.ifBlank { it.tradeName } },
                isError = error != null,
                errorMessage = error,
                placeholder = "Search Firm...",
                isRequired = true
            )

            if (firm != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selected Firm Details Block
                ExpandableSelectionCard(
                    title = firm.nickName.ifBlank { firm.tradeName },
                    subtitle = firm.city,
                    icon = Icons.Filled.CheckCircle,
                    containerColor = Color(0xFFF8FAFC),
                    content = {
                        Column {
                            // GSTIN
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Info, // Generic Info icon for GSTIN
                                    contentDescription = "GSTIN",
                                    tint = AppColors.PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "GSTIN",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                    Text(
                                        text = firm.gstin,
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
                                    imageVector = Icons.Filled.Place, // Pin icon for address
                                    contentDescription = "Address",
                                    tint = AppColors.PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Address",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                    Text(
                                        text = "${firm.addressLine1}, ${firm.city}, ${firm.state} - ${firm.pincode}",
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
