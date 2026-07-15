package com.dollyplastic.invoiceapp.ui.navigation


import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.dollyplastic.invoiceapp.ui.common.BlankScreen
import com.dollyplastic.invoiceapp.ui.screens.auth.ForgotPasswordScreen
import com.dollyplastic.invoiceapp.ui.screens.auth.LoginScreen

fun NavGraphBuilder.authGraph(
    navController: NavHostController
) {
    navigation(
        route = Route.Auth.route,
        startDestination = Route.Login.route
    ) {



        composable(Route.Login.route) {
            LoginScreen(navController)
        }

        composable(Route.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
    }
}
