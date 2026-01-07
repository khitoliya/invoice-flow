package com.dollyplastic.invoiceapp.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.dollyplastic.invoiceapp.ui.screens.home.HomeScreen
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.MastersScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.firm.FirmFormScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.firm.FirmListScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.item.ItemFormScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.item.ItemListScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.party.PartyFormScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.party.PartyListScreen
import com.dollyplastic.invoiceapp.ui.screens.settings.SettingsScreen

fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = Route.Home.route,
        route = Route.Main.route
    ) {
        composable(Route.Home.route) { HomeScreen() }
        composable(Route.Invoices.route) { InvoiceScreen() }
        composable(Route.Masters.route) { MastersScreen(navController) }
        composable(Route.Settings.route) { SettingsScreen() }
        //------------------Masters Sub Graph------------------//
        composable(Route.FirmList.route) {
            FirmListScreen(navController)
        }

        composable(
            route = Route.FirmForm.route,
            arguments = listOf(
                navArgument("firmId") {
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val firmId = it.arguments?.getString("firmId")
            FirmFormScreen(navController, firmId)
        }
    }

    composable(Route.PartyList.route) {
        PartyListScreen(navController)
    }

    composable(
        route = Route.PartyForm.route,
        arguments = listOf(
            navArgument("partyId") {
                nullable = true
                defaultValue = null
            }
        )
    ) {
        val partyId = it.arguments?.getString("partyId")
        PartyFormScreen(navController, partyId)
    }
    composable(Route.ItemList.route) {
        ItemListScreen(navController)
    }

    composable(
        route = Route.ItemForm.route,
        arguments = listOf(
            navArgument("itemId") {
                nullable = true
                defaultValue = null
            }
        )
    ) {
        val itemId = it.arguments?.getString("itemId")
        ItemFormScreen(navController, itemId)
    }


}

