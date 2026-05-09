package com.sarvix.app.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.components.PillHeader
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.ui.components.moodGradientBrush
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.MatchViewModel

@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    LaunchedEffect(key1 = userId) {
        viewModel.loadUser(userId)
    }

    LaunchedEffect(connectionState) {
        if (connectionState is Resource.Success) {
            val chatId = (connectionState as Resource.Success).data
            chatId?.let {
                navController.navigate(Screen.ChatDetail.createRoute(it)) {
                    popUpTo(navController.graph.startDestinationId)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PillHeader(
            title = "Profile",
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

        when (val state = userState) {
            is Resource.Loading -> { /* Blank */ }
            is Resource.Success -> {
                val user = state.data
                if (user != null) {
                    UserProfileContent(user, navController, viewModel)
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "User not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Error
                    )
                }
            }
            null -> {}
        }
    }
}

@Composable
private fun UserProfileContent(
    user: User,
    navController: NavController,
    viewModel: MatchViewModel
) {
    val connectionState by viewModel.connectionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture with mood gradient border
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = moodGradientBrush(user.mood ?: MoodStatus.NEUTRAL),
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
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceVariant)
                        .padding(24.dp),
                    tint = OnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username with mood
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = user.mood?.emoji ?: "", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
        }

        if (user.displayName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariant
            )
        }

        // Mood badge
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = getMoodColor(user.mood ?: MoodStatus.NEUTRAL).copy(alpha = 0.15f)
        ) {
            Text(
                text = "${user.mood?.emoji ?: ""} ${user.mood?.displayName ?: "Neutral"}",
                style = MaterialTheme.typography.labelMedium,
                color = getMoodColor(user.mood ?: MoodStatus.NEUTRAL),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (user.bio.isNotEmpty()) {
                    Text("Bio", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user.bio, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = DividerColor)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoItem("Country", user.country.ifEmpty { "Not set" })
                    InfoItem("Language", user.language.ifEmpty { "Not set" })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = DividerColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Interests", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                com.sarvix.app.ui.components.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    user.interests.forEach { interest ->
                        Surface(shape = RoundedCornerShape(16.dp), color = Primary.copy(alpha = 0.2f)) {
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

        // Connect Button
        GradientButton(
            onClick = { viewModel.connectWithUser(user.id) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Connect")
        }

        if (connectionState is Resource.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = connectionState.message ?: "Connection failed",
                style = MaterialTheme.typography.bodyMedium,
                color = Error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
    }
}
