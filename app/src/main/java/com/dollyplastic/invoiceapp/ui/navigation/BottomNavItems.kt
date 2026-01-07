package com.dollyplastic.invoiceapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings

val bottomNavItems = listOf(
    BottomNavItem(Route.Home, "Home", Icons.Default.Home),
    BottomNavItem(Route.Invoices, "Invoices", Icons.Default.Receipt),
    BottomNavItem(Route.Masters, "masters", Icons.Default.Dashboard),
    BottomNavItem(Route.Settings, "Settings", Icons.Default.Settings)
)
