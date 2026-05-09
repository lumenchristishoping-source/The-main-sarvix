package com.sarvix.app.ui.screens.match

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sarvix.app.data.model.Match
import com.sarvix.app.data.model.MatchScope
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.MatchViewModel

@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val matchesState by viewModel.matchesState.collectAsState()
    var selectedTab by remember { mutableStateOf(MatchScope.LOCAL) }

    LaunchedEffect(key1 = selectedTab) {
        viewModel.loadMatches(selectedTab)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
        ) {
            MatchScope.entries.forEach { scope ->
                val selected = selectedTab == scope
                val matchCount = (matchesState as? Resource.Success)?.data?.size ?: 0
                TextButton(
                    onClick = { selectedTab = scope },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selected) Primary.copy(alpha = 0.3f) else Color.Transparent,
                        contentColor = if (selected) PrimaryLight else OnSurfaceVariant
                    )
                ) {
                    Text(
                        text = scope.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Content
        when (val state = matchesState) {
            is Resource.Loading -> { /* Blank - no spinner */ }
            is Resource.Success -> {
                val matches = state.data ?: emptyList()
                if (matches.isEmpty()) {
                    EmptyMatchesView(scope = selectedTab)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(matches) { match ->
                            MatchCard(
                                match = match,
                                onClick = {
                                    navController.navigate(Screen.MatchDetail.createRoute(match.user.id))
                                }
                            )
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
                            text = state.message ?: "Failed to load matches",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GradientButton(
                            onClick = { viewModel.loadMatches(selectedTab) }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            null -> {}
        }
    }
}

@Composable
fun MatchCard(match: Match, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture with mood
            Box {
                if (match.user.profilePictureUrl.isNotEmpty()) {
                    AsyncImage(
                        model = match.user.profilePictureUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariant)
                            .padding(16.dp),
                        tint = OnSurfaceVariant
                    )
                }
                // Mood badge
                match.user.mood?.let { mood ->
                    Surface(
                        shape = CircleShape,
                        color = Surface,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = mood.emoji,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = match.user.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface
                    )
                    match.user.mood?.let { mood ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${mood.emoji} @",
                            style = MaterialTheme.typography.labelSmall,
                            color = getMoodColor(mood)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (match.user.displayName.isNotEmpty()) {
                    Text(
                        text = match.user.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // Shared interests
                if (match.sharedInterests.isNotEmpty()) {
                    Text(
                        text = match.sharedInterests.joinToString(", ").take(50),
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Score badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Primary.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "${match.score}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryLight,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyMatchesView(scope: MatchScope) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = PrimaryLight.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No ${scope.displayName} Matches Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Complete your profile to get matched with people",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
