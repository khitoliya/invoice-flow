package com.dollyplastic.invoiceapp.domain.Validation

import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.domain.config.StateConfig

object FirmValidator {

    fun validate(
        firm: Firm,
        level: ValidationLevel
    ): List<ValidationError> {

        val errors = mutableListOf<ValidationError>()

        // BASE
        if (firm.tradeName.isBlank()) {
            errors.add(error("tradeName", "Trade name is required"))
        }
        if (firm.nickName.isBlank()) {
            errors.add(error("nickName", "Nick name is required write same as Trade name if none"))
        }
        if (firm.addressLine1.isBlank()) {
            errors.add(error("addressLine1", "Address is required"))
        }



        if (firm.gstin.isBlank()) {
            errors.add(error("gstin", "GSTIN is required"))
        } else if (!ValidationUtils.isValidGSTIN(firm.gstin)) {
            errors.add(error("gstin", "Invalid GSTIN format"))
        } else {
            val stateCode = firm.gstin.take(2)
            val state = StateConfig.getByCode(stateCode)

            if (state == null) {
                errors.add(error("gstin", "Invalid GSTIN state code"))
            }
        }




        if (!ValidationUtils.isValidPincode(firm.pincode)) {
            errors.add(error("pincode", "Invalid pincode"))
        }

        // GST
        if (level >= ValidationLevel.GST) {
            val gstState = firm.gstin.take(2)
            if (gstState != firm.stateCode) {
                errors.add(
                    error(
                        "gstin",
                        "GSTIN state code does not match firm state"
                    )
                )
            }
        }

        // ---------------- BANK DETAILS (MANDATORY) ----------------

        if (firm.bankName.isBlank()) {
            errors.add(error("bankName", "Bank name is required"))
        }

        if (firm.accountNumber.isBlank()) {
            errors.add(error("accountNumber", "Account number is required"))
        }

        if (firm.ifscCode.isBlank()) {
            errors.add(error("ifscCode", "IFSC code is required"))
        } else if (!ValidationUtils.isValidIFSC(firm.ifscCode)) {
            errors.add(error("ifscCode", "Invalid IFSC code"))
        }

        if (firm.branchName.isBlank()) {
            errors.add(error("branchName", "Branch name is required"))
        }



        return errors
    }

    private fun error(field: String, msg: String) =
        ValidationError(field, msg, section = "Firm")
}
