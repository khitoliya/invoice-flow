package com.dollyplastic.invoiceapp.ui.screens.invoice.A_HeaderSection

import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import com.dollyplastic.invoiceapp.domain.Utils.FinancialYearUtils

object InvoiceNumberGenerator {

    suspend fun generate(
        repo: InvoiceRepository,
        firm: Firm,
        invoiceDate: String
    ): Pair<Int, String> {

        val fy =
            FinancialYearUtils.fromDate(
                DateUtils.parse(invoiceDate)
            )

        val lastSeq =
            repo.getLastInvoiceSequence(
                firm.gstin,
                fy
            )

        val nextSeq = lastSeq + 1
        val number = nextSeq.toString().padStart(3, '0')

        return nextSeq to number
    }
}