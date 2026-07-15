package com.dollyplastic.invoiceapp.ui.screens.applock


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dollyplastic.invoiceapp.ui.common.BlankScreen
import com.dollyplastic.invoiceapp.ui.common.buttons.PrimaryButton
import com.dollyplastic.invoiceapp.ui.common.softLayerShadow
import com.dollyplastic.invoiceapp.ui.navigation.Route
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppRadius
import com.dollyplastic.invoiceapp.ui.theme.AppText


@Composable
fun PinSetupScreen(
    navController: NavHostController,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }

    // Simple error state for feedback
    var verificationError by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {}

    LaunchedEffect(uiState) {
        if (uiState is AppLockUiState.Success) {
            viewModel.enableBiometricIfAvailable()
            navController.navigate(Route.Main.route) {
                popUpTo(Route.AppLockGraph.route) { inclusive = true }
            }
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
                .padding(horizontal = 24.dp),
        ) {

            Spacer(Modifier.height(24.dp))

            PinHeader(
                title = "Create PIN",
                subtitle = "Set a 4-digit PIN to secure your app",
                onBack = { navController.popBackStack() }
            )

            Spacer(Modifier.height(32.dp))

            PinSectionLabel("Enter PIN")

            Spacer(Modifier.height(12.dp))

            PinBoxes(
                pin = pin,
                showPin = showPin,
                active = pin.length < 4
            )

            Spacer(Modifier.height(24.dp))

            PinSectionLabel("Confirm PIN")

            Spacer(Modifier.height(12.dp))

            PinBoxes(
                pin = confirmPin,
                showPin = showPin,
                active = pin.length == 4
            )
        
            if (verificationError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "PINs do not match",
                     style = AppText.BodySmall,
                    color = AppColors.Destructive
                )
            }

            Spacer(Modifier.height(28.dp))

            PrimaryButton(
                text = "Set PIN",
                loading = false,
                onClick = {
                    if (pin.length == 4 && pin == confirmPin) {
                        viewModel.setPin(pin)
                    } else {
                        // Feedback: Clear confirm pin if they don't match or incomplete
                        verificationError = true
                        confirmPin = ""
                    }
                },
                enabled = pin.isNotEmpty() // Allow clicking to see error, or disable? distinct usage. Kept enabled mostly but basic check.
            )

            Spacer(Modifier.height(28.dp))

        }

        PinKeypad(
            showPin = showPin,
            onToggleVisibility = { showPin = !showPin },
            onNumber = { number ->
            verificationError = false
            if (pin.length < 4) {
                pin += number
            } else if (confirmPin.length < 4) {
                confirmPin += number
            }
        },
            onDelete = {
            verificationError = false
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else if (pin.isNotEmpty()) {
                pin = pin.dropLast(1)
            }
        },
            modifier = Modifier.weight(1f)
        )

    }

}
