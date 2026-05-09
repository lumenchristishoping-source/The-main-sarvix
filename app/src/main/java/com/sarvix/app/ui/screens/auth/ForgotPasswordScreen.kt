package com.sarvix.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val passwordResetState by viewModel.passwordResetState.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AccentCyan
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.headlineLarge,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Enter your email address and we'll send you a link to reset your password.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
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
                imeAction = ImeAction.Done
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
        Spacer(modifier = Modifier.height(24.dp))

        // FIXED: Gradient Button for Send
        GradientButton(
            onClick = { viewModel.sendPasswordResetEmail(email) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Send Reset Link",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Success Message
        if (passwordResetState is Resource.Success) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Reset link sent! Check your email.",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentCyan,
                textAlign = TextAlign.Center
            )
        }
        // Error Message
        if (passwordResetState is Resource.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (passwordResetState as Resource.Error).message ?: "Failed to send reset link",
                style = MaterialTheme.typography.bodyMedium,
                color = Error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
