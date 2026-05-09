package com.sarvix.app.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.components.PillHeader
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.AuthViewModel

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val changePasswordState by viewModel.changePasswordState.collectAsState()

    LaunchedEffect(changePasswordState) {
        if (changePasswordState is Resource.Success) {
            navController.navigateUp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PillHeader(
            title = "Change Password",
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AccentCyan
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Update your password",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Current Password
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = DividerColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // New Password
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                isError = newPassword.isNotEmpty() && newPassword.length < 8,
                supportingText = if (newPassword.isNotEmpty() && newPassword.length < 8) {
                    { Text("Must be at least 8 characters", color = Error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = DividerColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                supportingText = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                    { Text("Passwords don't match", color = Error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = DividerColor
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Change Button
            GradientButton(
                onClick = {
                    viewModel.changePassword(currentPassword, newPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentPassword.isNotEmpty() &&
                        newPassword.length >= 8 &&
                        newPassword == confirmPassword
            ) {
                Text("Update Password")
            }

            // Error / Success
            if (changePasswordState is Resource.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (changePasswordState as Resource.Error).message ?: "Failed to change password",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (changePasswordState is Resource.Success) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Password updated successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Success,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
