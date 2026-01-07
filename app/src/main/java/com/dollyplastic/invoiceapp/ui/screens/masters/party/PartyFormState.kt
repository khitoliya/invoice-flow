package com.dollyplastic.invoiceapp.ui.screens.masters.party


import com.dollyplastic.invoiceapp.data.models.Party
import java.util.UUID

data class PartyFormState(
    val partyId: String? = null,
    val tradeName: String = "",
    val nickName: String = "",
    val gstin: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val stateCode: String = "",
    val pincode: String = "",
    val errors: Map<String, String> = emptyMap()
) {

    fun toParty(): Party = Party(
        partyId = partyId ?: UUID.randomUUID().toString(),
        tradeName = tradeName,
        nickName = nickName,
        gstin = gstin,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2.ifBlank { null },
        city = city,
        state = state,
        stateCode = stateCode,
        pincode = pincode
    )

    fun update(field: String, value: String): PartyFormState =
        when (field) {
            "tradeName" -> copy(tradeName = value)
            "nickName" -> copy(nickName = value)
            "gstin" -> copy(gstin = value.uppercase())
            "addressLine1" -> copy(addressLine1 = value)
            "addressLine2" -> copy(addressLine2 = value)
            "city" -> copy(city = value)
            "state" -> copy(state = value)
            "pincode" -> copy(pincode = value)
            else -> this
        }

    companion object {
        fun fromParty(party: Party) = PartyFormState(
            partyId = party.partyId,
            tradeName = party.tradeName,
            nickName = party.nickName,
            gstin = party.gstin,
            addressLine1 = party.addressLine1,
            addressLine2 = party.addressLine2 ?: "",
            city = party.city,
            state = party.state,
            stateCode = party.stateCode,
            pincode = party.pincode
        )
    }
}
