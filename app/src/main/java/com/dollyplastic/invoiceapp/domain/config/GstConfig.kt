package com.dollyplastic.invoiceapp.domain.config


object GstConfig {

    /**
     * Allowed GST rates in India.
     * Keep this as single source of truth.
     */
    val ALLOWED_GST_RATES: List<Double> = listOf(
        0.0,   // Exempt / NIL
        5.0,
        18.0,
    )

    fun isValidRate(rate: Double): Boolean {
        return ALLOWED_GST_RATES.contains(rate)
    }
}
