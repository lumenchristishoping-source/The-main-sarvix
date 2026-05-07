package com.sarvix.app.ui.screens.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sarvix.app.data.model.Post
import com.sarvix.app.data.model.PostType
import com.sarvix.app.data.model.ReadSpace
import com.sarvix.app.data.model.TextPost
import com.sarvix.app.data.model.VideoPost
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SarvixReadsScreen(
    navController: NavController,
    viewModel: PostViewModel = hiltViewModel()
) {
    val postsState by viewModel.postsState.collectAsState()
    val currentReadSpace by viewModel.currentReadSpace.collectAsState()
    
    LaunchedEffect(key1 = currentReadSpace) {
        viewModel.loadPosts()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Read Space Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = currentReadSpace == ReadSpace.INTERNATIONAL,
                onClick = { viewModel.setReadSpace(ReadSpace.INTERNATIONAL) },
                label = { Text("International") },
                leadingIcon = if (currentReadSpace == ReadSpace.INTERNATIONAL) {
                    { Icon(Icons.Default.Public, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            
            FilterChip(
                selected = currentReadSpace == ReadSpace.LOCAL,
                onClick = { viewModel.setReadSpace(ReadSpace.LOCAL) },
                label = { Text("Local") },
                leadingIcon = if (currentReadSpace == ReadSpace.LOCAL) {
                    { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
        
        // Posts Content
        when (val state = postsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val posts = state.data ?: emptyList()
                if (posts.isEmpty()) {
                    EmptyPostsView(
                        onCreatePost = {
                            navController.navigate(com.sarvix.app.ui.navigation.Screen.NewPost.route)
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(posts) { post ->
                            when (post) {
                                is TextPost -> TextPostCard(
                                    post = post,
                                    onReport = { viewModel.reportPost(post.id, "Inappropriate content") }
                                )
                                is VideoPost -> VideoPostCard(
                                    post = post,
                                    onReport = { viewModel.reportPost(post.id, "Inappropriate content") }
                                )
                                else -> {}
                            }
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
                        text = state.message ?: "Failed to load posts",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            null -> {}
        }
    }
}

@Composable
fun TextPostCard(
    post: TextPost,
    onReport: () -> Unit
) {
    var showTranslation by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    
    val displayContent = if (showTranslation && post.translatedContent != null) {
        post.translatedContent
    } else {
        post.content
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author Avatar
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = SurfaceVariant
                ) {
                    if (post.authorProfilePicture.isNotEmpty()) {
                        AsyncImage(
                            model = post.authorProfilePicture,
                            contentDescription = "Author",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            tint = OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Author Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorUsername,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = post.timestamp?.let { 
                            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(it) 
                        } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                
                // Options Menu
                IconButton(onClick = { showOptions = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                
                DropdownMenu(
                    expanded = showOptions,
                    onDismissRequest = { showOptions = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Report") },
                        onClick = {
                            onReport()
                            showOptions = false
                        },
                        leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            Text(
                text = displayContent,
                style = MaterialTheme.typography.bodyLarge
            )
            
            // Translation indicator
            if (post.translatedContent != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showTranslation = !showTranslation },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        Icons.Default.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (showTranslation) "Show Original" else "Show Translation"
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPostCard(
    post: VideoPost,
    onReport: () -> Unit
) {
    var showTranslation by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    
    val displayCaption = if (showTranslation && post.translatedContent != null) {
        post.translatedContent
    } else {
        post.content
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Video Player Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(SurfaceVariant)
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    // Thumbnail or Play Button
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play",
                        modifier = Modifier.size(64.dp),
                        tint = Primary
                    )
                } else {
                    // In production, use ExoPlayer here
                    Text(
                        text = "Video Playing...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Duration Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = Surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${post.duration}s",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Post Info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Author Avatar
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = SurfaceVariant
                    ) {
                        if (post.authorProfilePicture.isNotEmpty()) {
                            AsyncImage(
                                model = post.authorProfilePicture,
                                contentDescription = "Author",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Author Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.authorUsername,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = post.timestamp?.let { 
                                SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(it) 
                            } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    
                    // Options Menu
                    IconButton(onClick = { showOptions = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    
                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report") },
                            onClick = {
                                onReport()
                                showOptions = false
                            },
                            leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) }
                        )
                    }
                }
                
                // Caption
                if (displayCaption.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayCaption,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Translation toggle
                if (post.translatedContent != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { showTranslation = !showTranslation },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            Icons.Default.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (showTranslation) "Show Original" else "Show Translation",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPostsView(
    onCreatePost: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No posts yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Be the first to share something with the community",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onCreatePost) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Post")
        }
    }
}