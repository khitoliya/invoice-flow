package com.dollyplastic.invoiceapp.domain.Validation

import android.util.Log
import com.dollyplastic.invoiceapp.data.models.Invoice

object InvoiceValidator {

    fun validate(
        invoice: Invoice,
        level: ValidationLevel
    ): ValidationResult {

        val errors = mutableListOf<ValidationError>()

        // Firm
        errors += FirmValidator.validate(invoice.firm, level)

        if (invoice.firm.tradeName.isBlank()) {
            errors.add(
                ValidationError(
                    field = "firm",
                    message = "Firm is required",
                    section = "Invoice"
                )
            )
        }

        // Invoice Number
        if (invoice.invoiceNumber.isBlank()) {
            errors.add(
                ValidationError(
                    field = "invoiceNumber",
                    message = "Invoice number is required",
                    section = "Invoice"
                )
            )
        } else {
            val seq = invoice.invoiceSequence

            if (seq <= 0) {
                errors.add(
                    ValidationError(
                        field = "invoiceNumber",
                        message = "Invoice number must be greater than zero",
                        section = "Invoice"
                    )
                )
            }
        }



        // Party
        if (!invoice.isCashSale && invoice.billToParty == null) {
            errors.add(
                ValidationError(
                    field = "billToParty",
                    message = "Buyer (Bill To) is required",
                    section = "Party"
                )
            )
        }


        // Party vs Firm GST validation
        if (!invoice.isCashSale) {

            val billTo = invoice.billToParty
            val shipTo = invoice.shipToParty

            if (billTo != null && invoice.firm.gstin == billTo.gstin) {
                errors.add(
                    ValidationError(
                        field = "billToParty",
                        message = "Buyer GSTIN cannot be same as seller GSTIN",
                        section = "Party"
                    )
                )
            }

            if (shipTo != null && invoice.firm.gstin == shipTo.gstin) {
                errors.add(
                    ValidationError(
                        field = "shipToParty",
                        message = "Consignee GSTIN cannot be same as seller GSTIN",
                        section = "Party"
                    )
                )
            }
        }



        // Items
        if (invoice.items.isEmpty()) {
            errors.add(
                ValidationError(
                    "items",
                    "At least one item required",
                    section = "Items"
                )
            )
        }

        invoice.items.forEach {
            errors += InvoiceItemValidator.validate(it, level)
        }

        // Transport
        errors += TransportValidator.validate(
            invoice.transportDetails,
            level,

        )

        if ( (invoice.generateEInvoice || invoice.generateEWayBill) &&
            invoice.transportDetails.deliveryType == com.dollyplastic.invoiceapp.data.models.DeliveryType.BUYER_PICKUP
        ) {
            errors.add(
                ValidationError(
                    field = "deliveryType",
                    message = "Buyer Pickup is not allowed for E-Invoice/E-Way Bill",
                    section = "Transport"
                )
            )
        }
        Log.d("InvoiceValidator", "Validation errors: $errors")

        return if (errors.isEmpty())
            ValidationResult.Valid
        else
            ValidationResult.Invalid(errors)
    }
}
