package com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.AdditionalDetails
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceItem
import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.data.models.Party
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.dollyplastic.invoiceapp.data.models.TransportDetails
import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.repository.ItemRepository
import com.dollyplastic.invoiceapp.data.repository.PartyRepository
import com.dollyplastic.invoiceapp.domain.Usecase.AddInvoiceUseCase
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import com.dollyplastic.invoiceapp.domain.Utils.FinancialYearUtils
import com.dollyplastic.invoiceapp.domain.Validation.InvoiceValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationDependencies
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import com.dollyplastic.invoiceapp.ui.screens.invoice.A_HeaderSection.InvoiceNumberGenerator
import com.dollyplastic.invoiceapp.ui.screens.invoice.E_TaxSummary.InvoiceCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.pdf.InvoicePdfGenerator

import androidx.lifecycle.SavedStateHandle
import com.dollyplastic.invoiceapp.data.models.DeliveryType
import com.dollyplastic.invoiceapp.data.models.TransportMode

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val repo: InvoiceRepository,
    private val firmRepo: FirmRepository,
    private val partyRepo: PartyRepository,
    private val itemRepo: ItemRepository,
    private val distanceRepository: com.dollyplastic.invoiceapp.data.repository.DistanceRepository,
    private val addInvoiceUseCase: AddInvoiceUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state =
        MutableStateFlow(InvoiceFormState())
    val state = _state.asStateFlow()

    private val _events =
        MutableSharedFlow<InvoiceUiEvent>()
    val events = _events.asSharedFlow()

    private val _firms = MutableStateFlow<List<Firm>>(emptyList())
    val firms = _firms.asStateFlow()

    private val _parties = MutableStateFlow<List<Party>>(emptyList())
    val parties = _parties.asStateFlow()

    private val _itemsMaster = MutableStateFlow<List<Item>>(emptyList())
    val itemsMaster = _itemsMaster.asStateFlow()

    init {
        loadMasters()
        
        savedStateHandle.get<String>("invoiceId")?.let { id ->
            if (id.isNotBlank()) loadInvoice(id)
        } ?: run {
             // New Invoice: Set Today's Date
             setInvoiceDate(DateUtils.today())
        }
    }

    private fun loadInvoice(id: String) {
        viewModelScope.launch {
            val result = repo.getInvoice(id)
            if (result is Result.Success) {
                val inv = result.data
                _state.update {
                    it.copy(
                        isEditing = true,
                        invoiceId = inv.invoiceId,
                        invoiceNumber = inv.invoiceNumber,
                        invoiceSequence = inv.invoiceSequence,
                        invoiceDate = inv.invoiceDate,
                        financialYear = inv.financialYear,
                        firm = inv.firm,
                        isCashSale = inv.isCashSale,
                        billToParty = inv.billToParty,
                        shipToParty = inv.shipToParty,
                        items = inv.items,
                        taxSummary = inv.taxSummary,
                        totalTaxableValue = inv.totalTaxableValue,
                        totalTaxAmount = inv.totalTaxAmount,
                        totalInvoiceValue = inv.totalInvoiceValue,
                        transportDetails = inv.transportDetails,
                        generateEInvoice = inv.generateEInvoice,
                        generateEWayBill = inv.generateEWayBill,
                        additionalDetails = inv.additionalDetails ?: AdditionalDetails()
                    )
                }
                checkComplianceCompatibility(inv.invoiceDate, isInitialLoad = true)
                recalc()
                if (inv.transportDetails.distance == 0) {
                    autoFillDistance()
                }
                validate()
            }
        }
    }

    private fun loadMasters() {
        viewModelScope.launch {
            firmRepo.getAllFirms().let {
                if (it is Result.Success) _firms.value = it.data
            }
            partyRepo.getAllParties().let {
                if (it is Result.Success) _parties.value = it.data
            }
            itemRepo.getAllItems().let {
                if (it is Result.Success) _itemsMaster.value = it.data
            }
        }
    }



    /* ---------- HEADER ---------- */

