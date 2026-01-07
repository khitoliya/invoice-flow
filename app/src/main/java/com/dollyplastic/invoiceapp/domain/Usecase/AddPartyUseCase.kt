package com.dollyplastic.invoiceapp.domain.Usecase

import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.data.repository.PartyRepository
import com.dollyplastic.invoiceapp.domain.Validation.PartyValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationError
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import javax.inject.Inject

class AddPartyUseCase @Inject constructor(
    private val repository: PartyRepository
) {

    suspend fun execute(party: Party): ValidationResult {

        val validationErrors =
            PartyValidator.validate(party, ValidationLevel.GST)

        if (validationErrors.isNotEmpty()) {
            return ValidationResult.Invalid(validationErrors)
        }

        if (
            repository.partyExists(
                gstin = party.gstin,
                excludePartyId = party.partyId
            )
        ) {
            return ValidationResult.Invalid(
                listOf(
                    ValidationError(
                        field = "gstin",
                        message = "Another party with this GSTIN already exists",
                        section = "Party"
                    )
                )
            )
        }


        repository.addParty(party)
        return ValidationResult.Valid
    }
}
