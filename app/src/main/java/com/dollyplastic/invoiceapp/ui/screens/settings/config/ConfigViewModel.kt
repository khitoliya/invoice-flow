package com.dollyplastic.invoiceapp.ui.screens.settings.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.repository.ConfigRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.config.IndianState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val repository: ConfigRepository
) : ViewModel() {

    private val _gstRates = MutableStateFlow<List<Double>>(emptyList())
    val gstRates = _gstRates.asStateFlow()

    private val _states = MutableStateFlow<List<IndianState>>(emptyList())
    val states = _states.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            launch {
                val r = repository.getGstRates()
                if (r is Result.Success) _gstRates.value = r.data
            }
            
            launch {
                val r = repository.getStates()
                if (r is Result.Success) _states.value = r.data
            }
            
            _isLoading.value = false
        }
    }

    fun addGstRate(rate: Double) {
        val current = _gstRates.value.toMutableList()
        if (!current.contains(rate)) {
            current.add(rate)
            current.sort()
            saveGstRates(current)
        }
    }

    fun deleteGstRate(rate: Double) {
        val current = _gstRates.value.toMutableList()
        current.remove(rate)
        saveGstRates(current)
    }

    private fun saveGstRates(rates: List<Double>) {
        viewModelScope.launch {
             _gstRates.value = rates
             repository.updateGstRates(rates)
        }
    }

    fun addState(state: IndianState) {
        val current = _states.value.toMutableList()
        if (current.none { it.code == state.code }) {
            current.add(state)
            current.sortBy { it.name }
            saveStates(current)
        }
    }

    fun deleteState(state: IndianState) {
        val current = _states.value.toMutableList()
        current.remove(state)
        saveStates(current)
    }

    private fun saveStates(list: List<IndianState>) {
        viewModelScope.launch {
            _states.value = list
            repository.updateStates(list)
        }
    }
}