// Voiding this intended step to fix the previous error.

    fun setInvoiceDate(date: String) {
        val fy = FinancialYearUtils.fromDate(DateUtils.parse(date))

        _state.update {
            it.copy(
                invoiceDate = date,
                financialYear = fy
            )
        }

        checkComplianceCompatibility(date)
        fetchLastInvoiceEpoch()
        
        regenerateInvoiceNumber()
        validate()
    }


    private fun regenerateInvoiceNumber() {
        // Set loading state
        _state.update { it.copy(isGeneratingInvoiceNumber = true) }
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Simulate delay for loader visibility

            val firm = _state.value.firm 
            if (firm == null) {
                _state.update { it.copy(isGeneratingInvoiceNumber = false) }
                return@launch
            }
            
            val date = _state.value.invoiceDate

            val (seq, number) =
                InvoiceNumberGenerator.generate(
                    repo,
                    firm,
                    date
                )

            _state.update {
                it.copy(
                    invoiceSequence = seq,
                    invoiceNumber = number,
                    isGeneratingInvoiceNumber = false
                )
            }
            validate() // Re-validate with new number
        }
    }

    /* ---------- PARTY ---------- */

    fun setCashSale(isCash: Boolean) {
        android.util.Log.d("InvoiceViewModel", "setCashSale called with: $isCash")
        _state.update {
            it.copy(
                isCashSale = isCash,
                billToParty = if (isCash) null else it.billToParty,
                shipToParty = if (isCash) null else it.shipToParty
            )
        }
        recalc()
        validate()
    }

    /* ... */



