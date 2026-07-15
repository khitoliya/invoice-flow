package com.dollyplastic.invoiceapp.domain.Parsing

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream


object ExcelResultReader {

    fun readFirstSheet(file: File): List<Map<String, String>> {
        FileInputStream(file).use { fis ->
            val workbook = WorkbookFactory.create(fis)
            val sheet = workbook.getSheetAt(0)

            val headerRow = sheet.getRow(0)
                ?: throw IllegalStateException("Missing header row")

            val headers = headerRow.map { it.stringCellValue.trim() }

            val rows = mutableListOf<Map<String, String>>()

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val rowMap = mutableMapOf<String, String>()

                headers.forEachIndexed { index, header ->
                    val cell = row.getCell(index)
                    rowMap[header] = cell?.toString()?.trim() ?: ""
                }

                rows.add(rowMap)
            }

            workbook.close()
            return rows
        }
    }
}
