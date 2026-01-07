package com.dollyplastic.invoiceapp.domain.Utils

import java.time.LocalDate

object FinancialYearUtils {

    fun fromDate(date: LocalDate): String {
        val year = date.year
        return if (date.monthValue >= 4) {
            "${year}-${(year + 1) % 100}"
        } else {
            "${year - 1}-${year % 100}"
        }
    }
}
