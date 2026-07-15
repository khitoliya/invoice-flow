package com.dollyplastic.invoiceapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RootNavGraph(
    startDestination: String // Changed from booleans to a String
) {
    val navController = rememberNavController()

    val bottomBarVisibility = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

    androidx.compose.runtime.CompositionLocalProvider(
        com.dollyplastic.invoiceapp.ui.navigation.LocalBottomBarVisibility provides bottomBarVisibility
    ) {
        MainScaffold(navController) { padding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding)
            ) {
                authGraph(navController)
                appLockGraph(navController)
                mainGraph(navController)
            }
        }
    }
}

