package com.dollyplastic.invoiceapp.domain.Validation

import com.dollyplastic.invoiceapp.data.models.DeliveryType
import com.dollyplastic.invoiceapp.data.models.TransportDetails
import com.dollyplastic.invoiceapp.data.models.TransportMode

object TransportValidator {

    fun validate(
        t: TransportDetails,
        level: ValidationLevel
    ): List<ValidationError> {

        val errors = mutableListOf<ValidationError>()

        when (t.deliveryType) {

            /* ---------- OWN VEHICLE ---------- */

            DeliveryType.OWN_VEHICLE -> {

                if (t.mode != TransportMode.ROAD) {
                    errors += error("mode", "Own vehicle delivery is allowed only by Road")
                }

                if (t.vehicleNumber.isNullOrBlank()) {
                    errors += error("vehicleNumber", "Vehicle number is required")
                } else if (!ValidationUtils.isValidVehicleNumber(t.vehicleNumber)) {
                    errors += error("vehicleNumber", "Invalid vehicle number format")
                }

                if (t.vehicleType == null) {
                    errors += error("vehicleType", "Vehicle type is required")
                }
            }

            /* ---------- TRANSPORTER ---------- */

            DeliveryType.TRANSPORTER -> {

                if (t.transporterName.isNullOrBlank()) {
                    errors += error("transporterName", "Transporter name is required")
                }

                if (t.transporterId.isNullOrBlank()) {
                    errors += error("transporterId", "Transporter ID is required")
                }

                if (t.transporterDocNo.isNullOrBlank()) {
                    errors += error("transporterDocNo", docLabelError(t.mode))
                }

                if (t.transporterDocDate.isNullOrBlank()) {
                    errors += error("transporterDocDate", "Document date is required")
                }

                if (t.mode == TransportMode.ROAD) {

                    // Vehicle required ONLY if transporterId is empty
                    if (t.transporterId.isNullOrBlank()) {

                        if (t.vehicleNumber.isNullOrBlank()) {
                            errors += error("vehicleNumber", "Vehicle number is required")
                        } else if (!ValidationUtils.isValidVehicleNumber(t.vehicleNumber)) {
                            errors += error("vehicleNumber", "Invalid vehicle number format")
                        }

                        if (t.vehicleType == null) {
                            errors += error("vehicleType", "Vehicle type is required")
                        }
                    }
                }

            }

            /* ---------- BUYER PICKUP ---------- */

            DeliveryType.BUYER_PICKUP -> {
                // Nothing required
            }
        }

        return errors
    }

    private fun docLabelError(mode: TransportMode): String =
        when (mode) {
            TransportMode.ROAD -> "LR / GR number is required"
            TransportMode.RAIL -> "Railway Receipt number is required"
            TransportMode.AIR  -> "Airway Bill number is required"
            TransportMode.SHIP -> "Bill of Lading number is required"
        }

    private fun error(field: String, msg: String) =
        ValidationError(field, msg, section = "Transport")
}


