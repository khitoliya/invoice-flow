package com.dollyplastic.invoiceapp

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dollyplastic.invoiceapp.ui.navigation.RootNavGraph
import com.dollyplastic.invoiceapp.ui.theme.InvoiceAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.lifecycle.lifecycleScope
import com.dollyplastic.invoiceapp.ui.screens.splash.AnimatedSplash
import com.dollyplastic.invoiceapp.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    @javax.inject.Inject lateinit var syncManager: com.dollyplastic.invoiceapp.data.sync.SyncManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Disable the default exit animation (fade/zoom) to make the transition
        // to our custom splash screen feel instant and seamless.
        splashScreen.setOnExitAnimationListener { splashProvider ->
            splashProvider.remove()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sync Data
        lifecycleScope.launch { syncManager.syncAll() }
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        setContent {
            InvoiceAppTheme {
                val startDestination by mainViewModel.startDestination.collectAsState()
                val isLoading by mainViewModel.isLoading.collectAsState()


                if (isLoading) {
                    SplashScreen()
                } else {
                    RootNavGraph(startDestination)
                }
            }
        }
    }
}


