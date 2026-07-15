package com.dollyplastic.invoiceapp.ui.navigation


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.dollyplastic.invoiceapp.ui.common.BlankScreen
import com.dollyplastic.invoiceapp.ui.screens.applock.PinSetupScreen
import com.dollyplastic.invoiceapp.ui.screens.applock.PinVerifyScreen

fun NavGraphBuilder.appLockGraph(
    navController: NavHostController
) {
    navigation(
        route = Route.AppLockGraph.route,
        startDestination = Route.AppLockVerify.route
    ) {
        composable(Route.PinSetup.route) {
            PinSetupScreen(navController)
        }
        composable(Route.AppLockVerify.route) {
            PinVerifyScreen(navController)
        }
    }
}

