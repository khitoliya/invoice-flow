package com.dollyplastic.invoiceapp.ui.screens.invoice.A_HeaderSection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.common.TextFields.InvoiceScreenTextField
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceFormState
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import java.time.Instant
import java.time.ZoneId

import com.dollyplastic.invoiceapp.ui.components.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHeaderSection(
    state: InvoiceFormState,
    onDateChange: (String) -> Unit
) {

    InvoiceScreenCommonCard(
        title = "Invoice Details",
        icon = Icons.Default.Receipt,
        iconColor = AppColors.PrimaryBlue,
        iconBgColor = AppColors.PrimaryBlue.copy(alpha = 0.1f)
    ) {
        Column {
            InvoiceScreenTextField(
                value = state.invoiceNumber,
                onValueChange = {},
                label = "Invoice Number",
                isReadOnly = true,
                placeholder = "Auto-generated",
                trailingIcon = {
                    if (state.isGeneratingInvoiceNumber) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = AppColors.PrimaryBlue
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Field
            DatePickerField(
                value = state.invoiceDate,
                label = "Invoice Date",
                onDateSelected = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                allowClear = false // Prevent clearing invoice date
            )

            Spacer(modifier = Modifier.height(16.dp))

            InvoiceScreenTextField(
                value = state.financialYear,
                onValueChange = {},
                label = "Financial Year",
                isReadOnly = true
            )
        }
    }
}
