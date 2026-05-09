package com.sarvix.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.theme.*
import com.sarvix.app.viewmodel.SettingsViewModel
import com.sarvix.app.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var premiumFeatureName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Pill Header
        com.sarvix.app.ui.components.PillHeader(
            title = "Settings",
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
                .padding(16.dp)
        ) {
            // Account Section
            SettingsSectionTitle("Account")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Person,
                    label = "Edit Profile",
                    onClick = { navController.navigate(Screen.EditProfile.route) }
                )
                Divider(color = DividerColor)
                SettingsItem(
                    icon = Icons.Default.Lock,
                    label = "Change Password",
                    onClick = { navController.navigate(Screen.ChangePassword.route) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Section
            SettingsSectionTitle("Premium")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.EmojiEmotions,
                    label = "Motion Emojis",
                    onClick = {
                        premiumFeatureName = "Motion Emojis"
                        showPremiumDialog = true
                    }
                )
                Divider(color = DividerColor)
                SettingsItem(
                    icon = Icons.Default.Animation,
                    label = "Animation Effects",
                    onClick = {
                        premiumFeatureName = "Animation Effects"
                        showPremiumDialog = true
                    }
                )
                Divider(color = DividerColor)
                SettingsItem(
                    icon = Icons.Default.Psychology,
                    label = "Advanced AI",
                    onClick = {
                        premiumFeatureName = "Advanced AI"
                        showPremiumDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences Section
            SettingsSectionTitle("Preferences")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    label = "Email Preferences",
                    onClick = { navController.navigate(Screen.EmailPreferences.route) }
                )
                Divider(color = DividerColor)
                SettingsItem(
                    icon = Icons.Default.Block,
                    label = "Blocked Users",
                    onClick = { navController.navigate(Screen.BlockedUsers.route) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Legal Section
            SettingsSectionTitle("Privacy & Legal")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Shield,
                    label = "Data & Privacy",
                    onClick = { navController.navigate(Screen.DataPrivacy.route) }
                )
                Divider(color = DividerColor)
                SettingsItem(
                    icon = Icons.Default.Description,
                    label = "Terms of Service",
                    onClick = { navController.navigate(Screen.TermsOfService.route) }
                )
                Divider(color = DividerColor)
                SettingsItem(
                    icon = Icons.Default.Policy,
                    label = "Privacy Policy",
                    onClick = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out?", color = OnSurface) },
            text = { Text("Are you sure you want to log out?", color = OnSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Log Out", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            },
            containerColor = Surface
        )
    }

    // Premium Coming Soon Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text("Coming Soon", color = OnSurface) },
            text = {
                Text(
                    "$premiumFeatureName is a Premium feature and will be available soon.",
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                GradientButton(
                    onClick = { showPremiumDialog = false }
                ) {
                    Text("Got it")
                }
            },
            containerColor = Surface
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = PrimaryLight,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
