package com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dollyplastic.invoiceapp.ui.common.Animations.SmoothExpansion
import com.dollyplastic.invoiceapp.ui.common.dialogs.ConfirmDetailDialog
import com.dollyplastic.invoiceapp.ui.common.dialogs.DetailItem
import com.dollyplastic.invoiceapp.ui.common.dialogs.DetailSection
import com.dollyplastic.invoiceapp.ui.common.softLayerShadow
import com.dollyplastic.invoiceapp.ui.components.EntitySelectionDialog
import com.dollyplastic.invoiceapp.ui.screens.invoice.A_HeaderSection.InvoiceHeaderSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.B_FirmSelection.InvoiceFirmSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.C_PartySelection.InvoicePartySection
import com.dollyplastic.invoiceapp.ui.screens.invoice.D_ItemSection.InvoiceItemSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.E_TaxSummary.InvoiceTaxSummarySection
import com.dollyplastic.invoiceapp.ui.screens.invoice.F_AdditionalDetails.InvoiceAdditionalDetailsSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.G_ComplainceSection.InvoiceComplianceSection
import com.dollyplastic.invoiceapp.ui.screens.invoice.H_TransportSection.InvoiceTransportSection
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreationScreen(
    viewModel: InvoiceViewModel = hiltViewModel(),
    onNavigateToProcessing: (String, Boolean) -> Unit,
    navController: NavHostController
) {
    val state by viewModel.state.collectAsState()
    val firms by viewModel.firms.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val items by viewModel.itemsMaster.collectAsState()
    var showFirmDialog by remember { mutableStateOf(false) }
    var showItemDialog by remember { mutableStateOf(false) }

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
                    
                    // Intent-Based Auto-Start: Pass true here
                    onNavigateToProcessing(event.invoiceId, true)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // Title
                Text(
                    text = if (!state.isEditing) "Create Invoice" else "Edit Invoice",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    ),

                    textAlign = TextAlign.Center
                )

                // Divider
                HorizontalDivider(
                    thickness = 1.dp,
                    color = AppColors.Border,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        },
        containerColor = AppColors.Muted
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = padding.calculateTopPadding()), // Apply only top padding            // Sticky Search & Action Header
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECTION 1: FIRM SELECTION
            item {
                Spacer(Modifier.height(16.dp))
                InvoiceFirmSection(
                    firm = state.firm,
                    firms = firms,
                    onFirmSelected = { 
                        if (it != null) viewModel.setFirm(it) else viewModel.clearFirm()
                    },
                    error = state.getVisibleError("firm")
                )
            }

            // SECTION 2: INVOICE HEADER DETAILS
            item {
                InvoiceHeaderSection(
                    state = state,
                    onDateChange = viewModel::setInvoiceDate
                )
            }

            // SECTION 3: PARTY SELECTION
            item {
                InvoicePartySection(
                    state = state,
                    parties = parties,
                    onCashSaleToggle = viewModel::setCashSale,
                    onBillToSelected = { 
                        viewModel.setBillToParty(it)
                        viewModel.onFieldBlur("billToParty")
                    },
                    onShipToSelected = { 
                        viewModel.setShipToParty(it)
                        viewModel.onFieldBlur("shipToParty")
                    },
                    onShipToSameToggle = viewModel::setShipToSameAsBillTo
                )
            }



            // SECTION 4: ITEMS
            item {
                InvoiceItemSection(
                    items = state.items,
                    itemsMaster = items,
                    isFormVisible = showItemDialog,
                    editingItem = editingItemIndex?.let { state.items[it] },
                    editingItemIndex = editingItemIndex, // Pass index
                    onEdit = {
                        editingItemIndex = it
                        showItemDialog = true
                    },
                    onRemove = viewModel::removeItem,
                    onAddClick = {
                        editingItemIndex = null
                        showItemDialog = true
                    },
                    onSave = { item ->
                        if (editingItemIndex == null) {
                            viewModel.addItem(item)
                        } else {
                            viewModel.updateItem(editingItemIndex!!, item)
                        }
                        showItemDialog = false
                    },
                    onCancel = {
                        showItemDialog = false
                    }
                )
            }

            // SECTION 5: TAX SUMMARY
            item {
                SmoothExpansion(visible = state.items.isNotEmpty()) {
                    
                        InvoiceTaxSummarySection(
                            taxable = state.totalTaxableValue,
                            taxSummary = state.taxSummary,
                            totalTax = state.totalTaxAmount,
                            grandTotal = state.totalInvoiceValue
                        )
                    
                }
            }

            

            // SECTION 6: TRANSPORT DETAILS
            item {
                InvoiceTransportSection(
                    transport = state.transportDetails,
                    onUpdate = viewModel::updateTransport,
                    getErrorMessage = state::getVisibleError,
                    onBlur = viewModel::onFieldBlur,
                    generateEInvoice = state.generateEInvoice,
                    generateEWayBill = state.generateEWayBill,
                    isDistanceReadOnly = state.isDistanceReadOnly
                )
            }



            // SECTION 7: ADDITIONAL DETAILS
            item {
                InvoiceAdditionalDetailsSection(
                    details = state.additionalDetails,
                    onUpdate = viewModel::updateAdditionalDetails
                )
            }



            // SECTION 8: COMPLIANCE
            if(!state.isCashSale){
                item {
                    InvoiceComplianceSection(
                        generateEInvoice = state.generateEInvoice,
                        generateEWayBill = state.generateEWayBill,
                        isEInvoiceAllowed = state.isEInvoiceAllowed,
                        isEWayBillAllowed = state.isEWayBillAllowed,
                        onEInvoiceChange = viewModel::setEInvoice,
                        onEWayChange = viewModel::setEWay
                    )
                }
            }



            // SECTION 9: SAVE BUTTON


