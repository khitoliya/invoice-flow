package com.dollyplastic.invoiceapp.domain.Utils

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    
    fun Double.toIndianCurrency(): String {
        return try {
             val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
             format.maximumFractionDigits = 2
             format.minimumFractionDigits = 2
             format.format(this)
        } catch (e: Exception) {
            "₹$this"
        }
    }

    fun String.toDisplayDate(): String {
        if (this.isBlank()) return ""
        return try {
            // Try ISO Compact first (yyyyMMdd)
            // If it contains "-", it might be yyyy-MM-dd
            val pattern = if (this.contains("-")) "yyyy-MM-dd" else "yyyyMMdd"
            val inputFormat = java.text.SimpleDateFormat(pattern, Locale.US)
            val date = inputFormat.parse(this) ?: return this
            
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", Locale.US)
            outputFormat.format(date)
        } catch (e: Exception) {
             this
        }
    }
    fun formatCurrency(amount: Double): String {
        return amount.toIndianCurrency()
    }

    fun formatQuantity(qty: Double): String {
        return if (qty % 1.0 == 0.0) {
            qty.toInt().toString()
        } else {
            String.format("%.2f", qty)
        }
    }
}
