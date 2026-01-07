package com.dollyplastic.invoiceapp.ui.screens.masters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastersScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Masters") }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            item {
                ListItem(
                    headlineContent = { Text("Firm Master") },
                    supportingContent = { Text("Manage your firms") },
                    modifier = Modifier.clickable {
                        navController.navigate(Route.FirmList.route)
                    }
                )
                Divider()
            }

            item {
                ListItem(
                    headlineContent = { Text("Party Master") },
                    supportingContent = { Text("Manage buyers / parties") },
                    modifier = Modifier.clickable {
                        navController.navigate(Route.PartyList.route)
                    }
                )
                Divider()
            }

            item {
                ListItem(
                    headlineContent = { Text("Item Master") },
                    supportingContent = { Text("Manage items & GST rates") },
                    modifier = Modifier.clickable {

                        navController.navigate(Route.ItemList.route)
                    }
                )
                Divider()
            }
        }
    }
}

