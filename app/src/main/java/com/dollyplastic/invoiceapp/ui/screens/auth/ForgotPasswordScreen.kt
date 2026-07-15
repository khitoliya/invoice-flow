package com.dollyplastic.invoiceapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dollyplastic.invoiceapp.ui.common.BlankScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.R
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthLabel
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthTextField
import com.dollyplastic.invoiceapp.ui.common.buttons.PrimaryButton
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppText

@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        // 🔙 Top Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = AppColors.Foreground
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // 🔒 Icon (same style as Login)
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Background,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = AppColors.PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Title
        Text(
            text = "Forgot Password",
            style = AppText.Heading,
            color = AppColors.Foreground
        )

        Spacer(Modifier.height(8.dp))

        // Helper text
        Text(
            text = "Enter your email and we’ll send you a link to reset your password.",
            style = AppText.Body,
            color = AppColors.MutedForeground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Email label
        AuthLabel("Email Address")

        // Email field
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "name@example.com",
            leadingIcon = Icons.Outlined.Email
        )

        Spacer(Modifier.height(24.dp))

        // Primary action
        PrimaryButton(
            text = "Send Reset Link",
            loading = uiState is AuthUiState.Loading

        ) {
            viewModel.sendPasswordReset(email)
        }

        // Error state
        if (uiState is AuthUiState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = (uiState as AuthUiState.Error).message,
                style = AppText.Error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(32.dp))

        // Back to login (secondary navigation)
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text(
                text = "Back to Login",
                style = AppText.Link,
                color = AppColors.Primary
            )
        }
    }
}



@Composable
fun ForgotPasswordScreen2(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Forgot Password")

        Spacer(Modifier.height(12.dp))

        androidx.compose.material3.OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(Modifier.height(16.dp))

        androidx.compose.material3.Button(
            onClick = {
                viewModel.sendPasswordReset(email)
            }
        ) {
            Text("Send Reset Link")
        }

        Spacer(Modifier.height(16.dp))

        if (uiState is AuthUiState.Error) {
            Text((uiState as AuthUiState.Error).message)
        }

        Spacer(Modifier.height(16.dp))

        androidx.compose.material3.TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back to Login")
        }
    }
}

