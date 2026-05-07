@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.sarvix.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.data.model.User
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.ui.components.FlowRow
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    
    // Form state
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(MoodStatus.NEUTRAL) }
    var showMoodSelector by remember { mutableStateOf(false) }
    
    val availableInterests by viewModel.availableInterests.collectAsState()
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    
    // Load profile data
    LaunchedEffect(key1 = true) {
        viewModel.loadProfile()
    }
    
    // Populate form when profile loads
    LaunchedEffect(profileState) {
        (profileState as? Resource.Success)?.data?.let { user ->
            displayName = user.displayName
            bio = user.bio
            selectedMood = user.mood
            selectedInterests = user.interests.toSet()
        }
    }
    
    // Handle update success
    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            navController.navigateUp()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateProfile(
                                displayName = displayName.takeIf { it.isNotBlank() },
                                bio = bio.takeIf { it.isNotBlank() },
                                mood = selectedMood,
                                interests = selectedInterests.toList()
                            )
                        },
                        enabled = updateState !is Resource.Loading
                    ) {
                        if (updateState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
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
            // Profile Picture
            item {
                ProfilePictureSection(
                    profileState = profileState,
                    uploadState = uploadState,
                    onUploadImage = { /* TODO: Image picker */ }
                )
            }
            
            // Basic Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Basic Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Display Name
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            supportingText = { Text("${bio.length}/500") }
                        )
                    }
                }
            }
            
            // Mood Selection
            item {
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showMoodSelector = true }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = getMoodColor(selectedMood).copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedMood.emoji,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedMood.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Tap to change",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Interests
            item {
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
                        
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableInterests.forEach { interest ->
                                val isSelected = selectedInterests.contains(interest)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedInterests = if (isSelected) {
                                            selectedInterests - interest
                                        } else {
                                            selectedInterests + interest
                                        }
                                    },
                                    label = { Text(interest) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Error Message
            item {
                if (updateState is Resource.Error) {
                    Text(
                        text = (updateState as Resource.Error).message ?: "Failed to update profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Mood Selector Dialog
    if (showMoodSelector) {
        AlertDialog(
            onDismissRequest = { showMoodSelector = false },
            title = { Text("Select Your Mood") },
            text = {
                Column {
                    MoodStatus.values().forEach { mood ->
                        ListItem(
                            headlineContent = { Text(mood.displayName) },
                            leadingContent = {
                                Text(
                                    text = mood.emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            modifier = Modifier.clickable {
                                selectedMood = mood
                                showMoodSelector = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoodSelector = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfilePictureSection(
    profileState: Resource<User>?,
    uploadState: Resource<String>?,
    onUploadImage: () -> Unit
) {
    val profilePictureUrl = (profileState as? Resource.Success)?.data?.profilePictureUrl ?: ""
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = SurfaceVariant
            ) {
                if (profilePictureUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profilePictureUrl,
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
            
            // Edit Button
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .clickable(onClick = onUploadImage),
                shape = CircleShape,
                color = Primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (uploadState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = OnPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Photo",
                            tint = OnPrimary
                        )
                    }
                }
            }
        }
    }
}
