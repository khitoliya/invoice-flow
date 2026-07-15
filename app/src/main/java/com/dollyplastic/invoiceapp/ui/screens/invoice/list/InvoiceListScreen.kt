package com.dollyplastic.invoiceapp.ui.screens.invoice.list

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.ui.components.InvoiceRowItem
import com.dollyplastic.invoiceapp.ui.components.FirmPillTab
import com.dollyplastic.invoiceapp.ui.components.MiniSummaryRow
import com.dollyplastic.invoiceapp.ui.common.SelectionContextBar
import com.dollyplastic.invoiceapp.pdf.InvoicePdfMerger.PageType
import com.dollyplastic.invoiceapp.ui.components.deletion.InvoiceDeletionDialogs
import com.dollyplastic.invoiceapp.ui.screens.home.HomeUiEvent
import com.dollyplastic.invoiceapp.ui.screens.home.HomeViewModel
import com.dollyplastic.invoiceapp.ui.screens.home.components.FilterSheetContent
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InvoiceListScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateToProcessing: (String) -> Unit,
    onNavigateToPortal: (String, String, InvoiceStorageRef, String, Boolean, String) -> Unit
) {
    val filters by viewModel.filterState.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val firms by viewModel.firms.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val items by viewModel.items.collectAsState()
    
    val selectedIds by viewModel.selectedInvoiceIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val deletionUiState by viewModel.deletionHelper.uiState.collectAsState()

    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    var isSearchActive by remember { mutableStateOf(false) }
    
    // Merge PDF State
    var showMergeDialog by remember { mutableStateOf(false) }
    
    // Bottom Sheet State
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current
    val listState = rememberLazyListState()

    /* ---------- Navigation Events ---------- */
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToPortal ->
                    onNavigateToPortal(event.invoiceId, event.url, event.storageRef, event.credentialsJson, event.isCancellation, event.mode)
                is HomeUiEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is HomeUiEvent.ShareInvoice -> {
                    // (Same share logic as before)
                    val file = event.file
                    val contact = event.contact
                    try {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            if (contact != null) {
                                val number = contact.second
                                val finalNumber = if (number.length == 10) "91$number" else number
                                setPackage("com.whatsapp")
                                putExtra("jid", "$finalNumber@s.whatsapp.net")
                            }
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallback = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(fallback, "Share Invoice"))
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
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

    val bottomBarVisibility = com.dollyplastic.invoiceapp.ui.navigation.LocalBottomBarVisibility.current
    LaunchedEffect(isSelectionMode) {
        bottomBarVisibility.value = !isSelectionMode
    }

    // Pagination
    LaunchedEffect(listState) {
        androidx.compose.runtime.snapshotFlow { 
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0)
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 5)
        }.collect { shouldLoad ->
            if (shouldLoad) viewModel.loadNextPage()
        }
    }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- Grouping Logic ---
    val groupedInvoices = remember(invoices) {
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
        )
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
        
        invoices.groupBy { invoice ->
            var formattedDate = "UNKNOWN DATE"
            for (formatter in formatters) {
                try {
                    val date = LocalDate.parse(invoice.invoiceDate, formatter)
                    formattedDate = date.format(monthFormatter).uppercase()
                    break // specific success
                } catch (e: Exception) {
                    // continue to next format
                }
            }
            formattedDate
        }
    }


    
    // --- Pull to Refresh ---
    val pullRefreshState = rememberPullToRefreshState()
    
    // Sync internal loading state with PullToRefresh state
    // We only end refresh to hide the manual pull indicator. We do not start it programmatically
    // to avoid the spinner dropping down on firm selection or other loads.
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            pullRefreshState.endRefresh()
        }
    }
    
    // Trigger refresh when user pulls
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            if (!isLoading) {
                viewModel.refresh()
            }
        }
    }

    // FAB Expansion Logic: Expanded at top, collapsed when scrolling down
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !isSelectionMode,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToCreate,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        expanded = expandedFab,
                        icon = { Icon(Icons.Default.Add, "Create Invoice") },
                        text = { Text("Create Invoice") }
                    )
                }
            },
            bottomBar = {
               // BottomBar is handled by MainScaffold via LocalBottomBarVisibility
            },
            containerColor = AppColors.Muted
        ) { padding ->
            
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                
                // 1. Firm Switcher (Scrollable Pills)
                if (firms.size > 1 && !isSelectionMode) {
                    com.dollyplastic.invoiceapp.ui.components.AnimatedFirmSelectionRow(
                        firms = firms,
                        selectedFirmGstin = filters.selectedFirmGstin,
                        onFirmSelected = { viewModel.onFirmSelected(it) }
                    )
                }

                // 2. Filter & Search Row (Fixed)
                if (!isSelectionMode) {
                    Surface(
                         color = AppColors.Muted,
                         modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        if (isSearchActive || filters.searchQuery.isNotEmpty()) {
                            // Expanded Search
                            TextField(
                                value = filters.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                placeholder = { Text("Search Invoice #") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                trailingIcon = {
                                    IconButton(onClick = { 
                                        viewModel.onSearchQueryChange("")
                                        isSearchActive = false 
                                    }) {
                                        Icon(Icons.Default.Close, "Close")
                                    }
                                }
                            )
                        } else {
                            // Sticky Filters
                            ActiveFiltersRow(
                                filters = filters,
                                parties = parties,
                                onDateClear = { viewModel.updateFilters { it.copy(startDateISO = null, endDateISO = null) } },
                                onStatusClear = { viewModel.onStatusSelected(null) },
                                onPartyClear = { viewModel.onPartySelected(null) },
                                onAmountClear = { viewModel.updateFilters { it.copy(minAmount = null, maxAmount = null) } },
                                onHsnClear = { viewModel.updateFilters { it.copy(hsnCode = null) } },
                                onSheetOpen = { showFilterSheet = true },
                                onSearchClick = { isSearchActive = true }
                            )
                        }
                    }
                }

                // 3. Invoice List
                if (invoices.isEmpty() && !isLoading) {
                     EmptyState(modifier = Modifier.fillMaxSize(), onCreateClick = onNavigateToCreate)
                } else if (invoices.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {


                        // Grouped Invoices
                        groupedInvoices.forEach { (month, monthInvoices) ->
                             stickyHeader {
                                 Surface(
                                     modifier = Modifier.fillMaxWidth(),
                                     color = MaterialTheme.colorScheme.background // Or slightly transparent
                                 ) {
                                     Row(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .padding(horizontal = 24.dp, vertical = 12.dp),
                                         horizontalArrangement = Arrangement.SpaceBetween,
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Text(
                                             text = month,
                                             style = MaterialTheme.typography.labelMedium,
                                             color = MaterialTheme.colorScheme.primary,
                                             fontWeight = FontWeight.Bold
                                         )
                                         
                                         // Monthly Taxable Summary
                                         val monthlyTaxable = monthInvoices.sumOf { it.totalTaxableValue }
                                         if (monthlyTaxable > 0) {
                                             Text(
                                                 text = "Taxable: ${com.dollyplastic.invoiceapp.domain.Utils.FormatUtils.formatCurrency(monthlyTaxable)}",
                                                 style = MaterialTheme.typography.labelSmall,
                                                 color = MaterialTheme.colorScheme.secondary,
                                                 fontWeight = FontWeight.Medium
                                             )
                                         }
                                     }
                                 }
                             }
                             
                             items(
                                 items = monthInvoices,
                                 key = { it.invoiceId },
                                 contentType = { "invoice_row" }
                             ) { invoice ->
                                 InvoiceRowItem(
                                     invoice = invoice,
                                     isSelected = selectedIds.contains(invoice.invoiceId),
                                     isSelectionMode = isSelectionMode,
                                     showFirmName = filters.selectedFirmGstin == null, // Show firm name if "All Firms" is selected
                                     onClick = {
                                         if (isSelectionMode) viewModel.toggleSelection(invoice.invoiceId)
                                         else onNavigateToProcessing(invoice.invoiceId)
                                     },
                                     onLongClick = { viewModel.toggleSelection(invoice.invoiceId) },
                                     onDelete = { viewModel.onDeleteClicked(invoice) },
                                     onShare = { viewModel.onShareClicked(invoice) }
                                 )
                             }
                        }
                    }
                }
            }
        }
        
        // Pull to Refresh Indicator
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Floating Selection Context Bar
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp) 
        ) {
            SelectionContextBar(
                count = selectedIds.size,
                onClear = { viewModel.clearSelection() },
                onDelete = { viewModel.onDeleteSelectedList() },
                onShare = {
                    val inv = invoices.find { it.invoiceId == selectedIds.firstOrNull() }
                    if (inv != null) viewModel.onShareClicked(inv)
                },
                onMerge = { showMergeDialog = true },
                onSelectAll = { viewModel.selectAllLoaded() },
                selectedIds = selectedIds,
                invoices = invoices,
                mergeOptions = {
                    if (showMergeDialog) {
                         MergeOptionsPopup(
                            onDismiss = { showMergeDialog = false },
                            onConfirm = { selectedTypes ->
                                showMergeDialog = false
                                viewModel.generateCombinedPdf(
                                    invoiceIds = selectedIds,
                                    pageTypes = selectedTypes,
                                    onSuccess = { file ->
                                        shareFile(context, file)
                                        viewModel.clearSelection()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }
            )
        }
    }
    
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = null
        ) {
            FilterSheetContent(
                currentFilters = filters,
                parties = parties,
                items = items,
                onApply = { newFilters -> 
                    viewModel.updateFilters { newFilters }
                    showFilterSheet = false 
                },
                onClear = { 
                    viewModel.updateFilters { HomeViewModel.FilterState() }
                    showFilterSheet = false 
                },
                onClose = { showFilterSheet = false }
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, onCreateClick: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Description, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text("No invoices found.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Create your first invoice to get started.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreateClick) {
            Text("Create Invoice")
        }
    }
}



@Composable
fun ActiveFiltersRow(
    parties: List<Party>,
    filters: HomeViewModel.FilterState,
    onDateClear: () -> Unit,
    onStatusClear: () -> Unit,
    onPartyClear: () -> Unit,
    onAmountClear: () -> Unit,
    onHsnClear: () -> Unit,
    onSheetOpen: () -> Unit,
    onSearchClick: () -> Unit
) {
    val hasFilters = filters.startDateISO != null || filters.selectedStatus != null || filters.selectedPartyId != null || filters.minAmount != null || filters.maxAmount != null || !filters.hsnCode.isNullOrBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scrollable Filters (Weight 1)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            if (!hasFilters) {
                // RESTING STATE
                Surface(
                    onClick = onSheetOpen,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters", modifier = Modifier.size(18.dp).align(Alignment.CenterVertically))
                        Text("Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // ACTIVE STATE
                // Circular '+' button
                Surface(
                    onClick = onSheetOpen,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border),
                    color = Color.White,
                    modifier = Modifier.size(40.dp)
                ) {
                     Box(contentAlignment = Alignment.Center) {
                         Icon(Icons.Default.Add, contentDescription = "Add Filter", tint = AppColors.TextPrimary)
                     }
                }

                // Date Chip
                if (filters.startDateISO != null) {
                    val startParsed = filters.startDateISO?.let {
                        try { 
                            val sdfIn = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
                            val sdfOut = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                            sdfOut.format(sdfIn.parse(it)!!)
                        } catch(e:Exception) { it }
                    } ?: ""
                    val endParsed = filters.endDateISO?.let {
                        try { 
                            val sdfIn = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
                            val sdfOut = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                            sdfOut.format(sdfIn.parse(it)!!)
                        } catch(e:Exception) { it }
                    } ?: ""
                    val dateLabel = if (startParsed == endParsed) startParsed else "$startParsed - $endParsed"
                    ActiveFilterChip(label = dateLabel, onClear = onDateClear)
                }
                // Status Chip
                if (filters.selectedStatus != null) {
                    ActiveFilterChip(label = if (filters.selectedStatus == InvoiceStatus.DRAFT) "PENDING" else filters.selectedStatus.name, onClear = onStatusClear)
                }
                // Amount Chip
                if (filters.minAmount != null || filters.maxAmount != null) {
                     val label = if (filters.minAmount != null && filters.maxAmount != null) "₹${filters.minAmount.toInt()} - ₹${filters.maxAmount.toInt()}" 
                                 else if (filters.minAmount != null) "> ₹${filters.minAmount.toInt()}"
                                 else "< ₹${filters.maxAmount!!.toInt()}"
                     ActiveFilterChip(label = label, onClear = onAmountClear)
                }
                // HSN Chip
                if (!filters.hsnCode.isNullOrBlank()) {
                     ActiveFilterChip(label = "HSN: ${filters.hsnCode}", onClear = onHsnClear)
                }
                // Party Chip
                 if (filters.selectedPartyId != null) {
                     val partyInfo = parties.find { it.partyId == filters.selectedPartyId }
                     val partyName = partyInfo?.nickName?.ifBlank { partyInfo.tradeName } ?: "Unknown Party"
                     ActiveFilterChip(label = partyName, onClear = onPartyClear)
                }
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        // Fixed Search Icon
        IconButton(onClick = onSearchClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = AppColors.PrimaryBlue)
        }
    }
}

@Composable
fun ActiveFilterChip(label: String, onClear: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = AppColors.textFieldGrey, // Light gray
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp)
            )
            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp).clickable { onClear() }, tint = AppColors.TextSecondary)
        }
    }
}

