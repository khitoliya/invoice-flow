package com.dollyplastic.invoiceapp.ui.screens.settings.recyclebin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val repository: InvoiceRepository
) : ViewModel() {

    private val _deletedInvoices = MutableStateFlow<List<Invoice>>(emptyList())
    val deletedInvoices = _deletedInvoices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadDeletedInvoices()
    }

    fun loadDeletedInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getArchivedInvoices()
            if (result is Result.Success) {
                _deletedInvoices.value = result.data
            }
            _isLoading.value = false
        }
    }
}
