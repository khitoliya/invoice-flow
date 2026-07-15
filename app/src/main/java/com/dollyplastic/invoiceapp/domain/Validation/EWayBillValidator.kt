package com.dollyplastic.invoiceapp.domain.Validation

import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDraft

object EWayBillValidator {

    fun validate(draft: EWayBillDraft): ValidationResult {

        val errors = mutableListOf<ValidationError>()

        validateTransactionType(draft, errors)
        validateGstin(draft, errors)
        validateStatesAndTax(draft, errors)
        validateDocumentDates(draft, errors)
        validateTransport(draft, errors)
        validateDistance(draft, errors)
        validateItems(draft, errors)


        return if (errors.isEmpty())
            ValidationResult.Valid
        else
            ValidationResult.Invalid(errors)
    }
    private fun validateGstin(
        d: EWayBillDraft,
        errors: MutableList<ValidationError>
    ) {

        // FROM GSTIN must always be valid
        if (!ValidationUtils.isValidGSTIN(d.fromGstin)) {
            errors += error("fromGstin", "Invalid supplier GSTIN")
        }

        // TO GSTIN can be URP or valid GSTIN
        if (d.toGstin != "URP" && !ValidationUtils.isValidGSTIN(d.toGstin)) {
            errors += error("toGstin", "Invalid recipient GSTIN")
        }
    }
    private fun validateStatesAndTax(
        d: EWayBillDraft,
        errors: MutableList<ValidationError>
    ) {

        val isIntraState = d.fromStateCode == d.toStateCode

        if (isIntraState) {
            if (d.igstValue > 0) {
                errors += error("igstValue", "IGST should be zero for intra-state supply")
            }
        } else {
            if (d.cgstValue > 0 || d.sgstValue > 0) {
                errors += error("cgst/sgst", "CGST/SGST should be zero for inter-state supply")
            }
        }
    }
    private fun validateDocumentDates(
        d: EWayBillDraft,
        errors: MutableList<ValidationError>
    ) {

        // docDate <= today
        if (!DateRules.isNotFuture(d.docDate)) {
            errors += error("docDate", "Document date cannot be in future")
        }

        // transDocDate >= docDate (if present)
        if (!DateRules.isAfterOrEqual(d.transportDocDate, d.docDate)) {
            errors += error(
                "transportDocDate",
                "Transport document date cannot be before invoice date"
            )
        }
    }

    private fun validateTransport(
        d: EWayBillDraft,
        errors: MutableList<ValidationError>
    ) {

        when (d.transportMode) {

            // ROAD
            1 -> {
                if (d.transporterId.isNullOrBlank() &&
                    d.vehicleNumber.isNullOrBlank()
                ) {
                    errors += error(
                        "vehicleNumber",
                        "Vehicle number is mandatory for road transport if transporter ID is not provided"
                    )
                }

                if (!d.vehicleNumber.isNullOrBlank() &&
                    !ValidationUtils.isValidVehicleNumber(d.vehicleNumber)
                ) {
                    errors += error("vehicleNumber", "Invalid vehicle number format")
                }
            }

            // RAIL / AIR / SHIP
            2, 3, 4 -> {
                if (d.transportDocNo.isNullOrBlank()) {
                    errors += error(
                        "transportDocNo",
                        "Transport document number is mandatory for non-road transport"
                    )
                }
                if (d.transportDocDate.isNullOrBlank()) {
                    errors += error(
                        "transportDocDate",
                        "Transport document date is mandatory for non-road transport"
                    )
                }
            }
        }
    }

    private fun validateDistance(
        d: EWayBillDraft,
        errors: MutableList<ValidationError>
    ) {
        if (d.transDistance <= 0 || d.transDistance > 4000) {
            errors += error(
                "transDistance",
                "Distance must be between 1 and 4000 KM"
            )
        }
    }

    private fun validateItems(
        d: EWayBillDraft,
        errors: MutableList<ValidationError>
    ) {

        if (d.itemList.isEmpty()) {
            errors += error("itemList", "At least one item is required")
            return
        }

        d.itemList.forEachIndexed { index, item ->

            if (!ValidationUtils.isValidHSN(item.hsnCode)) {
                errors += error(
                    "itemList[$index].hsnCode",
                    "Invalid HSN code"
                )
            }

            if (item.quantity <= 0) {
                errors += error(
                    "itemList[$index].quantity",
                    "Quantity must be greater than zero"
                )
            }
        }
    }

    private fun validateTransactionType(
        d: EWayBillDraft,
        errors: MutableList<ValidationError>
    ) {
        if (d.transType != 1 && d.transType != 2) {
            errors += error(
                "transType",
                "Unsupported transaction type. Only Regular or Bill To–Ship To is allowed"
            )
        }
    }


    private fun error(field: String, msg: String) =
        ValidationError(
            field = field,
            message = msg,
            section = "E-Way Bill"
        )







}

object DateRules {

    fun isNotFuture(date: String): Boolean =
        try {
            !java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .isAfter(java.time.LocalDate.now())
        } catch (e: Exception) {
            false
        }

    fun isAfterOrEqual(d1: String?, d2: String): Boolean {
        if (d1 == null) return true
        return try {
            val date1 = java.time.LocalDate.parse(d1, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val date2 = java.time.LocalDate.parse(d2, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            !date1.isBefore(date2)
        } catch (e: Exception) {
            false
        }
    }
}