// ... inside InvoiceCreationScreen ...

            // SECTION 9: SAVE BUTTON
            item {
                Button(
                    onClick = viewModel::requestSave,
                    enabled = state.isFormValid,
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp)
                        .softLayerShadow(
                            color = Color.Black.copy(alpha = 0.1f), // Standard grey shadow
                            cornersRadius = 12.dp,
                            shadowBlurRadius = 8.dp,
                            offsetY = 4.dp
                        )
                ) {
                    Text(
                        text = if (state.isEditing) "Update Invoice" else "Save Invoice",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }



        }
    }


    if (showPreview) {
        ConfirmDetailDialog(
            title = "Confirm Invoice",
            description = "Please review the details before saving.",
            sections = remember(state) { buildInvoicePreviewSections(state) },
            onConfirm = {
                // showPreview = false // Don't close immediately. Wait for save.
                viewModel.confirmSave(context)
            },
            onDismiss = { showPreview = false },
            isLoading = state.isSaving,
            confirmText = "Confirm & Save"
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







}

private fun buildInvoicePreviewSections(state: InvoiceFormState): List<DetailSection> {
    val sections = mutableListOf<DetailSection>()

    // 1. Invoice Details
    sections.add(
        DetailSection(
            title = "Invoice Details",
            items = listOf(
                DetailItem("Invoice No", state.invoiceNumber),
                DetailItem("Date", state.invoiceDate),
                DetailItem("Financial Year", state.financialYear)
            )
        )
    )

    // 2. Seller (Firm)
    state.firm?.let { firm ->
        sections.add(
            DetailSection(
                title = "Seller (Firm)",
                items = listOf(
                    DetailItem("Name", firm.tradeName),
                    DetailItem("GSTIN", firm.gstin),
                    DetailItem("Address", "${firm.addressLine1}, ${firm.city}, ${firm.state} - ${firm.pincode}")
                )
            )
        )
    }

    // 3. Buyer & Consignee
    if (!state.isCashSale) {
        state.billToParty?.let { party ->
            sections.add(
                DetailSection(
                    title = "Buyer (Bill To)",
                    items = listOf(
                        DetailItem("Name", party.nickName.ifBlank { party.tradeName }),
                        DetailItem("GSTIN", party.gstin),
                        DetailItem("Address", "${party.addressLine1}, ${party.city}, ${party.state} - ${party.pincode}")
                    )
                )
            )
        }

        val shipTo = if (state.shipToSameAsBillTo) state.billToParty else state.shipToParty
        shipTo?.let { party ->
            sections.add(
                DetailSection(
                    title = "Consignee (Ship To)",
                    items = listOf(
                        DetailItem("Name", party.nickName.ifBlank { party.tradeName }),
                        DetailItem("Address", "${party.addressLine1}, ${party.city}, ${party.state} - ${party.pincode}")
                    )
                )
            )
        }
    } else {
        sections.add(
            DetailSection(
                title = "Sale Type",
                items = listOf(DetailItem("Type", "Cash Sale"))
            )
        )
    }

    // 4. Items
    if (state.items.isNotEmpty()) {
        val itemRows = state.items.mapIndexed { index, line ->
            val label = "${index + 1}. ${line.item.name}"
            val value = "${line.quantity} x ₹${line.rate} = ₹${line.taxableValue}" // Simplified
            DetailItem(label, value)
        }
        sections.add(
            DetailSection(
                title = "Items",
                items = itemRows
            )
        )
    }

    // 5. Tax Summary
    val taxItems = mutableListOf<DetailItem>()
    taxItems.add(DetailItem("Taxable Value", "₹%.2f".format(state.totalTaxableValue)))
    if (state.taxSummary.cgst > 0) {
        taxItems.add(DetailItem("CGST", "₹%.2f".format(state.taxSummary.cgst)))
        taxItems.add(DetailItem("SGST", "₹%.2f".format(state.taxSummary.sgst)))
    }
    if (state.taxSummary.igst > 0) {
        taxItems.add(DetailItem("IGST", "₹%.2f".format(state.taxSummary.igst)))
    }
    taxItems.add(DetailItem("Invoice Total", "₹%.2f".format(state.totalInvoiceValue)))

    sections.add(
        DetailSection(
            title = "Tax Summary",
            items = taxItems
        )
    )

    // 6. Transport
    val t = state.transportDetails
    val transportItems = mutableListOf<DetailItem>()
    transportItems.add(DetailItem("Delivery Type", t.deliveryType.name.replace("_", " ")))

    when (t.deliveryType) {
        com.dollyplastic.invoiceapp.data.models.DeliveryType.OWN_VEHICLE -> {
            t.vehicleType?.let { transportItems.add(DetailItem("Vehicle Type", it.name)) }
            t.vehicleNumber?.let { transportItems.add(DetailItem("Vehicle Number", it)) }
        }
        com.dollyplastic.invoiceapp.data.models.DeliveryType.TRANSPORTER -> {
             transportItems.add(DetailItem("Mode", t.mode.name))
             t.transporterName?.let { transportItems.add(DetailItem("Transporter", it)) }
             t.transporterId?.let { transportItems.add(DetailItem("Transporter ID", it)) }
             t.vehicleNumber?.let { transportItems.add(DetailItem("Vehicle Number", it)) }
        }
        else -> {}
    }
    
    if (transportItems.isNotEmpty()) {
        sections.add(DetailSection("Transport Details", transportItems))
    }

    // 7. Compliance
    if (state.generateEInvoice || state.generateEWayBill) {
        val complianceItems = mutableListOf<DetailItem>()
        if (state.generateEInvoice) complianceItems.add(DetailItem("e-Invoice", "Yes"))
        if (state.generateEWayBill) complianceItems.add(DetailItem("e-Way Bill", "Yes"))
        
        sections.add(
            DetailSection(
                title = "Compliance",
                items = complianceItems
            )
        )
    }

    // 8. Additional Details (If any field is present)
    val ad = state.additionalDetails
    val additionalItems = mutableListOf<DetailItem>()

    if (ad.paymentMode != null) additionalItems.add(DetailItem("Payment Mode", ad.paymentMode.name))
    if (!ad.deliveryNoteNo.isNullOrBlank()) additionalItems.add(DetailItem("Delivery Note No", ad.deliveryNoteNo))
    if (!ad.deliveryNoteDate.isNullOrBlank()) additionalItems.add(DetailItem("Delivery Note Date", ad.deliveryNoteDate))
    if (!ad.buyerOrderNo.isNullOrBlank()) additionalItems.add(DetailItem("Buyer Order No", ad.buyerOrderNo))
    if (!ad.referenceNo.isNullOrBlank()) additionalItems.add(DetailItem("Reference No", ad.referenceNo))
    if (!ad.referenceDate.isNullOrBlank()) additionalItems.add(DetailItem("Reference Date", ad.referenceDate))
    if (!ad.otherReferences.isNullOrBlank()) additionalItems.add(DetailItem("Other References", ad.otherReferences))
    if (!ad.termsOfDelivery.isNullOrBlank()) additionalItems.add(DetailItem("Terms of Delivery", ad.termsOfDelivery))

    if (additionalItems.isNotEmpty()) {
        sections.add(
            DetailSection(
                title = "Additional Details",
                items = additionalItems
            )
        )
    }

    return sections
}
