package com.sarvix.app.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.data.model.Post
import com.sarvix.app.data.model.PostScope
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.ui.navigation.Screen
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
    var selectedTab by remember { mutableStateOf(PostScope.INTERNATIONAL) }

    LaunchedEffect(key1 = selectedTab) {
        viewModel.loadPosts(selectedTab)
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
            PostScope.entries.forEach { scope ->
                val selected = selectedTab == scope
                TextButton(
                    onClick = { selectedTab = scope },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selected) Primary.copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color.Transparent,
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
        when (val state = postsState) {
            is Resource.Loading -> { /* Blank */ }
            is Resource.Success -> {
                val posts = state.data ?: emptyList()
                if (posts.isEmpty()) {
                    EmptyReadsView(scope = selectedTab, navController = navController)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(posts) { post ->
                            PostCard(
                                post = post,
                                onAuthorClick = {
                                    navController.navigate(Screen.UserProfile.createRoute(post.userId))
                                },
                                onTranslate = { viewModel.translatePost(post.id) }
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
                            text = state.message ?: "Failed to load posts",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.loadPosts(selectedTab) }) {
                            Text("Retry", color = AccentCyan)
                        }
                    }
                }
            }
            null -> {}
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onAuthorClick: () -> Unit,
    onTranslate: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    val dateStr = post.createdAt?.let { dateFormat.format(it) } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAuthorClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.userProfilePicture.isNotEmpty()) {
                    AsyncImage(
                        model = post.userProfilePicture,
                        contentDescription = "Author",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Author",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariant)
                            .padding(8.dp),
                        tint = OnSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.username,
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurface
                        )
                        post.mood?.let { mood ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${mood.emoji} @",
                                style = MaterialTheme.typography.labelSmall,
                                color = getMoodColor(mood)
                            )
                        }
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
                // Scope badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = post.scope.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface
            )

            // Media (if video)
            post.videoUrl?.let { url ->
                if (url.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    VideoThumbnail(
                        videoUrl = url,
                        onClick = { /* Open video player */ }
                    )
                }
            }

            // Tags
            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariant
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Translation
            if (post.translatedContent.isNotEmpty() && post.translatedContent != post.content) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Translated: ${post.translatedContent}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoThumbnail(
    videoUrl: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Video",
                tint = PrimaryLight,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to play",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyReadsView(
    scope: PostScope,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No ${scope.displayName} Posts Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Be the first to share something!",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        com.sarvix.app.ui.components.GradientButton(
            onClick = { navController.navigate(Screen.NewPost.route) }
        ) {
            Text("Create Post")
        }
    }
}
