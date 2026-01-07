package com.dollyplastic.invoiceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dollyplastic.invoiceapp.ui.navigation.RootNavGraph
import com.dollyplastic.invoiceapp.ui.theme.InvoiceAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvoiceAppTheme {

                RootNavGraph(
                    isLoggedIn = true
                )
            }
        }
    }
}


