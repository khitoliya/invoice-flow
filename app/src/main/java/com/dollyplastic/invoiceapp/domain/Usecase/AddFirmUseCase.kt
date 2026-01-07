package com.dollyplastic.invoiceapp.domain.Usecase

import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import com.dollyplastic.invoiceapp.domain.Validation.FirmValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationError
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import javax.inject.Inject

class AddFirmUseCase @Inject constructor(
    private val repository: FirmRepository
) {

    suspend fun execute(firm: Firm): ValidationResult {

        // 1️⃣ Base + GST validation
        val validationErrors =
            FirmValidator.validate(firm, ValidationLevel.GST)

        if (validationErrors.isNotEmpty()) {
            return ValidationResult.Invalid(validationErrors)
        }

        // 2️⃣ Duplicacy check
        if (
            repository.firmExistsByGstin(
                gstin = firm.gstin,
                excludeFirmId = firm.firmId
            )
        ) {
            return ValidationResult.Invalid(
                listOf(
                    ValidationError(
                        field = "gstin",
                        message = "Another firm with this GSTIN already exists",
                        section = "Firm"
                    )
                )
            )
        }


        // 3️⃣ Save
        repository.addFirm(firm)

        return ValidationResult.Valid
    }
}
