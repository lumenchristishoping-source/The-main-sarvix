package com.sarvix.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sarvix.app.ui.components.PillHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            PillHeader(
                title = "Privacy",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Your Privacy Matters",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Sarvix is designed to reduce misunderstandings without heavy surveillance. We do not sell your personal chat data to third parties.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                var showOnlineStatus by remember { mutableStateOf(true) }
                var showReadReceipts by remember { mutableStateOf(true) }

                ListItem(
                    headlineContent = { Text("Show Online Status") },
                    supportingContent = { Text("Let others know when you are active") },
                    trailingContent = {
                        Switch(checked = showOnlineStatus, onCheckedChange = { showOnlineStatus = it })
                    }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Read Receipts") },
                    supportingContent = { Text("Others can see when you've read their messages") },
                    trailingContent = {
                        Switch(checked = showReadReceipts, onCheckedChange = { showReadReceipts = it })
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                com.sarvix.app.ui.components.GradientButton(
                    text = "Export My Data",
                    onClick = { /* Handle data export */ },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* Handle account deletion */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            }
        }
    }
}
