package com.dollyplastic.invoiceapp.ui.screens.masters.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.data.repository.ItemRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Usecase.AddItemUseCase
import com.dollyplastic.invoiceapp.domain.Validation.ItemValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.combine
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import kotlin.random.Random



@HiltViewModel
class ItemViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val firmRepository: FirmRepository,
    private val addItemUseCase: AddItemUseCase
) : ViewModel() {

    /* ---------- LIST ---------- */

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    private val _firms = MutableStateFlow<List<Firm>>(emptyList())
    
    // Wire up: Placeholder for future backend stock data
    // Map<ItemId, Map<FirmId, StockQty>>
    private val _stockData = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())

    private val _expandedItems = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedFirm = MutableStateFlow<Firm?>(null)

    val selectedFirm = _selectedFirm.asStateFlow()
    val availableFirms = _firms.asStateFlow()

    // Combined UI State
    val itemUiModels = combine(
        _items,
        _firms,
        _stockData,
        _expandedItems,
        _selectedFirm
    ) { items, firms, stockData, expandedIds, selectedFirm ->
        items.map { item ->
            val firmStocks = firms.map { firm ->
                val qty = stockData[item.itemId]?.get(firm.firmId) ?: 0
                FirmStock(firm.firmId, firm.nickName, qty)
            }.filter { 
                // If a firm is selected, only show stock for that firm
                selectedFirm == null || it.firmId == selectedFirm.firmId 
            }

            val totalStock = firmStocks.sumOf { it.stockQty }
            
            ItemUiModel(
                item = item,
                totalStock = totalStock,
                firmStocks = firmStocks,
                isExpanded = expandedIds.contains(item.itemId)
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )


    /* ---------- FORM ---------- */

    private val _formState = MutableStateFlow(ItemFormState())
    val formState = _formState.asStateFlow()

    /* ---------- UI EVENTS ---------- */

    sealed class ItemUiEvent {
        data class ShowErrorDialog(val msg: String) : ItemUiEvent()
        data object ShowConfirmDialog : ItemUiEvent()
    }

    private val _uiEvent = MutableSharedFlow<ItemUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val isFormValid =
        formState.map { 
            it.errors.isEmpty() && 
            it.name.isNotBlank() && 
            it.hsnCode.isNotBlank() && 
            it.unit.isNotBlank()
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            // Load Items
            launch {
                when (val r = repository.getAllItems()) {
                    is Result.Success -> {
                        _items.value = r.data
                        generateMockStock(r.data) // Generate mock stock when items load
                    }
                    is Result.Error -> _items.value = emptyList()
                }
            }
            // Load Firms
            launch {
                when (val r = firmRepository.getAllFirms()) {
                    is Result.Success -> _firms.value = r.data
                    is Result.Error -> _firms.value = emptyList()
                }
            }
        }
    }
    
    // Helper to generate random mock stock
    private fun generateMockStock(items: List<Item>) {
         val currentFirms = _firms.value
         if(currentFirms.isEmpty()) return

         val newStockData = items.associate { item ->
             item.itemId to currentFirms.associate { firm ->
                 firm.firmId to Random.nextInt(0, 150)
             }
         }
         _stockData.value = newStockData
    }

    // Call this if firms load AFTER items
    fun refreshMockStock() {
        generateMockStock(_items.value)
    }

    fun toggleItemExpansion(itemId: String) {
        val current = _expandedItems.value
        if (current.contains(itemId)) {
            _expandedItems.value = current - itemId
        } else {
            _expandedItems.value = current + itemId
        }
    }

    fun selectFirm(firm: Firm?) {
        _selectedFirm.value = firm
        // Collapse all items when switching views to avoid confusion
        _expandedItems.value = emptySet()
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            loadData() // Reload everything
        }
    }

    fun loadItemForEdit(itemId: String) {
        viewModelScope.launch {
            when (val r = repository.getItem(itemId)) {
                is Result.Success ->
                    _formState.value = ItemFormState.fromItem(r.data)
                else -> Unit
            }
        }
    }

    fun onFieldChange(field: String, value: String) {
        val updated = _formState.value.update(field, value)

        val errors = ItemValidator
            .validate(updated.toItem(), ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }

        _formState.value = updated.copy(errors = errors)
    }

    fun onGstRateChange(rate: Double) {
        val updated = _formState.value.updateGstRate(rate)

        val errors = ItemValidator
            .validate(updated.toItem(), ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }

        _formState.value = updated.copy(errors = errors)
    }

    fun requestSaveConfirmation() {
        viewModelScope.launch {
            _uiEvent.emit(ItemUiEvent.ShowConfirmDialog)
        }
    }


    fun saveItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = addItemUseCase.execute(_formState.value.toItem())

            when (result) {
                is ValidationResult.Valid -> {
                    _formState.value = ItemFormState()
                    _formState.value = ItemFormState()
                    loadData()
                    onSuccess()
                }
                is ValidationResult.Invalid -> {
                    val dup = result.errors.firstOrNull { it.field == "name" }
                    if (dup != null) {
                        _uiEvent.emit(ItemUiEvent.ShowErrorDialog(dup.message))
                    } else {
                        _formState.value = _formState.value.copy(
                            errors = result.errors.associate {
                                it.field to it.message
                            }
                        )
                    }
                }
            }
        }
    }
}
