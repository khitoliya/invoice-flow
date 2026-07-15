package com.dollyplastic.invoiceapp.ui.screens.masters.item
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.domain.config.GstConfig
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthDropdownField
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthLabel
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthTextField
import com.dollyplastic.invoiceapp.ui.common.dialogs.ConfirmDetailDialog
import com.dollyplastic.invoiceapp.ui.common.dialogs.DetailItem
import com.dollyplastic.invoiceapp.ui.common.dialogs.DetailSection
import com.dollyplastic.invoiceapp.ui.common.dialogs.ValidationErrorDialog
import com.dollyplastic.invoiceapp.ui.screens.masters.item.ItemViewModel.ItemUiEvent
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    navController: NavController,
    itemId: String?,
    viewModel: ItemViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()

    var showErrorDialog by remember { mutableStateOf<String?>(null) }


    // Load for edit
    LaunchedEffect(itemId) {
        itemId?.let { viewModel.loadItemForEdit(it) }
    }

    // UI events (duplicate etc.)
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ItemUiEvent.ShowErrorDialog ->
                    showErrorDialog = event.msg

                ItemUiEvent.ShowConfirmDialog ->
                    showConfirmDialog = true
            }
        }
    }


    showErrorDialog?.let { message ->
        ValidationErrorDialog(
            errors = listOf(message),
            onDismiss = { showErrorDialog = null }
        )
    }



    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier=Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(8.dp)
                    .size(18.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )
            Text(
                if (itemId == null) "Add New Item" else "Edit Item",
                style = AppText.H2.copy(fontSize =20.sp)
            )
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp),
                tint=Color.Transparent

            )
        }

        Spacer(modifier=Modifier.height(45.dp))

        // Profile Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.PrimaryBlue,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.Inventory,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier=Modifier.height(16.dp))

            // Item Name
            ItemFormField(
                label = "Product Name *",
                value = state.name,
                onValueChange = { viewModel.onFieldChange("name", it) },
                onBlur = {  },
                placeholder = "Enter product name",
                icon = Icons.Outlined.ShoppingBag,
                error = state.errors["name"]
            )

            // HSN / SAC
            ItemFormField(
                label = "HSN Code *",
                value = state.hsnCode,
                onValueChange = { viewModel.onFieldChange("hsnCode", it) },
                onBlur = { /* No specific blur logic yet */ },
                placeholder = "Enter HSN code",
                icon = Icons.Outlined.QrCode,
                error = state.errors["hsnOrSac"]
            )

            // Unit
            ItemFormField(
                label = "Unit *",
                value = state.unit,
                onValueChange = { viewModel.onFieldChange("unit", it) },
                onBlur = { /* No specific blur logic yet */ },
                placeholder = "Select unit", // Or enter unit
                icon = Icons.Outlined.Straighten // Ruler icon
            )

            // GST Rate Dropdown
            GstRateDropdown(
                selectedRate = state.gstRate,
                isError = state.errors.containsKey("gstRate"),
                onSelect = { viewModel.onGstRateChange(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.requestSaveConfirmation()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonColors(
                    containerColor = AppColors.PrimaryBlue,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.PrimaryBlue.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text(if (itemId == null) "Add Product" else "Update Product")
            }
        }



    val confirmSections = listOf(
        DetailSection(
            title = "",
            items = listOf(
                DetailItem("Item Name", state.name),
                DetailItem("HSN / SAC", state.hsnCode),
                DetailItem("Unit", state.unit),
                DetailItem("GST Rate", "${state.gstRate}%")
            )
        )
    )

    if (showConfirmDialog) {
        ConfirmDetailDialog(
            title = "Confirm Item Details",
            description = "Please review the details below before saving.",
            sections = confirmSections,
            icon = Icons.Default.Inventory,
            onConfirm = {
                showConfirmDialog = false
                viewModel.saveItem {
                    navController.popBackStack()
                }
            },
            onDismiss = { showConfirmDialog = false }
        )
    }


}


@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun GstRateDropdown(
    selectedRate: Double,
    isError: Boolean,
    onSelect: (Double) -> Unit
) {
    val rates = GstConfig.ALLOWED_GST_RATES
    
    AuthDropdownField(
        label = "GST Rate *",
        options = rates,
        selectedOption = selectedRate,
        onOptionSelected = onSelect,
        optionLabel = { rate -> 
             if (rate == 0.0) "0% (Exempt)" else "$rate%"
        },
        placeholder = "Select GST rate",
        leadingIcon = Icons.Outlined.Percent,
        isError = isError
    )
}

@Composable
fun ItemFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onBlur: (() -> Unit)? = null,
    placeholder: String,
    icon: ImageVector,
    error: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    var wasFocused by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        AuthLabel(label)
        AuthTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = icon,
            trailingContent = trailingContent,
            isError = error != null,
            modifier = Modifier.onFocusChanged { focusState ->
                if (wasFocused && !focusState.isFocused) {
                    onBlur?.invoke()
                }
                wasFocused = focusState.isFocused
            }
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
