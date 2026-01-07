package com.dollyplastic.invoiceapp.ui.screens.invoice.B_FirmSelection



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.ui.components.EntitySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceFirmSection(
    firm: Firm?,
    error: String?,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            "Seller (Firm)",
            style = MaterialTheme.typography.titleMedium
        )

        EntitySelector(
            label = "Select Firm*",
            value = firm?.nickName ?: firm?.tradeName ?: "",
            error = error,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
