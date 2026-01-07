package com.dollyplastic.invoiceapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.dollyplastic.invoiceapp.ui.screens.auth.LoginScreen

fun NavGraphBuilder.authGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = Route.Login.route,
        route = Route.Auth.route
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.Auth.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
