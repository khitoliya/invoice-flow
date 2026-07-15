package com.dollyplastic.invoiceapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dollyplastic.invoiceapp.ui.common.BlankScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.ui.navigation.Route
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.dollyplastic.invoiceapp.R
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthLabel
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthTextField
import com.dollyplastic.invoiceapp.ui.common.buttons.AuthDivider
import com.dollyplastic.invoiceapp.ui.common.buttons.GoogleButton
import com.dollyplastic.invoiceapp.ui.common.buttons.PrimaryButton
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppText


@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()



    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    fun signInWithGoogle() {
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        context.getString(R.string.default_web_client_id)
                    )
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // ✅ Correct call
                val response = credentialManager.getCredential(
                    context,
                    request
                )

                // ✅ Correct extraction
                val credential = response.credential

                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    val idToken = googleIdTokenCredential.idToken

                    viewModel.loginWithGoogle(idToken) { destination ->
                        navController.navigate(destination) {
                            popUpTo(Route.Auth.route) { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // optional: show snackbar
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(64.dp))

        // App Icon
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = Color(0xFF2F70B7),
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Welcome Back",
            style = AppText.Heading,
            color = AppColors.Foreground,
        )



        Spacer(Modifier.height(6.dp))

        Text(
            "Sign in to continue",
            style = AppText.Body,
            color = AppColors.MutedForeground
        )

        Spacer(Modifier.height(32.dp))

        AuthLabel("Email Address")

        // Email
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "name@example.com",
            leadingIcon = Icons.Outlined.Email
        )

        Spacer(Modifier.height(16.dp))

        AuthLabel("Password")

        // Password
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Enter your password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            showPassword = showPassword,
            onTogglePassword = { showPassword = !showPassword }
        )

        Spacer(Modifier.height(12.dp))

        // Remember / Forgot row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row() {}

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = { navController.navigate(Route.ForgotPassword.route) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Forgot?",
                    style = AppText.Link,
                    color = AppColors.Foreground
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Sign In
        PrimaryButton(
            text = "Sign In",
            loading = uiState is AuthUiState.Loading
        ) {
            viewModel.loginWithEmail(email, password) { destination ->
                navController.navigate(destination) {
                    popUpTo(Route.Auth.route) { inclusive = true }
                }
            }
        }

        if (uiState is AuthUiState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                (uiState as AuthUiState.Error).message,
                style = AppText.Error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(32.dp))

        AuthDivider()

        Spacer(Modifier.height(24.dp))

        GoogleButton(
            onClick = { signInWithGoogle() }
        )
    }
}

@Composable
fun LoginScreen2(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = androidx.credentials.CredentialManager.create(context)

    fun signInWithGoogle() {
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken) { destination ->
                        navController.navigate(destination) {
                            popUpTo(Route.Auth.route) { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
                // Optionally show a snackbar or update viewModel error state if needed
                // But ViewModel handles errors mostly
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login")

        Spacer(Modifier.height(12.dp))

        androidx.compose.material3.OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(Modifier.height(8.dp))

        androidx.compose.material3.OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(Modifier.height(16.dp))

        androidx.compose.material3.Button(
            onClick = {
                viewModel.loginWithEmail(email, password) { destination ->
                    navController.navigate(destination) {
                        popUpTo(Route.Auth.route) { inclusive = true }
                    }
                }
            }
        ) {
            Text("Login")
        }

        Spacer(Modifier.height(8.dp))

        androidx.compose.material3.TextButton(
            onClick = { navController.navigate(Route.ForgotPassword.route) }
        ) {
            Text("Forgot Password?")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { signInWithGoogle() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder for Google Icon if not available, ideally use a specific google icon
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text("Sign in with Google")
        }

        Spacer(Modifier.height(16.dp))

        if (uiState is AuthUiState.Loading) {
            Text("Logging in...")
        }

        if (uiState is AuthUiState.Error) {
            Text((uiState as AuthUiState.Error).message)
        }
    }
}
