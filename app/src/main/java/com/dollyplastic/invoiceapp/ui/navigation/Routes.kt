package com.dollyplastic.invoiceapp.ui.navigation

sealed class Route(val route: String) {

    /* ---------------- Root ---------------- */
    data object Auth : Route("auth")
    data object Main : Route("main")

    /* ---------------- AUTH ---------------- */
    data object Login : Route("login")

    /* ---------------- MAIN TABS ---------------- */

    data object Home : Route("home")
    data object Invoices : Route("invoices")
    data object Masters : Route("masters")
    data object Settings : Route("settings")

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




}
