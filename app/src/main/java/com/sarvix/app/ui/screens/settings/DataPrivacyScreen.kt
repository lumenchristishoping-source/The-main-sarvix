package com.sarvix.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sarvix.app.ui.components.PillHeader
import com.sarvix.app.ui.theme.*

@Composable
fun DataPrivacyScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PillHeader(
            title = "Data & Privacy",
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            PrivacySection("Your Data") {
                Text(
                    "Sarvix stores your profile information, chat messages, and match preferences securely using Firebase. Your data is encrypted in transit and at rest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            PrivacySection("Data Collection") {
                Text(
                    "We collect: email, username, display name, bio, mood, interests, country, language, profile picture, and chat content. We do NOT sell your data to third parties.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            PrivacySection("Data Retention") {
                Text(
                    "Your account data is retained while your account is active. Chat messages are stored for 90 days after the last activity in a conversation. You can request data deletion at any time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            PrivacySection("Third Parties") {
                Text(
                    "We use Firebase (Google Cloud) for data storage, authentication, and push notifications. FCM tokens are used only for delivering Sarvix notifications.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PrivacySection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
