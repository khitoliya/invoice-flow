package com.dollyplastic.invoiceapp.ui.screens.import_workflow

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.Purchase
import com.dollyplastic.invoiceapp.data.repository.PurchaseRepository
import com.dollyplastic.invoiceapp.domain.Parsing.InvoiceParser
import com.dollyplastic.invoiceapp.domain.Utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import java.util.UUID
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import com.dollyplastic.invoiceapp.data.repository.PartyRepository
import com.dollyplastic.invoiceapp.data.utils.Result

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Parsing : ImportUiState()
    data class ParsedPurchase(val purchase: Purchase) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
    object Success : ImportUiState()
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val app: Application,
    private val purchaseRepo: PurchaseRepository,
    private val firmRepo: FirmRepository,
    private val partyRepo: PartyRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun parse(uriStr: String) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Parsing
            try {
                // 1. Get Context (Firm GSTIN)
                // We assume single firm for now or pick the first one to filter "Self" from "Party"
                val firmsResult = firmRepo.getAllFirms()
                val firms = if (firmsResult is Result.Success) firmsResult.data else emptyList()
                val contextFirmGstin = if (firms.isNotEmpty()) firms.first().gstin else null // Fallback for Purchase

                val uri = Uri.parse(uriStr)
                val file = FileUtils.getFileFromUri(app, uri)
                if (file == null) {
                    _uiState.value = ImportUiState.Error("Failed to access file")
                    return@launch
                }

                val partiesResult = partyRepo.getAllParties()
                val parties = if (partiesResult is Result.Success) partiesResult.data else emptyList()

                var purchase = InvoiceParser.parsePurchase(app, file, contextFirmGstin)
                
                // Lookup Vendor
                purchase.vendor.gstin.takeIf { it.isNotEmpty() }?.let { extractedGstin ->
                    val match = parties.find { it.gstin.equals(extractedGstin, ignoreCase = true) }
                    if (match != null) {
                        Log.d("ImportWorkflow", "Vendor Lookup Found: ${match.tradeName}")
                        purchase = purchase.copy(vendor = match)
                    } else {
                        Log.d("ImportWorkflow", "Vendor Lookup Failed for: $extractedGstin")
                    }
                }
                
                _uiState.value = ImportUiState.ParsedPurchase(purchase)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ImportWorkflow", "Parsing Exception", e)
                _uiState.value = ImportUiState.Error("Parsing failed: ${e.message}")
            }
        }
    }

    fun savePurchase(purchase: Purchase) {
        viewModelScope.launch {
            try {
                purchaseRepo.savePurchase(purchase)
                _uiState.value = ImportUiState.Success
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error("Failed to save: ${e.message}")
            }
        }
    }
}
