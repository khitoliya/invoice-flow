package com.dollyplastic.invoiceapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavGraph(isLoggedIn: Boolean) {
    val navController = rememberNavController()

    MainScaffold(navController) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Route.Main.route else Route.Auth.route,
            modifier = Modifier.padding(padding)
        ) {
            authGraph(navController)
            mainGraph(navController)
        }
    }
}

