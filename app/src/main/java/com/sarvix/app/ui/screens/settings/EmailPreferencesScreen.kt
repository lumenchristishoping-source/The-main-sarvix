package com.sarvix.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sarvix.app.ui.components.PillHeader
import com.sarvix.app.ui.theme.*

@Composable
fun EmailPreferencesScreen(navController: NavController) {
    var marketingEmails by remember { mutableStateOf(true) }
    var newMatchEmails by remember { mutableStateOf(true) }
    var messageEmails by remember { mutableStateOf(false) }
    var productUpdates by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PillHeader(
            title = "Email Preferences",
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
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = PrimaryLight.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Email Preferences",
                style = MaterialTheme.typography.headlineSmall,
                color = OnSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Control which emails you receive from Sarvix",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            SettingsCard {
                EmailToggleItem(
                    title = "New Match Notifications",
                    description = "Get notified when you match with someone new",
                    checked = newMatchEmails,
                    onCheckedChange = { newMatchEmails = it }
                )
                Divider(color = DividerColor)
                EmailToggleItem(
                    title = "New Messages",
                    description = "Get notified of new messages when offline",
                    checked = messageEmails,
                    onCheckedChange = { messageEmails = it }
                )
                Divider(color = DividerColor)
                EmailToggleItem(
                    title = "Product Updates",
                    description = "Learn about new features and improvements",
                    checked = productUpdates,
                    onCheckedChange = { productUpdates = it }
                )
                Divider(color = DividerColor)
                EmailToggleItem(
                    title = "Marketing & Promotions",
                    description = "Tips, offers, and promotional content",
                    checked = marketingEmails,
                    onCheckedChange = { marketingEmails = it }
                )
            }
        }
    }
}

@Composable
private fun EmailToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryLight,
                checkedTrackColor = PrimaryLight.copy(alpha = 0.5f)
            )
        )
    }
}
