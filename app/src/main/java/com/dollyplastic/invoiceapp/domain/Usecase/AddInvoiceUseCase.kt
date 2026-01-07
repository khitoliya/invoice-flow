package com.dollyplastic.invoiceapp.domain.Usecase

import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import com.dollyplastic.invoiceapp.domain.Utils.FinancialYearUtils
import com.dollyplastic.invoiceapp.domain.Validation.InvoiceValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationError
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import java.time.LocalDate
import javax.inject.Inject

class AddInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {

    suspend fun execute(invoice: Invoice): ValidationResult {

        // 1️⃣ Base + GST validation
        val validation =
            InvoiceValidator.validate(invoice, ValidationLevel.GST)

        if (validation is ValidationResult.Invalid) {
            return validation
        }

        // 2️⃣ Financial year derivation
        val fy = FinancialYearUtils.fromDate(
            DateUtils.parse(invoice.invoiceDate)

        )

        // 3️⃣ Duplicacy check
        val exists = repository.invoiceExists(
            firmGstin = invoice.firm.gstin,
            invoiceNumber = invoice.invoiceNumber,
            financialYear = fy
        )

        if (exists) {
            return ValidationResult.Invalid(
                listOf(
                    ValidationError(
                        field = "invoiceNumber",
                        message =
                            "Invoice number already used for this firm in FY $fy",
                        section = "Invoice"
                    )
                )
            )
        }

        val lastSequenceUsed =
            repository.getLastInvoiceSequence(
                firmGstin = invoice.firm.gstin,
                financialYear = fy
            )

        // 4️⃣ Enforce sequence rule
        if (invoice.invoiceSequence <= lastSequenceUsed) {
            return ValidationResult.Invalid(
                listOf(
                    ValidationError(
                        field = "invoiceNumber",
                        message =
                            "Invoice number already used for this firm in FY $fy",
                        section = "Invoice"
                    )
                )
            )
        }


        // 4️⃣ Save with derived FY
        repository.createInvoice(
            invoice.copy(financialYear = fy)
        )

        return ValidationResult.Valid
    }
}

