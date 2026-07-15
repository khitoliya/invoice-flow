package com.dollyplastic.invoiceapp.domain.Validation




import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDraft


object EInvoiceValidator {

    fun validate(draft: EInvoiceDraft): ValidationResult {

        val errors = mutableListOf<ValidationError>()

        /* ================= TRANSACTION DETAILS ================= */

        if (draft.TranDtls.TaxSch != "GST") {
            errors += error("TranDtls.TaxSch", "Only GST tax scheme is supported")
        }

        if (draft.TranDtls.SupTyp !in listOf("B2B", "SEZWP", "SEZWOP", "EXPWP", "EXPWOP", "DEXP")) {
            errors += error("TranDtls.SupTyp", "Invalid supply type for E-Invoice")
        }

        /* ================= DOCUMENT DETAILS ================= */

        if (draft.DocDtls.No.isBlank()) {
            errors += error("DocDtls.No", "Invoice number is mandatory")
        }

        if (!DateRules.isNotFuture(draft.DocDtls.Dt)) {
            errors += error("DocDtls.Dt", "Invoice date cannot be in the future")
        }

        /* ================= SELLER ================= */

        if (!ValidationUtils.isValidGSTIN(draft.SellerDtls.Gstin)) {
            errors += error("SellerDtls.Gstin", "Invalid supplier GSTIN")
        }

        if (draft.SellerDtls.LglNm.isBlank()) {
            errors += error("SellerDtls.LglNm", "Supplier legal name is required")
        }

        if (!ValidationUtils.isValidPincode(draft.SellerDtls.Pin.toString())) {
            errors += error("SellerDtls.Pin", "Invalid supplier pincode")
        }

        /* ================= BUYER ================= */

        if (!ValidationUtils.isValidGSTIN(draft.BuyerDtls.Gstin)) {
            errors += error("BuyerDtls.Gstin", "Invalid buyer GSTIN")
        }

        if (draft.BuyerDtls.LglNm.isBlank()) {
            errors += error("BuyerDtls.LglNm", "Buyer legal name is required")
        }

        if (!ValidationUtils.isValidPincode(draft.BuyerDtls.Pin.toString())) {
            errors += error("BuyerDtls.Pin", "Invalid buyer pincode")
        }

        /* ================= ITEMS ================= */

        if (draft.ItemList.isEmpty()) {
            errors += error("ItemList", "At least one item is required")
        }

        draft.ItemList.forEachIndexed { index, item ->

            if (!ValidationUtils.isValidHSN(item.HsnCd)) {
                errors += error(
                    "ItemList[$index].HsnCd",
                    "Invalid HSN code"
                )
            }

            if (item.Qty <= 0) {
                errors += error(
                    "ItemList[$index].Qty",
                    "Quantity must be greater than zero"
                )
            }

            if (item.AssVal <= 0) {
                errors += error(
                    "ItemList[$index].AssVal",
                    "Taxable value must be greater than zero"
                )
            }

            if (item.TotItemVal <= 0) {
                errors += error(
                    "ItemList[$index].TotItemVal",
                    "Total item value must be greater than zero"
                )
            }
        }

        /* ================= TAX CONSISTENCY ================= */

        val intraState =
            draft.SellerDtls.Stcd == draft.BuyerDtls.Stcd

        if (intraState && draft.ValDtls.IgstVal > 0) {
            errors += error(
                "ValDtls.IgstVal",
                "IGST is not allowed for intra-state supply"
            )
        }

        if (!intraState &&
            (draft.ValDtls.CgstVal > 0 || draft.ValDtls.SgstVal > 0)
        ) {
            errors += error(
                "ValDtls.CgstVal/SgstVal",
                "CGST/SGST not allowed for inter-state supply"
            )
        }

        /* ================= TOTALS ================= */

        if (draft.ValDtls.TotInvVal <= 0) {
            errors += error(
                "ValDtls.TotInvVal",
                "Total invoice value must be greater than zero"
            )
        }

        /* ================= OPTIONAL EWB DETAILS ================= */

        draft.EwbDtls?.let { ewb ->

            if (ewb.Distance <= 0 || ewb.Distance > 4000) {
                errors += error(
                    "EwbDtls.Distance",
                    "E-Way Bill distance must be between 1 and 4000 km"
                )
            }

            if (ewb.TransMode !in listOf(null, "1", "2", "3", "4")) {
                errors += error(
                    "EwbDtls.TransMode",
                    "Invalid transport mode"
                )
            }
        }

        return if (errors.isEmpty())
            ValidationResult.Valid
        else
            ValidationResult.Invalid(errors)
    }

    private fun error(field: String, msg: String) =
        ValidationError(
            field = field,
            message = msg,
            section = "E-Invoice"
        )
}

