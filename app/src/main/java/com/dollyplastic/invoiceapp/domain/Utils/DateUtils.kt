package com.dollyplastic.invoiceapp.domain.Utils



import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {

    private val DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun today(): String =
        LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")).format(DISPLAY_FORMAT)

    fun parse(date: String): LocalDate =
        LocalDate.parse(date, DISPLAY_FORMAT)

    fun format(date: LocalDate): String =
        date.format(DISPLAY_FORMAT)
}
