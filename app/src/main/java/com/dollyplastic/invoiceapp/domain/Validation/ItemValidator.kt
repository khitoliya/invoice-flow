package com.dollyplastic.invoiceapp.domain.Validation

import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.domain.config.GstConfig

object ItemValidator {

    fun validate(
        item: Item,
        level: ValidationLevel
    ): List<ValidationError> {

        val errors = mutableListOf<ValidationError>()

        // BASE
        if (item.name.isBlank()) {
            errors.add(error("name", "Item name required"))
        }

        if (!ValidationUtils.isValidHSN(item.hsnCode)) {
            errors.add(error("hsnOrSac", "Invalid HSN/SAC"))
        }

        if (!GstConfig.isValidRate(item.gstRate)) {
            errors.add(error("gstRate", "Invalid GST rate"))
        }

        // E-INVOICE
        if (level >= ValidationLevel.E_INVOICE) {
            if (item.hsnCode.isBlank()) {
                errors.add(error("hsnOrSac", "HSN mandatory for e-Invoice"))
            }
        }

        return errors
    }

    private fun error(field: String, msg: String) =
        ValidationError(field, msg, section = "Item")
}
