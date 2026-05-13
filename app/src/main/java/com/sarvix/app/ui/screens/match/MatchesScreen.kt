package com.sarvix.app.ui.screens.match

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.sarvix.app.data.model.MatchSuggestion
import com.sarvix.app.data.model.MatchType
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.ChatViewModel
import com.sarvix.app.viewmodel.MatchViewModel

@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val suggestionsState by viewModel.suggestionsState.collectAsState()
    val matchesState by viewModel.matchesState.collectAsState()
    val selectedMatchType by viewModel.selectedMatchType.collectAsState()
    val createChatState by chatViewModel.createChatState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Discover, 1 = Matches
    
    LaunchedEffect(key1 = selectedMatchType) {
        viewModel.loadSuggestions()
    }
    
    LaunchedEffect(key1 = selectedTab) {
        if (selectedTab == 1) {
            viewModel.loadMatches()
        }
    }

    LaunchedEffect(createChatState) {
        val state = createChatState
        if (state is Resource.Success) {
            val chatId = state.data
            if (!chatId.isNullOrEmpty()) {
                navController.navigate(Screen.ChatDetail.createRoute(chatId))
                chatViewModel.clearStates()
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Discover") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("My Matches") }
            )
        }
        
        // Match Type Filter (only for Discover tab)
        if (selectedTab == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedMatchType == MatchType.LOCAL,
                    onClick = { viewModel.setMatchType(MatchType.LOCAL) },
                    label = { Text("Local") },
                    leadingIcon = if (selectedMatchType == MatchType.LOCAL) {
                        { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                
                FilterChip(
                    selected = selectedMatchType == MatchType.GLOBAL,
                    onClick = { viewModel.setMatchType(MatchType.GLOBAL) },
                    label = { Text("Global") },
                    leadingIcon = if (selectedMatchType == MatchType.GLOBAL) {
                        { Icon(Icons.Default.Public, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
        
        // Content
        when (selectedTab) {
            0 -> DiscoverContent(
                suggestionsState = suggestionsState,
                onAccept = { viewModel.acceptMatch(it) },
                onDecline = { viewModel.declineMatch(it) },
                onStartChat = { userId ->
                    chatViewModel.createChat(userId)
                    // Navigate to chat will happen via state observation
                },
                navController = navController
            )
            1 -> MyMatchesContent(
                matchesState = matchesState,
                onStartChat = { userId ->
                    chatViewModel.createChat(userId)
                },
                navController = navController
            )
        }
    }
}

@Composable
fun DiscoverContent(
    suggestionsState: Resource<List<MatchSuggestion>>?,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onStartChat: (String) -> Unit,
    navController: NavController
) {
    when (val state = suggestionsState) {
        is Resource.Loading -> {
            // Remove loading indicator, show blank state as requested
        }
        is Resource.Success -> {
            val suggestions = state.data ?: emptyList()
            if (suggestions.isEmpty()) {
                EmptyDiscoverView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(suggestions) { suggestion ->
                        MatchSuggestionCard(
                            suggestion = suggestion,
                            onAccept = { onAccept(suggestion.userId) },
                            onDecline = { onDecline(suggestion.userId) },
                            onStartChat = { onStartChat(suggestion.userId) },
                            onViewProfile = {
                                navController.navigate(Screen.UserProfile.createRoute(suggestion.userId))
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
                Text(
                    text = state.message ?: "Failed to load suggestions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        null -> {}
    }
}

@Composable
fun MyMatchesContent(
    matchesState: Resource<List<MatchSuggestion>>?,
    onStartChat: (String) -> Unit,
    navController: NavController
) {
    when (val state = matchesState) {
        is Resource.Loading -> {
            // Remove loading indicator, show blank state as requested
        }
        is Resource.Success -> {
            val matches = state.data ?: emptyList()
            if (matches.isEmpty()) {
                EmptyMatchesView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(matches) { match ->
                        MatchListItem(
                            match = match,
                            onStartChat = { onStartChat(match.userId) },
                            onViewProfile = {
                                navController.navigate(Screen.UserProfile.createRoute(match.userId))
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
                Text(
                    text = state.message ?: "Failed to load matches",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        null -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchSuggestionCard(
    suggestion: MatchSuggestion,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onStartChat: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with profile picture and match percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onViewProfile),
                    color = SurfaceVariant
                ) {
                    if (suggestion.profilePictureUrl.isNotEmpty()) {
                        AsyncImage(
                            model = suggestion.profilePictureUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            tint = OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // User Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = suggestion.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (suggestion.displayName.isNotEmpty()) {
                        Text(
                            text = suggestion.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Mood
                    Text(
                        text = "${suggestion.mood.emoji} ${suggestion.mood.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    
                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (suggestion.matchType == MatchType.LOCAL) 
                                Icons.Default.LocationOn else Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${suggestion.country} • ${suggestion.language}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                
                // Match Percentage
                Surface(
                    shape = CircleShape,
                    color = when {
                        suggestion.matchPercentage >= 70 -> Success
                        suggestion.matchPercentage >= 40 -> Warning
                        else -> Error
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${suggestion.matchPercentage.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bio
            if (suggestion.bio.isNotEmpty()) {
                Text(
                    text = suggestion.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Shared Interests
            if (suggestion.sharedInterests.isNotEmpty()) {
                Text(
                    text = "Shared Interests:",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestion.sharedInterests.take(5).forEach { interest ->
                        AssistChip(
                            onClick = {},
                            label = { Text(interest) }
                        )
                    }
                    if (suggestion.sharedInterests.size > 5) {
                        Text(
                            text = "+${suggestion.sharedInterests.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pass")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                com.sarvix.app.ui.components.GradientButton(
                    text = "Connect",
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MatchListItem(
    match: MatchSuggestion,
    onStartChat: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewProfile),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = SurfaceVariant
            ) {
                if (match.profilePictureUrl.isNotEmpty()) {
                    AsyncImage(
                        model = match.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = OnSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = match.username,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${match.mood.emoji} ${match.mood.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "${match.matchPercentage.toInt()}% match",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary
                )
            }
            
            // Chat Button
            IconButton(onClick = onStartChat) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Start Chat",
                    tint = Primary
                )
            }
        }
    }
}

@Composable
fun EmptyDiscoverView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No suggestions right now",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Check back later for new people to connect with",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyMatchesView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No matches yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start connecting with people in the Discover tab",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
