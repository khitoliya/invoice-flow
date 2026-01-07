package com.dollyplastic.invoiceapp.domain.config


data class IndianState(
    val name: String,
    val code: String,
    val isUnionTerritory: Boolean
)

object StateConfig {

    val STATES = listOf(
        IndianState("Jammu & Kashmir", "01", false),
        IndianState("Himachal Pradesh", "02", false),
        IndianState("Punjab", "03", false),
        IndianState("Chandigarh", "04", true),
        IndianState("Uttarakhand", "05", false),
        IndianState("Haryana", "06", false),
        IndianState("Delhi", "07", true),
        IndianState("Rajasthan", "08", false),
        IndianState("Uttar Pradesh", "09", false),
        IndianState("Bihar", "10", false),
        IndianState("Sikkim", "11", false),
        IndianState("Arunachal Pradesh", "12", false),
        IndianState("Nagaland", "13", false),
        IndianState("Manipur", "14", false),
        IndianState("Mizoram", "15", false),
        IndianState("Tripura", "16", false),
        IndianState("Meghalaya", "17", false),
        IndianState("Assam", "18", false),
        IndianState("West Bengal", "19", false),
        IndianState("Jharkhand", "20", false),
        IndianState("Odisha", "21", false),
        IndianState("Chhattisgarh", "22", false),
        IndianState("Madhya Pradesh", "23", false),
        IndianState("Gujarat", "24", false),
        IndianState("Maharashtra", "27", false),
        IndianState("Karnataka", "29", false),
        IndianState("Goa", "30", false),
        IndianState("Kerala", "32", false),
        IndianState("Tamil Nadu", "33", false),
        IndianState("Puducherry", "34", true),
        IndianState("Telangana", "36", false),
        IndianState("Andhra Pradesh", "37", false),
        IndianState("Ladakh", "38", true)
    )

    fun getByCode(code: String): IndianState? =
        STATES.firstOrNull { it.code == code }
}
