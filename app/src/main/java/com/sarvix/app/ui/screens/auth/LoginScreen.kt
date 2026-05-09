package com.sarvix.app.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToProfileSetup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> onNavigateToMain()
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryLight, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "S",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = OnPrimary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.displaySmall,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearErrors()
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = validationErrors.containsKey("email"),
            supportingText = validationErrors["email"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryLight,
                focusedLabelColor = PrimaryLight,
                unfocusedBorderColor = DividerColor,
                unfocusedLabelColor = OnSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearErrors()
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = validationErrors.containsKey("password"),
            supportingText = validationErrors["password"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryLight,
                focusedLabelColor = PrimaryLight,
                unfocusedBorderColor = DividerColor,
                unfocusedLabelColor = OnSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Forgot Password
        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.bodyMedium,
            color = AccentCyan,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onNavigateToForgotPassword() }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // FIXED: Gradient Button for Sign In
        GradientButton(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Error Message
        if (loginState is Resource.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (loginState as Resource.Error).message ?: "Login failed",
                style = MaterialTheme.typography.bodyMedium,
                color = Error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentCyan,
                modifier = Modifier.clickable { onNavigateToSignup() }
            )
        }
    }
}
