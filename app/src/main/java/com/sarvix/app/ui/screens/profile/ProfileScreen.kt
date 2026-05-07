package com.sarvix.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
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
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.data.model.User
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    
    LaunchedEffect(key1 = true) {
        viewModel.loadProfile()
    }
    
    when (val state = profileState) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            val user = state.data
            if (user != null) {
                ProfileContent(
                    user = user,
                    navController = navController
                )
            }
        }
        is Resource.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message ?: "Failed to load profile",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        null -> {}
    }
}

@Composable
fun ProfileContent(
    user: User,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(user = user)
        }
        
        // Stats (without follower counts as per requirements)
        item {
            ProfileStats(user = user)
        }
        
        // Mood Section
        item {
            MoodSection(mood = user.mood)
        }
        
        // Interests
        item {
            InterestsSection(interests = user.interests)
        }
        
        // Location & Language
        item {
            LocationSection(
                country = user.country,
                language = user.language
            )
        }
        
        // Actions
        item {
            ProfileActions(navController = navController)
        }
    }
}

@Composable
fun ProfileHeader(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = SurfaceVariant
            ) {
                if (user.profilePictureUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        tint = OnSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username
            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Display Name
            if (user.displayName.isNotEmpty()) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bio
            if (user.bio.isNotEmpty()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = OnSurface
                )
            }
        }
    }
}

@Composable
fun ProfileStats(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Online Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (user.isOnline) Success else OnSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (user.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            // Member Since
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Member",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            // Interests Count
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.interests.size.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary
                )
                Text(
                    text = "Interests",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun MoodSection(mood: MoodStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Mood",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = getMoodColor(mood).copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = mood.emoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = mood.displayName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "How you're feeling now",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InterestsSection(interests: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Interests",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (interests.isEmpty()) {
                Text(
                    text = "No interests added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { interest ->
                        AssistChip(
                            onClick = {},
                            label = { Text(interest) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSection(country: String, language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Location & Language",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Country
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Country",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = country.ifEmpty { "Not set" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Language
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Language",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = language.ifEmpty { "Not set" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileActions(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Edit Profile
            ListItem(
                headlineContent = { Text("Edit Profile") },
                leadingContent = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    navController.navigate(Screen.EditProfile.route)
                }
            )
            
            HorizontalDivider()
            
            // Settings
            ListItem(
                headlineContent = { Text("Settings") },
                leadingContent = {
                    Icon(Icons.Default.Settings, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Settings.route)
                }
            )
            
            HorizontalDivider()
            
            // Help & Support
            ListItem(
                headlineContent = { Text("Help & Support") },
                leadingContent = {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
fun getMoodColor(mood: MoodStatus): Color {
    return when (mood) {
        MoodStatus.HAPPY -> MoodHappy
        MoodStatus.EXCITED -> MoodExcited
        MoodStatus.CALM -> MoodCalm
        MoodStatus.THOUGHTFUL -> MoodThoughtful
        MoodStatus.TIRED -> MoodTired
        MoodStatus.STRESSED -> MoodStressed
        MoodStatus.INSPIRED -> MoodInspired
        MoodStatus.FOCUSED -> MoodFocused
        MoodStatus.SOCIAL -> MoodSocial
        MoodStatus.CREATIVE -> MoodCreative
        MoodStatus.REFLECTIVE -> MoodReflective
        MoodStatus.NEUTRAL -> MoodNeutral
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}