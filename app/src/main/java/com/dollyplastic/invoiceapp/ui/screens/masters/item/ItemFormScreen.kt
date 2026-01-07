package com.dollyplastic.invoiceapp.ui.screens.masters.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.domain.config.GstConfig
import com.dollyplastic.invoiceapp.ui.components.ConfirmDetailsDialog
import com.dollyplastic.invoiceapp.ui.screens.masters.item.ItemViewModel.ItemUiEvent

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
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (itemId == null) "Add Item" else "Edit Item")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Item Name
            OutlinedTextField(
                value = state.name,
                onValueChange = {
                    viewModel.onFieldChange("name", it)
                },
                label = { Text("Item Name*") },
                isError = state.errors.containsKey("name"),
                supportingText = {
                    state.errors["name"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // HSN / SAC
            OutlinedTextField(
                value = state.hsnCode,
                onValueChange = {
                    viewModel.onFieldChange("hsnCode", it)
                },
                label = { Text("HSN / SAC*") },
                isError = state.errors.containsKey("hsnOrSac"),
                supportingText = {
                    state.errors["hsnOrSac"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // Unit
            OutlinedTextField(
                value = state.unit,
                onValueChange = {
                    viewModel.onFieldChange("unit", it)
                },
                label = { Text("Unit (e.g. PCS, KG)") }
            )

            // GST Rate Dropdown
            GstRateDropdown(
                selectedRate = state.gstRate,
                isError = state.errors.containsKey("gstRate"),
                onSelect = { viewModel.onGstRateChange(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.requestSaveConfirmation()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }

    val confirmDetails = listOf(
        "Item Name" to state.name,
        "HSN / SAC" to state.hsnCode,
        "Unit" to state.unit,
        "GST Rate" to "${state.gstRate}%"
    )
    if (showConfirmDialog) {
        ConfirmDetailsDialog(
            title = "Confirm Item Details",
            details = confirmDetails,
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
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if (selectedRate == 0.0) "0% (Exempt)" else "$selectedRate%",
            onValueChange = {},
            readOnly = true,
            label = { Text("GST Rate*") },
            isError = isError,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rates.forEach { rate ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (rate == 0.0) "0% (Exempt)" else "$rate%"
                        )
                    },
                    onClick = {
                        onSelect(rate)
                        expanded = false
                    }
                )
            }
        }
    }
}
