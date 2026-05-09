package com.sarvix.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val currentUserMood by viewModel.currentUserMood.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(MoodStatus.NEUTRAL) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // FIXED: Image picker using ActivityResultContracts.GetContent()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            viewModel.uploadProfilePicture(it)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.loadUserProfile()
        viewModel.loadCurrentUserMood()
    }

    LaunchedEffect(profileState) {
        if (profileState is Resource.Success) {
            val user = (profileState as Resource.Success).data
            user?.let {
                displayName = it.displayName
                bio = it.bio
                it.mood?.let { mood -> selectedMood = mood }
            }
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            navController.navigateUp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Pill Header
        com.sarvix.app.ui.components.PillHeader(
            title = "Edit Profile",
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
            when (val state = profileState) {
                is Resource.Success -> {
                    val user = state.data
                    if (user != null) {
                        // Profile Picture with mood gradient border
                        Box(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = moodGradientBrush(selectedMood),
                                        shape = CircleShape
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(Background)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                            ) {
                                val imageUrl = profileImageUri?.toString()
                                    ?: user.profilePictureUrl
                                if (imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(SurfaceVariant)
                                            .padding(24.dp),
                                        tint = OnSurfaceVariant
                                    )
                                }
                                // Camera overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color(0x66000000),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change Photo",
                                        tint = OnPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        // Upload state
                        when (uploadState) {
                            is Resource.Loading -> {
                                // Blank - no spinner
                            }
                            is Resource.Success -> {
                                Text(
                                    text = "Photo updated!",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentCyan,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            is Resource.Error -> {
                                Text(
                                    text = (uploadState as Resource.Error).message ?: "Upload failed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Error,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            null -> {}
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Username (read-only)
                        OutlinedTextField(
                            value = user.username,
                            onValueChange = {},
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = DividerColor,
                                disabledTextColor = OnSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Display Name
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryLight,
                                unfocusedBorderColor = DividerColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryLight,
                                unfocusedBorderColor = DividerColor
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Mood Selection
                        Text(
                            text = "Current Mood",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val moodRows = MoodStatus.entries.chunked(4)
                        moodRows.forEach { rowMoods ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowMoods.forEach { mood ->
                                    val isSelected = selectedMood == mood
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedMood = mood }
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) getMoodColor(mood).copy(alpha = 0.2f)
                                                else Surface
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = mood.emoji,
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = mood.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) getMoodColor(mood) else OnSurfaceVariant
                                        )
                                    }
                                }
                                repeat(4 - rowMoods.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Save Button
                        GradientButton(
                            onClick = {
                                viewModel.updateProfile(
                                    displayName = displayName,
                                    bio = bio,
                                    mood = selectedMood
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Changes")
                        }

                        // Error
                        if (updateState is Resource.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (updateState as Resource.Error).message ?: "Update failed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Error,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                else -> {
                    // Blank for loading/error - no spinner
                }
            }
        }
    }
}
