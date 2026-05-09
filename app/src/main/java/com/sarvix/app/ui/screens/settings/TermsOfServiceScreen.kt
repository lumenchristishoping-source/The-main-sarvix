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
fun TermsOfServiceScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PillHeader(
            title = "Terms of Service",
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
                text = "Terms of Service",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            val sections = listOf(
                "1. Acceptance of Terms" to "By accessing or using Sarvix, you agree to be bound by these Terms of Service. If you do not agree, you may not use the service.",
                "2. Eligibility" to "You must be at least 13 years old to use Sarvix. By using the service, you represent that you meet this requirement.",
                "3. User Accounts" to "You are responsible for maintaining the confidentiality of your account credentials. You agree to provide accurate information during registration.",
                "4. Acceptable Use" to "You agree not to use Sarvix for harassment, hate speech, spam, or any illegal activities. Violations may result in account termination.",
                "5. Content" to "You retain ownership of content you post. By posting, you grant Sarvix a license to use, display, and distribute your content within the app.",
                "6. Termination" to "We reserve the right to suspend or terminate your account at any time for violations of these terms or for any other reason.",
                "7. Privacy" to "Your use of Sarvix is also governed by our Privacy Policy. Please review it to understand how we handle your data.",
                "8. Disclaimers" to "Sarvix is provided 'as is' without warranties of any kind. We do not guarantee uninterrupted service or error-free operation.",
                "9. Limitation of Liability" to "To the maximum extent permitted by law, Sarvix shall not be liable for any indirect, incidental, or consequential damages.",
                "10. Changes to Terms" to "We may update these terms at any time. Continued use of Sarvix after changes constitutes acceptance of the new terms."
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
