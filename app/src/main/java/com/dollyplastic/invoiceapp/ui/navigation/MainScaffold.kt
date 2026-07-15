package com.dollyplastic.invoiceapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value
            ?.destination?.route

    // Hide BottomBar on Auth screens and Portal
    val isRouteSupported = currentRoute != Route.AppLockGraph.route &&
            currentRoute != Route.AppLockVerify.route &&
            currentRoute != Route.Login.route &&
            currentRoute != Route.ForgotPassword.route &&
            currentRoute != Route.PinSetup.route &&
            currentRoute != Route.Welcome.route &&
            currentRoute != Route.PartyForm.route &&
            currentRoute != Route.FirmForm.route &&
            currentRoute != Route.ItemForm.route &&
            currentRoute != Route.Settings.route &&
            currentRoute?.startsWith("portal") != true

    val isVisibleLocally = LocalBottomBarVisibility.current.value
    val showBottomBar = isRouteSupported && isVisibleLocally

    Scaffold(
        bottomBar = {
            androidx.compose.animation.AnimatedVisibility(
                visible = showBottomBar,
                enter = androidx.compose.animation.slideInVertically { it },
                exit = androidx.compose.animation.slideOutVertically { it }
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route.route,
                            onClick = {
                                navController.navigate(item.route.route) {
                                    popUpTo(Route.Main.route)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(item.icon, null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        content(padding)
    }
}

val LocalBottomBarVisibility = androidx.compose.runtime.compositionLocalOf { 
    androidx.compose.runtime.mutableStateOf(true) 
}
