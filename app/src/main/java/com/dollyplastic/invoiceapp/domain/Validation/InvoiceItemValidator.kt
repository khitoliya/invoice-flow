package com.dollyplastic.invoiceapp.domain.Validation

import com.dollyplastic.invoiceapp.data.models.InvoiceItem
import com.dollyplastic.invoiceapp.data.models.Item

object InvoiceItemValidator {

    fun validateInput(
        item: Item?,
        qtyStr: String,
        tareStr: String,
        rateStr: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (item == null) {
            errors["item"] = "Please select an item"
        }

        val qtyValue = qtyStr.toDoubleOrNull()
        if (qtyValue == null || qtyValue <= 0) {
            errors["qty"] = "Invalid Qty"
        }

        if (tareStr.isNotBlank()) {
            val tareValue = tareStr.toDoubleOrNull()
            if (tareValue == null || tareValue < 0) {
                errors["tare"] = "Invalid Tare"
            } else if (qtyValue != null && tareValue >= (qtyValue + tareValue)) {
                errors["tare"] = "Tare exceeds gross weight"
            }
        }

        val rateValue = rateStr.toDoubleOrNull()
        if (rateValue == null || rateValue <= 0) {
            errors["rate"] = "Invalid Rate"
        }

        return errors
    }

    fun validate(invoiceItem: InvoiceItem, level: ValidationLevel): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        errors += ItemValidator.validate(invoiceItem.item, level)

        if (invoiceItem.quantity <= 0) {
            errors.add(ValidationError("quantity", "Invalid Qty", "InvoiceItem"))
        }

        if (invoiceItem.rate <= 0) {
            errors.add(ValidationError("rate", "Invalid Rate", "InvoiceItem"))
        }

        val tare = invoiceItem.tareWeight
        if (tare != null) {
            if (tare < 0) {
                errors.add(ValidationError("tareWeight", "Invalid Tare", "InvoiceItem"))
            } else if (tare >= (invoiceItem.quantity + tare)) {
                errors.add(ValidationError("tareWeight", "Tare exceeds gross weight", "InvoiceItem"))
            }
        }

        return errors
    }
}
