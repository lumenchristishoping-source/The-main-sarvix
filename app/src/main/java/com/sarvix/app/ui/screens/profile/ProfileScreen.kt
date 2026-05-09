package com.sarvix.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val currentUserMood by viewModel.currentUserMood.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.loadUserProfile()
        viewModel.loadCurrentUserMood()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        when (val state = profileState) {
            is Resource.Loading -> {
                // Blank - no spinner
            }
            is Resource.Success -> {
                val user = state.data
                if (user != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(32.dp))

                            // Profile Picture with mood border
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = moodGradientBrush(currentUserMood),
                                        shape = CircleShape
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(Background)
                                    .padding(3.dp)
                            ) {
                                if (user.profilePictureUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = user.profilePictureUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(SurfaceVariant)
                                            .padding(24.dp),
                                        tint = OnSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Username with mood emoji
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = user.mood?.emoji ?: MoodStatus.NEUTRAL.emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = OnSurface
                                )
                            }

                            // Display Name
                            if (user.displayName.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurfaceVariant
                                )
                            }

                            // Mood Display
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = getMoodColor(user.mood ?: MoodStatus.NEUTRAL).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${user.mood?.emoji ?: MoodStatus.NEUTRAL.emoji} ${user.mood?.displayName ?: "Neutral"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = getMoodColor(user.mood ?: MoodStatus.NEUTRAL),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Profile Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Bio
                                    if (user.bio.isNotEmpty()) {
                                        Text(
                                            text = "About",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = OnSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = user.bio,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = OnSurface
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = DividerColor)
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    // Country & Language
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        ProfileInfoItem("Country", user.country)
                                        ProfileInfoItem("Language", user.language)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = DividerColor)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Interests
                                    Text(
                                        text = "Interests",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = OnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    com.sarvix.app.ui.components.FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        user.interests.forEach { interest ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = SurfaceVariant
                                            ) {
                                                Text(
                                                    text = interest,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = OnSurface,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Actions
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column {
                                    ProfileActionItem(
                                        icon = Icons.Default.Edit,
                                        label = "Edit Profile",
                                        onClick = { navController.navigate(Screen.EditProfile.route) }
                                    )
                                    Divider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                                    ProfileActionItem(
                                        icon = Icons.Default.Settings,
                                        label = "Settings",
                                        onClick = { navController.navigate(Screen.Settings.route) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message ?: "Failed to load profile",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.loadUserProfile() }) {
                            Text("Retry", color = PrimaryLight)
                        }
                    }
                }
            }
            null -> {}
        }
    }
}

@Composable
private fun ProfileInfoItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifEmpty { "Not set" },
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface
        )
    }
}

@Composable
private fun ProfileActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AccentCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
