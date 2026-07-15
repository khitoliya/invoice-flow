package com.dollyplastic.invoiceapp.data.credentials

data class Credential(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val username: String,
    val password: String, // In a real app, this should be encrypted
    val url: String = "https://ewaybillgst.gov.in",
    val firmId: String? = null // Link to a specific Firm
)