fun shareInvoice(context: android.content.Context, invoice: Invoice) {
    val file = InvoiceStorage.getFinalPdfFile(invoice.firm, invoice) 
    if (file.exists()) {
        shareFile(context, file)
    } else {
        Toast.makeText(context, "PDF not found. Please view/generate it first.", Toast.LENGTH_SHORT).show()
    }
}

fun shareFile(context: android.content.Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}



@Composable
fun MergeOptionsPopup(
    onDismiss: () -> Unit,
    onConfirm: (Set<PageType>) -> Unit
) {
    val options = PageType.values()
    val selected = remember { mutableStateMapOf<PageType, Boolean>().apply {
        put(PageType.ORIGINAL, true)
        put(PageType.DUPLICATE, true)
        put(PageType.TRIPLICATE, true)
    }}

    // Use DropdownMenu for built-in animation and smart positioning (opens up if at bottom)
    DropdownMenu(
        expanded = true, // Managed by caller presence
        onDismissRequest = onDismiss,
        modifier = Modifier.width(260.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Include Pages:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            options.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected[type] = !(selected[type] ?: false) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selected[type] ?: false,
                        onCheckedChange = { selected[type] = it },
                        modifier = Modifier.size(20.dp) 
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = when(type) {
                            PageType.ORIGINAL -> "Original"
                            PageType.DUPLICATE -> "Duplicate"
                            PageType.TRIPLICATE -> "Triplicate"
                            PageType.EWAY_BILL -> "E-Way Bill"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val finalSet = selected.filter { it.value }.keys.toSet()
                    if (finalSet.isNotEmpty()) onConfirm(finalSet)
                    else onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Generate PDF")
            }
        }
    }
}
