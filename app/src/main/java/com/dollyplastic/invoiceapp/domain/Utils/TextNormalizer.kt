package com.dollyplastic.invoiceapp.domain.Utils



object TextNormalizer {

    fun normalize(value: String): String =
        value
            .trim()
            .lowercase()
            .replace("\\s+".toRegex(), "")
}
