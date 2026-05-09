package com.sarvix.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sarvix.app.ui.components.PillHeader
import com.sarvix.app.ui.theme.*

@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PillHeader(
            title = "Privacy Policy",
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
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            val sections = listOf(
                "1. Information We Collect" to "We collect: email address, username (@handle), display name, bio, mood status, interests, country, language preference, profile picture, FCM token, chat messages, and match data.",
                "2. How We Use Information" to "We use your information to: provide matching services, enable chat communication, send notifications, improve the app, and ensure community safety.",
                "3. Information Sharing" to "We do not sell your personal data. We share data only with: Firebase (Google Cloud) for storage and authentication, and as required by law.",
                "4. Data Security" to "We use industry-standard encryption (TLS in transit, AES at rest). Firebase handles authentication and data storage with enterprise-grade security.",
                "5. Chat Privacy" to "Chat messages are encrypted and stored securely. Messages are retained for 90 days after last conversation activity. Clarifications are processed securely.",
                "6. Your Rights" to "You can: access your data, update your profile, delete your account (which removes your data), and export your data by contacting support.",
                "7. Cookies & Tracking" to "Sarvix does not use cookies. We use Firebase Analytics for anonymous usage statistics to improve the app.",
                "8. Children's Privacy" to "Sarvix is not intended for users under 13. We do not knowingly collect data from children under 13.",
                "9. Changes to Policy" to "We may update this policy. Significant changes will be notified via in-app notice or email.",
                "10. Contact Us" to "For privacy questions or data requests, contact: privacy@sarvix.app"
            )

            sections.forEach { (title, body) ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
