package com.dollyplastic.invoiceapp.ui.test

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.dollyplastic.invoiceapp.data.utils.Result

@HiltViewModel
class FirebaseTestViewModel @Inject constructor(
    private val firmRepository: FirmRepository
) : ViewModel() {

    fun testAddFirm() {
        viewModelScope.launch {
            val firm = Firm(
                firmId = UUID.randomUUID().toString(),
                tradeName = "Dolly Plastic",
                nickName = "DP Delhi",
                gstin = "07ARDPK8616J1ZK",
                addressLine1 = "H.No.1221, Mohalla Barsan",
                addressLine2 = "Tikri Kalan",
                city = "Delhi",
                state = "Delhi",
                stateCode = "07",
                pincode = "110041"
            )

            when (val result = firmRepository.addFirm(firm)) {
                is Result.Success -> {
                    Log.d("FIREBASE_TEST", "Firm added successfully")
                }
                is Result.Error -> {
                    Log.e("FIREBASE_TEST", "Error", result.exception)
                }
            }
        }
    }

    fun testReadFirms() {
        viewModelScope.launch {
            when (val result = firmRepository.getAllFirms()) {
                is Result.Success -> {
                    Log.d("FIREBASE_TEST", "Firms: ${result.data}")
                }
                is Result.Error -> {
                    Log.e("FIREBASE_TEST", "Error", result.exception)
                }
            }
        }
    }
}
