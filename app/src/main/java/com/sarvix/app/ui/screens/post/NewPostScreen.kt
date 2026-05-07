package com.sarvix.app.ui.screens.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sarvix.app.data.model.Post
import com.sarvix.app.data.model.PostType
import com.sarvix.app.data.model.ReadSpace
import com.sarvix.app.ui.theme.Primary
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostScreen(
    navController: NavController,
    viewModel: PostViewModel = hiltViewModel()
) {
    val createPostState by viewModel.createPostState.collectAsState()
    
    var postType by remember { mutableStateOf(PostType.TEXT) }
    var content by remember { mutableStateOf("") }
    var selectedReadSpace by remember { mutableStateOf(ReadSpace.INTERNATIONAL) }
    var showTypeSelector by remember { mutableStateOf(true) }
    
    // Handle post creation success
    LaunchedEffect(createPostState) {
        if (createPostState is Resource.Success) {
            navController.navigateUp()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Post") },
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
                            if (content.isNotBlank()) {
                                viewModel.createTextPost(content, selectedReadSpace)
                            }
                        },
                        enabled = content.isNotBlank() && createPostState !is Resource.Loading
                    ) {
                        if (createPostState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Post Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = postType == PostType.TEXT,
                    onClick = { postType = PostType.TEXT },
                    label = { Text("Text") },
                    leadingIcon = {
                        Icon(Icons.Default.TextFields, null, modifier = Modifier.size(18.dp))
                    }
                )
                
                FilterChip(
                    selected = postType == PostType.VIDEO,
                    onClick = { postType = PostType.VIDEO },
                    label = { Text("Video") },
                    leadingIcon = {
                        Icon(Icons.Default.Videocam, null, modifier = Modifier.size(18.dp))
                    },
                    enabled = false // MVP: Video posts disabled
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Read Space Selector
            Text(
                text = "Post to:",
                style = MaterialTheme.typography.labelMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedReadSpace == ReadSpace.INTERNATIONAL,
                    onClick = { selectedReadSpace = ReadSpace.INTERNATIONAL },
                    label = { Text("International") }
                )
                
                FilterChip(
                    selected = selectedReadSpace == ReadSpace.LOCAL,
                    onClick = { selectedReadSpace = ReadSpace.LOCAL },
                    label = { Text("Local") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content Input
            when (postType) {
                PostType.TEXT -> {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("What's on your mind?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        maxLines = Int.MAX_VALUE
                    )
                }
                PostType.VIDEO -> {
                    // Video upload UI (disabled for MVP)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Primary.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Video posts coming soon",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "For now, share your thoughts as text posts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Error Message
            if (createPostState is Resource.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createPostState as Resource.Error).message ?: "Failed to create post",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}