// Methods moved to VALIDATION HELPERS section to consolidate dirty state logic

    /* ---------- ITEMS (NOW ACTUALLY USED) ---------- */

    fun addItem(line: InvoiceItem) {
        _state.update { it.copy(items = it.items + line) }
        recalc()
        validate()
    }

    fun updateItem(index: Int, line: InvoiceItem) {
        _state.update {
            it.copy(
                items = it.items.toMutableList().apply {
                    this[index] = line
                }
            )
        }
        recalc()
        validate()
    }

    fun removeItem(index: Int) {
        _state.update {
            it.copy(
                items = it.items.toMutableList().apply {
                    removeAt(index)
                }
            )
        }
        recalc()
        validate()
    }

    /* ---------- TRANSPORT ---------- */

    fun updateTransport(details: TransportDetails) {
        val oldDetails = _state.value.transportDetails
        _state.update { it.copy(transportDetails = details) }
        
        // Fix: Auto-fetch distance if switching from Buyer Pickup (or unknown) to a type that needs distance
        if (details.deliveryType != com.dollyplastic.invoiceapp.data.models.DeliveryType.BUYER_PICKUP 
            && details.distance == 0
            && (oldDetails.deliveryType == com.dollyplastic.invoiceapp.data.models.DeliveryType.BUYER_PICKUP || oldDetails.distance == 0)
        ) {
             // Only if distance is 0, attempt to fetch. 
             // IMPORTANT: prevent recursion if autoFillDistance calls updateTransport
             autoFillDistance()
        }

        // Immediate Feedback: Mark fields as touched if they are being edited
        if (!details.vehicleNumber.isNullOrBlank() && details.vehicleNumber != oldDetails.vehicleNumber) {
            markFieldTouched("vehicleNumber")
        }
        
        // Check for immediate validation (Vehicle Number etc)
        validate()
    }

    /* ---------- COMPLIANCE ---------- */

    fun setEInvoice(flag: Boolean) {
        _state.update {
            it.copy(generateEInvoice = flag, transportDetails = if (flag && it.transportDetails.deliveryType == DeliveryType.BUYER_PICKUP) {
                it.transportDetails.copy(deliveryType = DeliveryType.OWN_VEHICLE)
            } else {
                it.transportDetails
            });
        }

        validate()
    }

    fun setEWay(flag: Boolean) {
        _state.update {
            it.copy(generateEWayBill = flag, transportDetails = if (flag && it.transportDetails.deliveryType == DeliveryType.BUYER_PICKUP) {
                it.transportDetails.copy(deliveryType = DeliveryType.OWN_VEHICLE)
            } else {
                it.transportDetails
            });
        }
        validate()
    }

    fun updateAdditionalDetails(details: AdditionalDetails) {
        _state.update {
            it.copy(additionalDetails = details)
        }
    }

    private fun autoFillDistance() {
        val s = _state.value
        val firm = s.firm
        val party = s.shipToParty ?: s.billToParty

        if (firm != null && party != null) {
            viewModelScope.launch {
                val dist = distanceRepository.getPincodeDistance(firm.pincode, party.pincode)
                
                if (dist != null && dist > 0) {
                     // Found in DB/Firestore -> Auto-fill and Lock
                     val currentTransport = _state.value.transportDetails
                     val newTransport = currentTransport.copy(distance = dist)
                     
                     // Update state directly to avoid recursion or extra events
                     _state.update { 
                         it.copy(
                             transportDetails = newTransport, 
                             isDistanceReadOnly = true 
                         )
                     }
                     // No need to call updateTransport() which might re-trigger logic.
                     validate() 
                } else {
                    // Not found -> Unlock for manual entry
                     _state.update { 
                         it.copy(isDistanceReadOnly = false) 
                     }
                }
            }
        } else {
             // Missing firm/party -> Unlock (should accept manual input if needed? Or wait for selection?)
             // Defaulting to unlocked seems safer.
             _state.update { it.copy(isDistanceReadOnly = false) }
        }
    }



    /* ---------- VALIDATION ---------- */

    private fun validate() {
        val firm = _state.value.firm
        val errorsMap = mutableMapOf<String, String>()

        // 1. Validate Firm Presence
        if (firm == null) {
            errorsMap["firm"] = "Seller firm is required"
        }

        // 2. Validate Components via Partial Checks (even if firm is null)
        
        // --- Party Validation ---
        if (!_state.value.isCashSale && _state.value.billToParty == null) {
             errorsMap["billToParty"] = "Buyer (Bill To) is required"
        }
        
        val transportErrors = com.dollyplastic.invoiceapp.domain.Validation.TransportValidator.validate(
            _state.value.transportDetails,
            ValidationLevel.BASE,

        )
        transportErrors.forEach { errorsMap[it.field] = it.message }

        val itemErrors = _state.value.items.flatMap { 
            com.dollyplastic.invoiceapp.domain.Validation.ItemValidator.validate(it.item, ValidationLevel.BASE)
        }
        itemErrors.forEach { errorsMap[it.field] = it.message }
        
        if (_state.value.items.isEmpty()) {
             errorsMap["items"] = "At least one item required"
        }

        // 3. Full Validation (Only if firm exists, to catch complex cross-field rules)
        if (firm != null) {
            val invoice = buildInvoice()
            val result = InvoiceValidator.validate(invoice, ValidationLevel.BASE)
            if (result is ValidationResult.Invalid) {
                result.errors.forEach { errorsMap[it.field] = it.message }
            }
        }
        
        // 4. Backdate rule validation
        if (!_state.value.isEditing) {
            val selectedEpoch = try {
                val f = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                f.parse(_state.value.invoiceDate)?.time ?: 0L
            } catch (e: Exception) { 0L }
            
            val lastEpoch = _state.value.lastInvoiceDateEpoch ?: 0L
            if (selectedEpoch > 0L && lastEpoch > 0L && selectedEpoch < lastEpoch) {
                val lastDateStr = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(java.util.Date(lastEpoch))
                errorsMap["invoiceDate"] = "Invoice date cannot be before the last bill date ($lastDateStr)"
            }
        }

        _state.update {
            it.copy(errors = errorsMap)
        }


        android.util.Log.d("InvoiceValidation", "Validation Errors Map: ${_state.value.errors}")
        android.util.Log.d("InvoiceValidation", "isFormValid: ${_state.value.isFormValid}")
        android.util.Log.d("InvoiceValidation", "Touched Fields: ${_state.value.touchedFields}")
        android.util.Log.d("InvoiceValidation", "Show All Errors: ${_state.value.showAllErrors}")
    }
    

    /* ---------- SAVE ---------- */

    /* ---------- VALIDATION HELPERS ---------- */

    fun onFieldBlur(field: String) {
        android.util.Log.d("InvoiceValidation", "onFieldBlur: $field")
        
        // 1. Get all prerequisites
        val dependencies = ValidationDependencies.getAllDependencies(field)

        // 2. Mark current field AND all prerequisites as touched
        _state.update {
            it.copy(touchedFields = it.touchedFields + field + dependencies)
        }
        
        // Force validation to update errors map
        validate() 
    }

    private fun markFieldTouched(field: String) {
        android.util.Log.d("InvoiceValidation", "markFieldTouched: $field")

        // 1. Get all prerequisites
        val dependencies = ValidationDependencies.getAllDependencies(field)
        
        // 2. Mark current field AND all prerequisites as touched
        _state.update {
            it.copy(touchedFields = it.touchedFields + field + dependencies)
        }
        
        // Force validation to update errors map
        validate()
        android.util.Log.d("InvoiceValidation", "Current Errors: ${_state.value.errors}")
    }

    /* ---------- HEADER ---------- */

    fun setFirm(firm: Firm) {
        android.util.Log.d("InvoiceViewModel", "setFirm called: ${firm.tradeName}")
        _state.update { it.copy(firm = firm) }
        markFieldTouched("firm")
        
        fetchLastInvoiceEpoch()
        
        regenerateInvoiceNumber()
        recalc()
        validate()
        autoFillDistance()
    }

    fun clearFirm() {
        android.util.Log.d("InvoiceViewModel", "clearFirm called. Previous firm: ${_state.value.firm?.tradeName}")
        _state.update { it.copy(firm = null) }
        markFieldTouched("firm")
        validate() // Will likely fail validation, which is correct
    }

    /* ... */

    /* ---------- PARTY ---------- */

    fun setShipToSameAsBillTo(flag: Boolean) {
        _state.update {
            it.copy(
                shipToSameAsBillTo = flag,
                shipToParty = if (flag) it.billToParty else null
            )
        }
        if (flag && _state.value.billToParty != null) {
            markFieldTouched("shipToParty")
        }
        validate()
        autoFillDistance()
    }

    fun setBillToParty(party: Party?) {
        _state.update {
            it.copy(
                billToParty = party,
                shipToParty =
                    if (it.shipToSameAsBillTo) party else it.shipToParty
            )
        }
        markFieldTouched("billToParty")
        if (_state.value.shipToSameAsBillTo) {
             markFieldTouched("shipToParty")
        }
        recalc()
        validate()
        autoFillDistance()
    }

    fun setShipToParty(party: Party?) {
        _state.update { it.copy(shipToParty = party) }
        markFieldTouched("shipToParty")
        validate()
        autoFillDistance()
    }

    /* ... */

    /* ---------- SAVE ---------- */

    fun requestSave() {
        // Show all errors when user attempts to save
        _state.update { it.copy(showAllErrors = true) }
        
        if (_state.value.isFormValid) {
            viewModelScope.launch {
                _events.emit(InvoiceUiEvent.ShowConfirmDialog)
            }
        } else {
             // If invalid, maybe show a toast or just let the red errors appear (which they will now)
             viewModelScope.launch {
                 val errorMsg = _state.value.errors.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                 // We could emit a "Check Errors" toast here if desired
             }
        }
    }


    fun confirmSave(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            val invoice= buildInvoice()
            val result =
                addInvoiceUseCase.execute(invoice)

            _state.update { it.copy(isSaving = false) }

            if (result is ValidationResult.Valid) {
                
                // Save Distance if manually entered/valid
                val dist = invoice.transportDetails.distance
                val destParty = invoice.shipToParty ?: invoice.billToParty
                if (dist > 0 && destParty != null) {
                    distanceRepository.setPincodeDistance(invoice.firm.pincode, destParty.pincode, dist)
                }

                val needsProcessing = invoice.generateEInvoice || invoice.generateEWayBill
                _events.emit(
                    InvoiceUiEvent.InvoiceSaved(
                        invoiceId = invoice.invoiceId,
                        needsProcessing = needsProcessing
                    )
                )

                _state.update {
                    InvoiceFormState()
                }
                android.util.Log.d("InvoiceViewModel", "Invoice saved successfully: ${invoice.invoiceId}. NeedsProcessing: $needsProcessing")
                android.util.Log.d("InvoiceViewModel", "Saved Items List: ${invoice.items}")
            } else if (result is ValidationResult.Invalid) {
                val errorMsg = result.errors.joinToString("\n") { "${it.field}: ${it.message}" }
                android.util.Log.e("InvoiceViewModel", "SAVE FAILED: $errorMsg")
                _events.emit(
                    InvoiceUiEvent.ShowErrorDialog(
                        "Validation Errors:\n$errorMsg"
                    )
                )
            } else {
                 _events.emit(
                    InvoiceUiEvent.ShowErrorDialog(
                        "Unknown validation error"
                    )
                )
            }
        }
    }

    /* ---------- HELPERS ---------- */

    private fun recalc() {
        _state.update { InvoiceCalculator.recalc(it) }
    }

    private fun buildInvoice(): Invoice {
        val s = _state.value
        val inv = Invoice(
            invoiceId = s.invoiceId ?: UUID.randomUUID().toString(),
            invoiceNumber = s.invoiceNumber,
            invoiceSequence = s.invoiceSequence,
            invoiceDate = s.invoiceDate,
            financialYear = s.financialYear,
            firm = s.firm!!,
            isCashSale = s.isCashSale,
            billToParty = s.billToParty,
            shipToParty = s.shipToParty,
            items = s.items,
            taxSummary = s.taxSummary,
            totalTaxableValue = s.totalTaxableValue,
            totalTaxAmount = s.totalTaxAmount,
            totalInvoiceValue = s.totalInvoiceValue,
            transportDetails = s.transportDetails,
            generateEInvoice = s.generateEInvoice,
            generateEWayBill = s.generateEWayBill,
            additionalDetails = s.additionalDetails
        )
        android.util.Log.d("InvoiceViewModel", "buildInvoice: isCashSale=${inv.isCashSale}, BillTo=${inv.billToParty}")
        return inv
    }

    private fun checkComplianceCompatibility(dateString: String, isInitialLoad: Boolean = false) {
        try {
            val selectedDate = DateUtils.parse(dateString)
            val today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"))
            val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(selectedDate, today)

            val isFuture = daysDiff < 0
            val isPast = daysDiff > 0

            val eWayAllowed = !isFuture && !isPast
            val eInvoiceAllowed = !isFuture && daysDiff <= 30

            _state.update {
                it.copy(
                    isEWayBillAllowed = eWayAllowed,
                    isEInvoiceAllowed = eInvoiceAllowed,
                    // Auto-uncheck only if it's NOT the initial load of an existing invoice
                    generateEWayBill = if (eWayAllowed || isInitialLoad) it.generateEWayBill else false,
                    generateEInvoice = if (eInvoiceAllowed || isInitialLoad) it.generateEInvoice else false
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("InvoiceViewModel", "Failed to check compliance compatibility", e)
        }
    }

    private fun fetchLastInvoiceEpoch() {
        val firm = _state.value.firm ?: return
        val fy = _state.value.financialYear
        viewModelScope.launch {
            val lastEpoch = repo.getLastInvoiceDateEpoch(firm.gstin, fy)
            _state.update { it.copy(lastInvoiceDateEpoch = lastEpoch) }
            validate() // Re-validate after fetching
        }
    }
}