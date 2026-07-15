package com.dollyplastic.invoiceapp.ui.screens.applock


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dollyplastic.invoiceapp.ui.common.softLayerShadow
import com.dollyplastic.invoiceapp.ui.navigation.Route
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppRadius
import com.dollyplastic.invoiceapp.ui.theme.AppText


@Composable
fun PinVerifyScreen(
    navController: NavHostController,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        if (!viewModel.isPinSet()) {
            navController.navigate(Route.PinSetup.route) {
                popUpTo(Route.AppLockGraph.route) { inclusive = false }
            }
            return@LaunchedEffect
        }
    }

    val context = LocalContext.current
    val activity = context as FragmentActivity
    var showPin by remember { mutableStateOf(false) }


    val biometricManager = remember {
        BiometricAuthManager(context)
    }

    val biometricEnabled by produceState(initialValue = false) {
        value = viewModel.isBiometricEnabled()
    }

    LaunchedEffect(Unit) {
        if (biometricEnabled && biometricManager.isFingerprintAvailable()) {
            biometricManager.authenticate(
                activity = activity,
                onSuccess = {
                    viewModel.markUnlocked()
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.AppLockGraph.route) { inclusive = true }
                    }
                },
                onFailure = {
                    // fallback silently to PIN
                }
            )
        }
    }
    var pin by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        activity.finish()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AppLockUiState.Success -> {
                navController.navigate(Route.Main.route) {
                    popUpTo(Route.AppLockGraph.route) { inclusive = true }
                }
            }
            is AppLockUiState.Error -> {
                pin = ""
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize().background(AppColors.FieldBackground)
    )
    {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .softLayerShadow(
                    color = Color.Black.copy(alpha = 0.05f),
                    cornersRadius = AppRadius.XXXl,
                    shadowBlurRadius = 16.dp,
                    offsetY = 8.dp
                )
                .background(Color.White, RoundedCornerShape(bottomStart = AppRadius.XXXl, bottomEnd = AppRadius.XXXl))
                .padding(horizontal = 24.dp)
                //.weight(1f)
            ,horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            PinHeader(
                title = "Enter PIN",
                subtitle = "",
                onBack = { activity.finish() },
                icon = com.dollyplastic.invoiceapp.R.drawable.ic_launcher_foreground,
                iconSize = 90.dp
            )

            Spacer(Modifier.height(32.dp))

            PinBoxes(
                pin = pin,
                showPin = showPin,
                active = true
            )

            if (uiState is AppLockUiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = (uiState as AppLockUiState.Error).message,
                    style = AppText.BodySmall,
                    color = AppColors.Destructive
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Forgot PIN?",
                style = AppText.Link,
                modifier = Modifier.clickable { /* TODO */ },
                color = AppColors.PrimaryBlue
            )

            Spacer(Modifier.height(32.dp))



            LaunchedEffect(pin) {
                if (pin.length == 4) {
                    viewModel.verifyPin(pin)
                }
            }
        }
        Column(modifier = Modifier
            //.weight(1f)
        ) {
            PinKeypad(
                showPin = showPin,
                onToggleVisibility = { showPin = !showPin },
                onNumber = {
                    if (pin.isEmpty()) viewModel.resetState()
                    if (pin.length < 4) pin += it
                },
                onDelete = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                },
            )
        }

    }
}
