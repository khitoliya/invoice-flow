package com.dollyplastic.invoiceapp.domain.Utils


import java.math.BigDecimal
import java.util.Locale

object NumberToWords {

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convert(amount: Double): String {
        val bd = BigDecimal(amount.toString())
        var wholePart = bd.toBigInteger().toLong()
        val fractionPart = bd.remainder(BigDecimal.ONE).multiply(BigDecimal(100)).toInt()

        if (wholePart == 0L && fractionPart == 0) return "Zero Only"

        val words = StringBuilder("INR ")

        if (wholePart > 0) {
            words.append(convertNumber(wholePart))
        }

        if (fractionPart > 0) {
            if (wholePart > 0) words.append(" and ")
            words.append(convertNumber(fractionPart.toLong())).append(" Paise")
        }

        words.append(" Only")
        return words.toString()
    }

    private fun convertNumber(n: Long): String {
        if (n < 0) return "Minus " + convertNumber(-n)
        if (n < 20) return units[n.toInt()]
        if (n < 100) return tens[n.toInt() / 10] + (if (n % 10 != 0L) " " + units[(n % 10).toInt()] else "")
        if (n < 1000) return units[(n / 100).toInt()] + " Hundred" + (if (n % 100 != 0L) " " + convertNumber(n % 100) else "")
        if (n < 100000) return convertNumber(n / 1000) + " Thousand" + (if (n % 1000 != 0L) " " + convertNumber(n % 1000) else "")
        if (n < 10000000) return convertNumber(n / 100000) + " Lakh" + (if (n % 100000 != 0L) " " + convertNumber(n % 100000) else "")

        return convertNumber(n / 10000000) + " Crore" + (if (n % 10000000 != 0L) " " + convertNumber(n % 10000000) else "")
    }
}