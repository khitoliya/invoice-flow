package com.dollyplastic.invoiceapp.ui.navigation

import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import java.net.URLEncoder

sealed class Route(val route: String) {

    /* ---------------- Root ---------------- */
    data object Auth : Route("auth")
    data object Main : Route("main")

    /* ---------------- AUTH ---------------- */
    data object Welcome : Route("welcome")
    data object Login : Route("login")
    data object ForgotPassword : Route("forgot_password")

    /* ---------------- APP LOCK ---------------- */
    data object AppLockGraph : Route("app_lock_graph")
    data object AppLockVerify : Route("app_lock_verify")
    data object PinSetup : Route("pin_setup")


    data object Portal : Route(
        "portal?invoiceId={invoiceId}&url={url}&storageRef={storageRef}&credentials={credentials}&isCancellation={isCancellation}&mode={mode}"
    ) {
        fun create(
            invoiceId: String,
            url: String,
            storageRef: InvoiceStorageRef,
            credentialsJson: String,
            isCancellation: Boolean = false,
            mode: String = "EWAY" // "EWAY" or "EINVOICE"
        ): String {
            return "portal?" +
                    "invoiceId=$invoiceId&" +
                    "url=${URLEncoder.encode(url, "UTF-8")}&" +
                    "storageRef=${storageRef.encode()}&" +
                    "credentials=${URLEncoder.encode(credentialsJson, "UTF-8")}&" + 
                    "isCancellation=$isCancellation&" +
                    "mode=$mode"
        }
    }

    /* ---------------- MAIN TABS ---------------- */

    data object Home : Route("home")
    
    // New List Route
    data object Invoices : Route("invoices_list")
    
    // Purchase List Route
    data object Purchases : Route("purchases_list")
    
    // New Creation/Edit Route
    data object InvoiceCreation : Route("invoice_creation?invoiceId={invoiceId}") {
        fun create(invoiceId: String? = null): String =
            if (invoiceId == null) "invoice_creation" else "invoice_creation?invoiceId=$invoiceId"
    }
    data object Masters : Route("masters")
    data object Settings : Route("settings")
    data object Credentials : Route("settings/credentials")
    data object RecycleBin : Route("settings/recycle_bin")
    data object GstConfig : Route("settings/gst_config")
    data object StateConfig : Route("settings/state_config")

    /* ---------------- MASTERS : FIRM ---------------- */

    data object FirmList : Route("firm_list")

    data object FirmForm : Route("firm_form?firmId={firmId}") {
        fun create(firmId: String? = null): String =
            if (firmId == null) {
                "firm_form"
            } else {
                "firm_form?firmId=$firmId"
            }
    }

    /* ---------------- MASTERS : PARTY ---------------- */

    data object PartyList : Route("party_list")

    data object PartyForm : Route("party_form?partyId={partyId}") {
        fun create(partyId: String? = null): String =
            if (partyId == null) {
                "party_form"
            } else {
                "party_form?partyId=$partyId"
            }
    }

    /* ---------------- MASTERS : ITEM ---------------- */

    data object ItemList : Route("item_list")

    data object ItemForm : Route("item_form?itemId={itemId}") {
        fun create(itemId: String? = null): String =
            if (itemId == null) {
                "item_form"
            } else {
                "item_form?itemId=$itemId"
            }
    }




    data object InvoiceProcessing : Route("invoice_processing?invoiceId={invoiceId}&autoStart={autoStart}") {
        fun create(invoiceId: String, autoStart: Boolean = false): String = 
            "invoice_processing?invoiceId=$invoiceId&autoStart=$autoStart"
    }
    
    /* ---------------- IMPORT FLOW ---------------- */
    data object ImportSelection : Route("import_selection")
    
    // uri
    data object ImportVerification : Route("import_verification?uri={uri}") {
        fun create(uri: String): String =
            // Use URLEncoder for URI safety in navigation
            "import_verification?uri=${URLEncoder.encode(uri, "UTF-8")}"
    }

}
