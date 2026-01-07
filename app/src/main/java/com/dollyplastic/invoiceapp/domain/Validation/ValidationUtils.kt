package com.dollyplastic.invoiceapp.domain.Validation

object ValidationUtils {

    private val gstinRegex =
        Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")

    private val vehicleRegex = Regex(
        "^(" +
                "[A-Z]{2}[0-9]{1,2}[A-Z]{0,3}[0-9]{4}" +   // Normal vehicles
                "|" +
                "(DF|TR|BP|NP)X{6}" +                     // Temporary / special vehicles
                ")$"
    )
    private val ifscRegex =
        Regex("^[A-Z]{4}0[A-Z0-9]{6}$")

    fun isValidIFSC(ifsc: String): Boolean =
        ifscRegex.matches(ifsc)



    fun isValidGSTIN(gstin: String): Boolean =
        gstinRegex.matches(gstin)

    fun isValidPincode(pincode: String): Boolean =
        pincode.length == 6 && pincode.all { it.isDigit() }

    fun isValidVehicleNumber(vehicle: String): Boolean =
        vehicleRegex.matches(vehicle)

    fun isValidHSN(hsn: String): Boolean =
        hsn.all { it.isDigit() } && hsn.length in listOf(2, 4, 6, 8)
}
