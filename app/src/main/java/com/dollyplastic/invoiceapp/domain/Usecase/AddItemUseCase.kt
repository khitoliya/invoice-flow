package com.dollyplastic.invoiceapp.domain.Usecase

import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.data.repository.ItemRepository
import com.dollyplastic.invoiceapp.domain.Validation.ItemValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationError
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {

        suspend fun execute(item: Item): ValidationResult {

            val validationErrors =
                ItemValidator.validate(item, ValidationLevel.BASE)

            if (validationErrors.isNotEmpty()) {
                return ValidationResult.Invalid(validationErrors)
            }

            if (
                repository.itemExists(
                    name = item.name,
                    hsnCode = item.hsnCode,
                    excludeItemId = item.itemId
                )
            ) {
                return ValidationResult.Invalid(
                    listOf(
                        ValidationError(
                            field = "name",
                            message = "Item with same name & HSN already exists",
                            section = "Item"
                        )
                    )
                )
            }


            repository.addItem(item)
            return ValidationResult.Valid
        }
}
