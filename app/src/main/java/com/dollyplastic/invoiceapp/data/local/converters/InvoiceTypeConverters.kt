package com.dollyplastic.invoiceapp.data.local.converters

import androidx.room.TypeConverter
import com.dollyplastic.invoiceapp.data.models.*
import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class InvoiceTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromPurchaseItemList(list: List<PurchaseItem>?): String? {
        return list?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toPurchaseItemList(json: String?): List<PurchaseItem>? {
        return json?.let {
            val type = object : TypeToken<List<PurchaseItem>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromFirm(firm: Firm?): String? {
        return firm?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFirm(json: String?): Firm? {
        return json?.let { gson.fromJson(it, Firm::class.java) }
    }

    @TypeConverter
    fun fromParty(party: Party?): String? {
        return party?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toParty(json: String?): Party? {
        return json?.let { gson.fromJson(it, Party::class.java) }
    }

    @TypeConverter
    fun fromInvoiceItemList(list: List<InvoiceItem>?): String? {
        return list?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toInvoiceItemList(json: String?): List<InvoiceItem>? {
        return json?.let {
            val type = object : TypeToken<List<InvoiceItem>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromTaxSummary(taxSummary: TaxSummary?): String? {
        return taxSummary?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toTaxSummary(json: String?): TaxSummary? {
        return json?.let { gson.fromJson(it, TaxSummary::class.java) }
    }

    @TypeConverter
    fun fromTransportDetails(transportDetails: TransportDetails?): String? {
        return transportDetails?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toTransportDetails(json: String?): TransportDetails? {
        return json?.let { gson.fromJson(it, TransportDetails::class.java) }
    }

    @TypeConverter
    fun fromAdditionalDetails(additionalDetails: AdditionalDetails?): String? {
        return additionalDetails?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toAdditionalDetails(json: String?): AdditionalDetails? {
        return json?.let { gson.fromJson(it, AdditionalDetails::class.java) }
    }

    @TypeConverter
    fun fromEInvoiceDetails(details: EInvoiceDetails?): String? {
        return details?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toEInvoiceDetails(json: String?): EInvoiceDetails? {
        return json?.let { gson.fromJson(it, EInvoiceDetails::class.java) }
    }

    @TypeConverter
    fun fromEWayBillDetails(details: EWayBillDetails?): String? {
        return details?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toEWayBillDetails(json: String?): EWayBillDetails? {
        return json?.let { gson.fromJson(it, EWayBillDetails::class.java) }
    }
    
    @TypeConverter
    fun fromGstRates(rates: List<Double>?): String? {
        return rates?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toGstRates(json: String?): List<Double>? {
        return json?.let {
            val type = object : TypeToken<List<Double>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromInvoiceStatus(status: InvoiceStatus): String {
        return status.name
    }

    @TypeConverter
    fun toInvoiceStatus(name: String): InvoiceStatus {
        return try {
            InvoiceStatus.valueOf(name)
        } catch (_: Exception) {
            InvoiceStatus.DRAFT // Fallback
        }
    }
}
