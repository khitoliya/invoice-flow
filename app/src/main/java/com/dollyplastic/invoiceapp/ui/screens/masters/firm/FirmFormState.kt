package com.dollyplastic.invoiceapp.ui.screens.masters.firm

import com.dollyplastic.invoiceapp.data.models.Firm
import java.util.UUID


data class FirmFormState(
    val firmId: String? = null,
    val tradeName: String = "",
    val nickName: String = "",
    val gstin: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val stateCode: String = "",
    val pincode: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val branchName: String = "",

    val errors: Map<String, String> = emptyMap()
) {

    fun toFirm(): Firm = Firm(
        firmId = firmId ?: UUID.randomUUID().toString(),
        tradeName = tradeName,
        nickName = nickName,
        gstin = gstin,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2.ifBlank { null },
        city = city,
        state = state,
        stateCode = stateCode,
        pincode = pincode,
        bankName = bankName,
        accountNumber = accountNumber,
        ifscCode = ifscCode.uppercase(),
        branchName = branchName
    )

    fun update(field: String, value: String): FirmFormState =
        when (field) {
            "tradeName" -> copy(tradeName = value)
            "nickName" -> copy(nickName = value)
            "gstin" -> copy(gstin = value.uppercase())
            "addressLine1" -> copy(addressLine1 = value)
            "addressLine2" -> copy(addressLine2 = value)
            "city" -> copy(city = value)
            "state" -> copy(state = value)
            "stateCode" -> copy(stateCode = value)
            "pincode" -> copy(pincode = value)
            "bankName" -> copy(bankName = value)
            "accountNumber" -> copy(accountNumber = value)
            "ifscCode" -> copy(ifscCode = value.uppercase())
            "branchName" -> copy(branchName = value)
            else -> this
        }

    companion object {
        fun fromFirm(firm: Firm): FirmFormState =
            FirmFormState(
                firmId = firm.firmId,
                tradeName = firm.tradeName,
                nickName = firm.nickName,
                gstin = firm.gstin,
                addressLine1 = firm.addressLine1,
                addressLine2 = firm.addressLine2 ?: "",
                city = firm.city,
                state = firm.state,
                stateCode = firm.stateCode,
                pincode = firm.pincode,
                bankName = firm.bankName,
                accountNumber = firm.accountNumber,
                ifscCode = firm.ifscCode,
                branchName = firm.branchName
            )
    }
}

