package com.dollyplastic.invoiceapp.domain.Validation

sealed class ValidationResult {

    object Valid : ValidationResult()

    data class Invalid(
        val errors: List<ValidationError>
    ) : ValidationResult()

    fun isValid(): Boolean = this is Valid
}
