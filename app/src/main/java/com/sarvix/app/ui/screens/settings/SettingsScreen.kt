package com.sarvix.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sarvix.app.ui.components.PillHeader
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.theme.Error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            PillHeader(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            item {
                SettingsSection(title = "Account") {
                    ListItem(
                        headlineContent = { Text("Change Password") },
                        leadingContent = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.BlockedUsers.route)
                        }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Email Preferences") },
                        leadingContent = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Privacy.route)
                        }
                    )
                }
            }
            
            // Notifications Section
            item {
                SettingsSection(title = "Notifications") {
                    var pushEnabled by remember { mutableStateOf(true) }
                    var messageEnabled by remember { mutableStateOf(true) }
                    var matchEnabled by remember { mutableStateOf(true) }
                    
                    ListItem(
                        headlineContent = { Text("Push Notifications") },
                        leadingContent = {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        },
                        trailingContent = {
                            Switch(
                                checked = pushEnabled,
                                onCheckedChange = { pushEnabled = it }
                            )
                        }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Message Notifications") },
                        leadingContent = {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                        },
                        trailingContent = {
                            Switch(
                                checked = messageEnabled,
                                onCheckedChange = { messageEnabled = it }
                            )
                        }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Match Notifications") },
                        leadingContent = {
                            Icon(Icons.Default.People, contentDescription = null)
                        },
                        trailingContent = {
                            Switch(
                                checked = matchEnabled,
                                onCheckedChange = { matchEnabled = it }
                            )
                        }
                    )
                }
            }
            
            // Privacy Section
            item {
                SettingsSection(title = "Privacy") {
                    ListItem(
                        headlineContent = { Text("Blocked Users") },
                        leadingContent = {
                            Icon(Icons.Default.Block, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable { }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Data & Privacy") },
                        leadingContent = {
                            Icon(Icons.Default.Security, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable { }
                    )
                }
            }
            
            // Premium Section (Monetization)
            item {
                SettingsSection(title = "Premium") {
                    ListItem(
                        headlineContent = { Text("Motion Emojis") },
                        supportingContent = { Text("Unlock animated emojis for your messages") },
                        leadingContent = {
                            Icon(Icons.Default.EmojiEmotions, contentDescription = null)
                        },
                        trailingContent = {
                            Button(onClick = { }) {
                                Text("Upgrade")
                            }
                        },
                        modifier = Modifier.clickable { }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Animation Effects") },
                        supportingContent = { Text("Add visual effects to your messages") },
                        leadingContent = {
                            Icon(Icons.Default.Animation, contentDescription = null)
                        },
                        trailingContent = {
                            Button(onClick = { }) {
                                Text("Upgrade")
                            }
                        },
                        modifier = Modifier.clickable { }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Advanced AI Tools") },
                        supportingContent = { Text("Enhanced clarification and tone analysis") },
                        leadingContent = {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                        },
                        trailingContent = {
                            Button(onClick = { }) {
                                Text("Upgrade")
                            }
                        },
                        modifier = Modifier.clickable { }
                    )
                }
            }
            
            // About Section
            item {
                SettingsSection(title = "About") {
                    ListItem(
                        headlineContent = { Text("Terms of Service") },
                        leadingContent = {
                            Icon(Icons.Default.Description, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable { }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Privacy Policy") },
                        leadingContent = {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable { }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("App Version") },
                        supportingContent = { Text("1.0.0") },
                        leadingContent = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        }
                    )
                }
            }
            
            // Logout Button
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out")
                }
            }
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}