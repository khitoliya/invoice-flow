package com.dollyplastic.invoiceapp.ui.screens.processing

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.pdf.InvoicePdfGenerator
import com.dollyplastic.invoiceapp.ui.screens.invoice.H_TransportSection.InvoiceTransportSection
import com.dollyplastic.invoiceapp.ui.screens.processing.components.*
import com.dollyplastic.invoiceapp.ui.components.deletion.InvoiceDeletionDialogs
import java.io.File
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceProcessingScreen(
    invoiceId: String,
    autoStart: Boolean = false,
    viewModel: InvoiceProcessingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPortal: (String, InvoiceStorageRef, String, Boolean, String) -> Unit, // Added Mode
    onViewPdf: (Invoice) -> Unit,
    onSharePdf: (File, Pair<String, String>?) -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val deletionUiState by viewModel.deletionHelper.uiState.collectAsState()
    
    val context = LocalContext.current

 
    /* ---------- Load once ---------- */
    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId, autoStart)
    }

    /* ---------- Navigation Events ---------- */
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InvoiceProcessingUiEvent.NavigateBack ->
                    onNavigateBack()

                is InvoiceProcessingUiEvent.InvoiceDeleted ->
                    onNavigateBack() // Or navigate to list

                is InvoiceProcessingUiEvent.NavigateToPortal ->
                    onNavigateToPortal(event.url, event.storageRef, event.credentialsJson, event.isCancellation, event.mode)

                is InvoiceProcessingUiEvent.ViewPdf ->
                    onViewPdf(event.invoice)

                is InvoiceProcessingUiEvent.SharePdf ->
                    onSharePdf(event.file, event.preferredContact)

                is InvoiceProcessingUiEvent.NavigateToEdit ->
                    onNavigateToEdit(event.invoiceId)
                    
                is InvoiceProcessingUiEvent.ShowError ->
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Unified Deletion Dialogs
    InvoiceDeletionDialogs(
        uiState = deletionUiState,
        onConfirmHardDelete = { viewModel.confirmHardDelete(it) },
        onConfirmArchive = { inv, remark -> viewModel.confirmCancellation(inv, remark) },
        onWarningAcknowledged = { viewModel.deletionHelper.onWarningAcknowledged(it) },
        onDismiss = { viewModel.dismissDeleteDialog() },
        onOpenPortal = { viewModel.openPortalForCancellation() }
    )


    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Invoice Processing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onDeleteClicked() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Invoice", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
            // .weight has to be inside a column context that supports it (like below), but here we are root.
        ) {

            /* ---------- Invoice Summary ---------- */
            state.invoice?.let { invoice ->
                InvoiceDetailsView(
                    invoice = invoice,
                    isComplianceLocked = true, // always read-only here
                    onEditClick = {} // removed
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                /* ---------- Post-Generation Compliance Actions ---------- */
                if (invoice.status == InvoiceStatus.COMPLETED && !invoice.isCashSale) {
                    if (state.showInlineTransportForm) {
                        // INLINE TRANSPORT FORM
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f) // Takes up remaining space
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Complete Transport Details for ${if (state.pendingComplianceAction == "EWAY") "e-Way Bill" else "e-Invoice"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                            item {
                                InvoiceTransportSection(
                                    transport = state.draftTransportDetails,
                                    onUpdate = viewModel::updateDraftTransport,
                                    getErrorMessage = { field -> state.transportErrors[field] },
                                    onBlur = viewModel::onDraftTransportFieldBlur,
                                    generateEInvoice = state.pendingComplianceAction == "EINVOICE" || invoice.generateEInvoice,
                                    generateEWayBill = state.pendingComplianceAction == "EWAY" || invoice.generateEWayBill,
                                    isDistanceReadOnly = false
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::cancelComplianceAddition,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = viewModel::confirmComplianceAddition,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Proceed")
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    } else if (
                        (!invoice.generateEWayBill && state.isEWayBillAllowed) || 
                        (!invoice.generateEInvoice && state.isEInvoiceAllowed)
                    ) {
                        // ACTION BUTTONS ROW
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (!invoice.generateEInvoice && state.isEInvoiceAllowed) {
                                Button(
                                    onClick = { viewModel.initiateComplianceAddition("EINVOICE") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Add e-Invoice")
                                }
                            }
                            if (!invoice.generateEWayBill && state.isEWayBillAllowed) {
                                Button(
                                    onClick = { viewModel.initiateComplianceAddition("EWAY") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Add e-Way Bill")
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            /* ---------- Timeline ---------- */
            if (!state.showInlineTransportForm) {
                TimelineView(
                    steps = state.timeline,
                    onAction = { action ->
                        viewModel.handleTimelineAction(action)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TimelineView(
    steps: List<TimelineStep>,
    onAction: (TimelineActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(steps) { step ->
            TimelineItem(
                step = step,
                onAction = onAction
            )
        }
    }
}

@Composable
fun TimelineItem(
    step: TimelineStep,
    onAction: (TimelineActionType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (step.state) {
                StepState.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                StepState.FAILED -> MaterialTheme.colorScheme.errorContainer
                StepState.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                StepState.PENDING -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* ---------- Title ---------- */
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            /* ---------- Message ---------- */
            step.message?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            /* ---------- Progress ---------- */
            if (step.state == StepState.ACTIVE) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            /* ---------- Actions ---------- */
            if (step.primaryAction != null || step.secondaryAction != null) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    step.primaryAction?.let {
                        Button(
                            onClick = { onAction(it.action) }
                        ) {
                            Text(it.label)
                        }
                    }

                    step.secondaryAction?.let {
                        OutlinedButton(
                            onClick = { onAction(it.action) }
                        ) {
                            Text(it.label)
                        }
                    }
                }
            }
        }
    }
}
