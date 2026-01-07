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

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val repo: InvoiceRepository,
    private val firmRepo: FirmRepository,
    private val partyRepo: PartyRepository,
    private val itemRepo: ItemRepository,
    private val addInvoiceUseCase: AddInvoiceUseCase
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

    fun setFirm(firm: Firm) {
        _state.update { it.copy(firm = firm) }
        regenerateInvoiceNumber()
        recalc()
        validate()
    }

    fun setInvoiceDate(date: String) {
        val fy = FinancialYearUtils.fromDate(DateUtils.parse(date))

        _state.update {
            it.copy(
                invoiceDate = date,
                financialYear = fy
            )
        }

        regenerateInvoiceNumber()
        validate()
    }


    private fun regenerateInvoiceNumber() {
        viewModelScope.launch {

            val firm = _state.value.firm ?: return@launch
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
                    invoiceNumber = number
                )
            }
        }
    }

    /* ---------- PARTY ---------- */

    fun setCashSale(isCash: Boolean) {
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

    fun setBillToParty(party: Party) {
        _state.update {
            it.copy(
                billToParty = party,
                shipToParty =
                    if (it.shipToSameAsBillTo) party else it.shipToParty
            )
        }
        recalc()
        validate()
    }

    fun setShipToSameAsBillTo(flag: Boolean) {
        _state.update {
            it.copy(
                shipToSameAsBillTo = flag,
                shipToParty = if (flag) it.billToParty else null
            )
        }
        validate()
    }

    fun setShipToParty(party: Party) {
        _state.update { it.copy(shipToParty = party) }
        validate()
    }

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
        _state.update { it.copy(transportDetails = details) }
        validate()
    }

    /* ---------- COMPLIANCE ---------- */

    fun setEInvoice(flag: Boolean) {
        _state.update { it.copy(generateEInvoice = flag) }
        validate()
    }

    fun setEWay(flag: Boolean) {
        _state.update { it.copy(generateEWayBill = flag) }
        validate()
    }

    fun updateAdditionalDetails(details: AdditionalDetails) {
        _state.update {
            it.copy(additionalDetails = details)
        }
    }



    /* ---------- VALIDATION ---------- */

    private fun validate() {
        val firm = _state.value.firm ?: return

        val invoice = buildInvoice()
        val result =
            InvoiceValidator.validate(
                invoice,
                ValidationLevel.BASE
            )

        _state.update {
            if (result is ValidationResult.Invalid)
                it.copy(
                    errors =
                        result.errors.associate {
                            e -> e.field to e.message
                        }
                )
            else it.copy(errors = emptyMap())
        }
    }

    /* ---------- SAVE ---------- */

    fun requestSave() {
        if (_state.value.isFormValid) {
            viewModelScope.launch {
                _events.emit(InvoiceUiEvent.ShowConfirmDialog)
            }
        }
    }

    fun confirmSave(context: Context) {
        viewModelScope.launch {
            val invoice= buildInvoice()
            val result =
                addInvoiceUseCase.execute(invoice)

            if (result is ValidationResult.Valid) {

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
            } else {
                _events.emit(
                    InvoiceUiEvent.ShowErrorDialog(
                        "Validation failed"
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
        return Invoice(
            invoiceId = UUID.randomUUID().toString(),
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
    }
}