package com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen

import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dollyplastic.invoiceapp.ui.components.EntitySelectionDialog
import com.dollyplastic.invoiceapp.ui.screens.invoice.A_HeaderSection.InvoiceHeaderSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.B_FirmSelection.InvoiceFirmSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.C_PartySelection.InvoicePartySection
import com.dollyplastic.invoiceapp.ui.screens.invoice.D_ItemSection.InvoiceItemDialog
import com.dollyplastic.invoiceapp.ui.screens.invoice.D_ItemSection.InvoiceItemSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.E_TaxSummary.InvoiceTaxSummarySection
import com.dollyplastic.invoiceapp.ui.screens.invoice.F_AdditionalDetails.InvoiceAdditionalDetailsSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.G_ComplainceSection.InvoiceComplianceSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.H_TransportSection.InvoiceTransportSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoicePreviewDialog
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceUiEvent
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val firms by viewModel.firms.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val items by viewModel.itemsMaster.collectAsState()
    var showFirmDialog by remember { mutableStateOf(false) }
    var showItemDialog by remember { mutableStateOf(false) }
    var showBillToDialog by remember { mutableStateOf(false) }
    var showShipToDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf<Int?>(null) }



    val context = LocalContext.current
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InvoiceUiEvent.ShowConfirmDialog -> {
                    showPreview = true
                }
                is InvoiceUiEvent.InvoiceSaved -> {
                    Toast.makeText(
                        context,
                        "Invoice saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is InvoiceUiEvent.ShowErrorDialog -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Invoice") })
        },
        bottomBar = {
            Button(
                onClick = viewModel::requestSave,
                enabled = state.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Invoice")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            InvoiceFirmSection(
                firm = state.firm,
                error = state.errors["firm"],
                onClick = { showFirmDialog = true }
            )

            Divider()

            InvoiceHeaderSection(
                state = state,
                onDateChange = viewModel::setInvoiceDate
            )

            Divider()

            InvoicePartySection(
                state = state,
                onCashSaleToggle = viewModel::setCashSale,
                onBillToClick = { showBillToDialog = true },
                onShipToClick = { showShipToDialog = true },
                onShipToSameToggle = viewModel::setShipToSameAsBillTo
            )


            Divider()

            InvoiceItemSection(
                items = state.items,
                onEdit = {
                    editingItemIndex = it
                    showItemDialog = true
                },
                onRemove = viewModel::removeItem,
                onAddClick = {
                    editingItemIndex = null
                    showItemDialog = true
                }
            )


            Divider()


            InvoiceTaxSummarySection(
                taxable = state.totalTaxableValue,
                taxSummary = state.taxSummary,
                totalTax = state.totalTaxAmount,
                grandTotal = state.totalInvoiceValue
            )

            Divider()

            InvoiceTransportSection(
                transport = state.transportDetails,
                onUpdate = viewModel::updateTransport,
                errors = state.errors
            )


            Divider()

            InvoiceAdditionalDetailsSection(
                details = state.additionalDetails,
                onUpdate = viewModel::updateAdditionalDetails
            )

            Divider()
            if(!state.isCashSale){

                InvoiceComplianceSection(
                    generateEInvoice = state.generateEInvoice,
                    generateEWayBill = state.generateEWayBill,
                    onEInvoiceChange = viewModel::setEInvoice,
                    onEWayChange = viewModel::setEWay
                )

            }


        }
    }

    if (showPreview) {
        InvoicePreviewDialog(
            state = state,
            onConfirm = {
                showPreview = false
                viewModel.confirmSave(context)
            },
            onDismiss = { showPreview = false }
        )
    }

    if (showFirmDialog) {
        EntitySelectionDialog(
            title = "Select Firm",
            items = firms, // <-- from ViewModel
            displayName = {
                it.nickName.ifBlank { it.tradeName }
            },
            subText = {
                "${it.city}, ${it.state}"
            },
            onSelect = {
                viewModel.setFirm(it)
                showFirmDialog = false
            },
            onDismiss = { showFirmDialog = false }
        )
    }

    if (showBillToDialog) {
        EntitySelectionDialog(
            title = "Select Buyer (Bill To)",
            items = parties,
            displayName = {
                it.nickName.ifBlank { it.tradeName }
            },
            subText = {
                "${it.city}, ${it.state}"
            },
            onSelect = {
                viewModel.setBillToParty(it)
                showBillToDialog = false
            },
            onDismiss = { showBillToDialog = false }
        )
    }
    if (showShipToDialog) {
        EntitySelectionDialog(
            title = "Select Consignee (Ship To)",
            items = parties,
            displayName = {
                it.nickName.ifBlank { it.tradeName }
            },
            subText = {
                "${it.city}, ${it.state}"
            },
            onSelect = {
                viewModel.setShipToParty(it)
                showShipToDialog = false
            },
            onDismiss = { showShipToDialog = false }
        )
    }
    if (showItemDialog) {
        InvoiceItemDialog(
            items = items,
            initial = editingItemIndex?.let { state.items[it] },
            onSave = { item ->
                if (editingItemIndex == null) {
                    viewModel.addItem(item)
                } else {
                    viewModel.updateItem(editingItemIndex!!, item)
                }
                showItemDialog = false
            },
            onDismiss = {
                showItemDialog = false
            }
        )
    }




}
