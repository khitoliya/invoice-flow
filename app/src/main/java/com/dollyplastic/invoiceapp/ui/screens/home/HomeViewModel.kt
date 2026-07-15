package com.dollyplastic.invoiceapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.repository.PartyRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.pdf.InvoicePdfMerger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.util.Log
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import com.dollyplastic.invoiceapp.data.repository.ItemRepository
import com.dollyplastic.invoiceapp.ui.common.deletion.DeletionUiState
import com.dollyplastic.invoiceapp.ui.common.deletion.InvoiceDeletionHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: InvoiceRepository,
    private val firmRepo: FirmRepository,
    private val partyRepo: PartyRepository,
    private val itemRepo: ItemRepository,
    private val invoicePdfMerger: InvoicePdfMerger,
    private val settingsRepository: com.dollyplastic.invoiceapp.data.settings.SettingsRepository,
    @ApplicationContext private val context: Context,
    val deletionHelper: InvoiceDeletionHelper
) : ViewModel() {

    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<HomeUiEvent>()
    val events = _events.asSharedFlow()

    // DevUtils Generation State
    private val _isFixingInvoices = MutableStateFlow(false)
    val isFixingInvoices = _isFixingInvoices.asStateFlow()
    
    private val _fixProgress = MutableStateFlow(0f)
    val fixProgress = _fixProgress.asStateFlow()

    fun runInvoiceFix() {
        if (_isFixingInvoices.value) return
        
        viewModelScope.launch {
            _isFixingInvoices.value = true
            _fixProgress.value = 0f
            
            try {
                com.dollyplastic.invoiceapp.utils.DevUtils.fixDummyFinancialYear(
                    repository = repository,
                    onProgress = { current, total ->
                        val progress = current.toFloat() / total.toFloat()
                        _fixProgress.value = progress
                    }
                )
                _events.emit(HomeUiEvent.ShowToast("Invoice FY grouping fixed!"))
            } catch (e: Exception) {
                _events.emit(HomeUiEvent.ShowToast("Error fixing invoices: ${e.message}"))
            } finally {
                _isFixingInvoices.value = false
                refresh() // Refresh list automatically
            }
        }
    }

    // DevUtils Generation State
    private val _isGeneratingDummyInvoices = MutableStateFlow(false)
    val isGeneratingDummyInvoices = _isGeneratingDummyInvoices.asStateFlow()
    
    private val _dummyInvoiceProgress = MutableStateFlow(0f)
    val dummyInvoiceProgress = _dummyInvoiceProgress.asStateFlow()

    fun generateDummyInvoices() {
        if (_isGeneratingDummyInvoices.value) return
        
        viewModelScope.launch {
            _isGeneratingDummyInvoices.value = true
            _dummyInvoiceProgress.value = 0f
            
            try {
                com.dollyplastic.invoiceapp.utils.DevUtils.generateDummyInvoices(
                    repository = repository,
                    onProgress = { current, total ->
                        val progress = current.toFloat() / total.toFloat()
                        _dummyInvoiceProgress.value = progress
                    }
                )
                _events.emit(HomeUiEvent.ShowToast("Dummy invoices generated successfully!"))
            } catch (e: Exception) {
                _events.emit(HomeUiEvent.ShowToast("Error generating invoices: ${e.message}"))
            } finally {
                _isGeneratingDummyInvoices.value = false
                refresh() // Refresh list automatically
            }
        }
    }

    // State Holders
    data class FilterState(
        val searchQuery: String = "",
        val selectedFirmGstin: String? = null,
        val startDateISO: String? = null,
        val endDateISO: String? = null,
        val selectedStatus: InvoiceStatus? = null,
        val minAmount: Double? = null,
        val maxAmount: Double? = null,
        val hsnCode: String? = null,
        val selectedPartyId: String? = null
    )

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()
    
    // Selection Mode State
    private val _selectedInvoiceIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedInvoiceIds = _selectedInvoiceIds.asStateFlow()
    
    val isSelectionMode = _selectedInvoiceIds.map { it.isNotEmpty() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Summary State
    private val _summary = MutableStateFlow<Pair<Int, Double>>(0 to 0.0)
    val summary = _summary.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices = _invoices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // PDF Generation State
    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf = _isGeneratingPdf.asStateFlow()

    val firms = firmRepo.observeAllFirms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parties = partyRepo.observeAllParties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _financialYears = MutableStateFlow<List<String>>(emptyList())
    val financialYears = _financialYears.asStateFlow()

    private var currentPage = 0
    private var isLastPage = false
    private val pageSize = 50

    val items = itemRepo.observeAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock State
    data class StockItem(
        val name: String,
        val hsn: String,
        val quantity: Double,
        val unit: String
    )

    private val _stockState = MutableStateFlow<List<StockItem>>(emptyList())
    val stockState = _stockState.asStateFlow()

    init {
        loadFinancialYears()
        
        // Initial Dummy Logic for Stock
        viewModelScope.launch {
            _filterState.map { it.selectedFirmGstin }.distinctUntilChanged().collect { firmGstin ->
                updateDummyStock(firmGstin)
            }
        }
        
        // Reactive Loading
        viewModelScope.launch {
            _filterState.collectLatest { filters ->
                   if (filters.searchQuery.isNotEmpty()) {
                       kotlinx.coroutines.delay(300)
                   }
                   resetAndLoad(filters)
            }
        }
    }

    private fun updateDummyStock(gstin: String?) {
        // Dummy Data Generation
        _stockState.value = if (gstin == null) {
            // Aggregate (All Firms)
            listOf(
                StockItem("PP Granules", "3902", 1500.0, "kg"),
                StockItem("HDPE Granules", "3901", 2300.0, "kg"),
                StockItem("Packaging Rolls", "4819", 500.0, "kg"),
                StockItem("Waste Scrap", "3915", 120.0, "kg")
            )
        } else {
            // Deterministic Dummy based on Hash of GSTIN to be consistent
            val seed = gstin.hashCode()
            if (seed % 2 == 0) {
                 listOf(
                    StockItem("PP Granules", "3902", 800.0, "kg"),
                    StockItem("Packaging Rolls", "4819", 250.0, "kg")
                )
            } else {
                 listOf(
                    StockItem("HDPE Granules", "3901", 1200.0, "kg"),
                    StockItem("Waste Scrap", "3915", 80.0, "kg"),
                    StockItem("Color Masterbatch", "3206", 50.0, "kg")
                )
            }
        }
    }
    
    /* ---------- SELECTION METHODS ---------- */
    
    fun toggleSelection(invoiceId: String) {
        _selectedInvoiceIds.update { current ->
            if (current.contains(invoiceId)) current - invoiceId
            else current + invoiceId
        }
    }
    
    fun clearSelection() {
        _selectedInvoiceIds.value = emptySet()
    }
    
    fun selectAllLoaded() {
        _selectedInvoiceIds.value = _invoices.value.map { it.invoiceId }.toSet()
    }
    
    fun onDeleteSelectedList() {
        val selected = _selectedInvoiceIds.value
        if (selected.isEmpty()) return
        
        viewModelScope.launch {
             // For now, we'll just trigger the deletion flow for the first one if multiple
             // or ideally we needs a bulk deletion helper.
             // As a temporary fix to restore "Delete Icon" functionality without complex bulk compliance logic:
             val firstId = selected.firstOrNull()
             val invoice = _invoices.value.find { it.invoiceId == firstId }
             if (invoice != null) {
                 // Trigger existing single deletion flow (User can repeat or we improve later)
                 // This ensures safety over a careless bulk delete loop
                 deletionHelper.onRequestDeletion(invoice)
             }
        }
    }

    /* ---------- FILTER METHODS ---------- */

    fun updateFilters(update: (FilterState) -> FilterState) {
        _filterState.update(update)
    }
    
    fun onSearchQueryChange(query: String) = updateFilters { it.copy(searchQuery = query) }
    fun onFirmSelected(gstin: String?) = updateFilters { it.copy(selectedFirmGstin = gstin) }
    fun onPartySelected(partyId: String?) = updateFilters { it.copy(selectedPartyId = partyId) }
    fun onFinancialYearSelected(fy: String?) {
        _financialYears.value 
    }
    
    fun onDateRangeSelected(startISO: String?, endISO: String?) = updateFilters { it.copy(startDateISO = startISO, endDateISO = endISO) }
    fun onStatusSelected(status: InvoiceStatus?) = updateFilters { it.copy(selectedStatus = status) }
    
    fun resetAndLoad(filters: FilterState) {
        currentPage = 0
        isLastPage = false
        _invoices.value = emptyList()
        clearSelection()
        loadNextPage()
        loadSummary()
    }
    
    fun refresh() {
        resetAndLoad(_filterState.value)
    }

    private fun parseIsoToEpoch(isoDate: String?): Long? {
        if (isoDate.isNullOrBlank()) return null
        return try {
            java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).parse(isoDate)?.time
        } catch (e: Exception) {
            null
        }
    }

    fun loadNextPage() {
        if (_isLoading.value || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true
            val filters = _filterState.value
            
            val firm = filters.selectedFirmGstin
            val party = filters.selectedPartyId
            val query = filters.searchQuery.ifBlank { null }
            
            Log.d("HomeViewModel", ">>> LOADING PAGE $currentPage <<<")
            Log.d("HomeViewModel", "Filters: UseEpoch=true")
            
            val result = repository.getInvoicesPaged(
                firmGstin = firm,
                query = query,
                partyId = party,
                status = filters.selectedStatus,
                minAmount = filters.minAmount,
                maxAmount = filters.maxAmount,
                hsnCode = filters.hsnCode,
                startEpoch = parseIsoToEpoch(filters.startDateISO),
                endEpoch = parseIsoToEpoch(filters.endDateISO),
                limit = pageSize,
                offset = currentPage * pageSize
            )
            
            if (result is Result.Success) {
                val newItems = result.data
                Log.d("HomeViewModel", "Loaded ${newItems.size} items.")
                
                if (newItems.size < pageSize) {
                    isLastPage = true
                }
                _invoices.update { it + newItems }
                currentPage++
            } else {
                Log.e("HomeViewModel", "Failed to load invoices")
            }
            _isLoading.value = false
        }
    }
    
    private fun loadSummary() {
         viewModelScope.launch {
            val filters = _filterState.value
            val result = repository.getFilteredSummary(
                firmGstin = filters.selectedFirmGstin,
                query = filters.searchQuery.ifBlank { null },
                partyId = filters.selectedPartyId,
                status = filters.selectedStatus,
                minAmount = filters.minAmount,
                maxAmount = filters.maxAmount,
                hsnCode = filters.hsnCode,
                startEpoch = parseIsoToEpoch(filters.startDateISO),
                endEpoch = parseIsoToEpoch(filters.endDateISO)
            )
            if (result is Result.Success) {
                _summary.value = result.data
            }
         }
    }

    /* ---------- DELETION LOGIC ---------- */
    
    fun onDeleteClicked(invoice: Invoice) {
        viewModelScope.launch {
            deletionHelper.onRequestDeletion(invoice)
        }
    }
    
    fun dismissDeleteDialog() {
        deletionHelper.dismiss()
    }
    
    fun confirmHardDelete(invoice: Invoice) {
        viewModelScope.launch {
            val result = deletionHelper.onConfirmHardDelete(invoice)
            if (result is Result.Success) {
                // Remove from local list
                _invoices.update { current -> current.filter { it.invoiceId != invoice.invoiceId } }
                loadSummary()
                viewModelScope.launch { _events.emit(HomeUiEvent.ShowToast("Invoice deleted successfully")) }
            } else {
                viewModelScope.launch { _events.emit(HomeUiEvent.ShowToast("Failed to delete invoice")) }
            }
        }
    }
    
    fun confirmCancellation(invoice: Invoice, remark: String) {
        viewModelScope.launch {
             val result = deletionHelper.onConfirmArchive(invoice, remark)
             if (result is Result.Success) {
                 // Remove from local list (assuming archived are hidden)
                 _invoices.update { current -> current.filter { it.invoiceId != invoice.invoiceId } }
                 loadSummary()
                 viewModelScope.launch { _events.emit(HomeUiEvent.ShowToast("Invoice cancelled & archived")) }
             } else {
                 viewModelScope.launch { _events.emit(HomeUiEvent.ShowToast("Failed to cancel invoice")) }
             }
        }
    }
    
    fun openPortalForCancellation() {
        viewModelScope.launch {
             val inv = deletionHelper.uiState.value.let { 
                 if (it is DeletionUiState.ShowComplianceWarning) it.invoice else null
             } ?: return@launch
             
             // Construct Portal URL and Creds
             val url = com.dollyplastic.invoiceapp.data.utils.PortalUtils.getPortalUrl(inv)
             val mode = com.dollyplastic.invoiceapp.data.utils.PortalUtils.getPortalMode(inv, url)

             // Ideally fetch creds from repo, passing empty for now or existing logic
             val json = "[]" 
             
             val storageRef = InvoiceStorageRef(
                firmName = InvoiceStorage.getFirmIdentifier(inv.firm),
                financialYear = inv.financialYear,
                invoiceNumber = inv.invoiceNumber
            )

             _events.emit(HomeUiEvent.NavigateToPortal(inv.invoiceId, url, storageRef, json, true, mode))
        }
    }
    
    fun generateCombinedPdf(
        invoiceIds: Set<String>, 
        pageTypes: Set<InvoicePdfMerger.PageType>,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                // 1. Fetch full invoice objects for selected IDs
                val selectedInvoices = _invoices.value.filter { invoiceIds.contains(it.invoiceId) }
                    .sortedByDescending { it.invoiceNumber } 
                
                if (selectedInvoices.isEmpty()) {
                    onError("No valid invoices selected")
                    return@launch
                }

                // 2. Determine File Path
                val file = InvoiceStorage.getMergedPdfFile(selectedInvoices.size)

                // 3. Generate
                withContext(Dispatchers.IO) {
                    val result = invoicePdfMerger.generateCombinedPdf(context, selectedInvoices, pageTypes, file)
                    result.onSuccess { 
                        withContext(Dispatchers.Main) { onSuccess(it) }
                    }.onFailure { 
                        withContext(Dispatchers.Main) { onError(it.message ?: "Unknown Error") }
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to generate PDF")
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }


    
    private fun loadFinancialYears() {
        viewModelScope.launch {
            repository.observeFinancialYears().collect {
                _financialYears.value = it
            }
        }
    }
    fun onShareClicked(invoice: Invoice) {
        viewModelScope.launch {
            val file = InvoiceStorage.getFinalPdfFile(invoice.firm, invoice)
            if (file.exists()) {
                val contact = settingsRepository.getWhatsAppContact()
                _events.emit(HomeUiEvent.ShareInvoice(file, contact))
            } else {
                _events.emit(HomeUiEvent.ShowToast("PDF not found. Please generate it first."))
            }
        }
    }
}




sealed interface HomeUiEvent {
    data class NavigateToPortal(val invoiceId: String, val url: String, val storageRef: InvoiceStorageRef, val credentialsJson: String, val isCancellation: Boolean, val mode: String) : HomeUiEvent
    data class ShowToast(val message: String) : HomeUiEvent
    data class ShareInvoice(val file: File, val contact: Pair<String, String>?) : HomeUiEvent
}
