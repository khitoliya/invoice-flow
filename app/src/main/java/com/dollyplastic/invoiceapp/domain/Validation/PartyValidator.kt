package com.dollyplastic.invoiceapp.domain.Validation

import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.domain.config.StateConfig

object PartyValidator {

    fun validate(
        party:Party,
        level: ValidationLevel
    ): List<ValidationError> {

        val errors = mutableListOf<ValidationError>()

        // BASE
        // BASE
        if (party.tradeName.isBlank()) {
            errors.add(error("tradeName", "Trade name is required"))
        }


        if (party.addressLine1.isBlank()) {
            errors.add(error("addressLine1", "Address is required"))
        }
        if (party.gstin.isBlank()) {
            errors.add(error("gstin", "GSTIN is required"))
        } else if (!ValidationUtils.isValidGSTIN(party.gstin)) {
            errors.add(error("gstin", "Invalid GSTIN format"))
        } else {
            val stateCode = party.gstin.take(2)
            val state = StateConfig.getByCode(stateCode)

            if (state == null) {
                errors.add(error("gstin", "Invalid GSTIN state code"))
            }
        }





        if (!ValidationUtils.isValidPincode(party.pincode)) {
            errors.add(error("pincode", "Invalid pincode"))
        }


        // GST
        if (level >= ValidationLevel.GST) {
            val gstState = party.gstin.take(2)
            if (gstState != party.stateCode) {
                errors.add(
                    error(
                        "gstin",
                        "GSTIN state code does not match firm state"
                    )
                )
            }
        }

        return errors
    }

    private fun error(field: String, msg: String) =
        ValidationError(field, msg, section = "Party")
}
