package com.dollyplastic.invoiceapp.ui.common.Utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged

fun Modifier.onFocusLost(action: () -> Unit): Modifier = composed {
    var wasFocused by remember { mutableStateOf(false) }
    
    onFocusChanged { focusState ->
        if (wasFocused && !focusState.isFocused) {
            action()
        }
        wasFocused = focusState.isFocused
    }
}

fun formatIndianCurrency(amount: Double): String {
    return java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN")).format(amount)
}

fun getResponsiveFontSize(textLength: Int, baseSize: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit {
    return when {
        textLength > 20 -> baseSize * 0.6f
        textLength > 15 -> baseSize * 0.75f
        textLength > 12 -> baseSize * 0.9f
        else -> baseSize
    }
}
