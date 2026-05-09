package com.sarvix.app.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sarvix.app.data.model.PostScope
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.components.PillHeader
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.PostViewModel
import com.sarvix.app.viewmodel.ProfileViewModel

@Composable
fun NewPostScreen(
    navController: NavController,
    viewModel: PostViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var content by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf(PostScope.INTERNATIONAL) }
    val createState by viewModel.createState.collectAsState()

    // FIXED: Fetch current user before creating post
    LaunchedEffect(key1 = true) {
        profileViewModel.loadUserProfile()
    }

    // Redirect to profile setup if user doc missing
    LaunchedEffect(createState) {
        if (createState is Resource.Error) {
            val msg = (createState as Resource.Error).message ?: ""
            if (msg.contains("User not found") || msg.contains("profile setup")) {
                navController.navigate("profile_setup") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        if (createState is Resource.Success) {
            navController.navigateUp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PillHeader(
            title = "New Post",
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
            // FIXED: Scope selector
            Text("Post Visibility", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                PostScope.entries.forEach { scope ->
                    val selected = selectedScope == scope
                    TextButton(
                        onClick = { selectedScope = scope },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Content field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Share your thoughts...", color = OnSurfaceVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                maxLines = 20,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = DividerColor
                )
            )

            Text(
                text = "${content.length}/2000",
                style = MaterialTheme.typography.labelSmall,
                color = if (content.length > 2000) Error else OnSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tags placeholder
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Add tags (optional)", color = OnSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = DividerColor,
                    disabledPlaceholderColor = OnSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Video placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { /* Video picker */ }
                    .background(SurfaceVariant),
                color = SurfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.displaySmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "Add Video (max 30s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FIXED: Gradient Post Button
            GradientButton(
                onClick = { viewModel.createPost(content, selectedScope) },
                modifier = Modifier.fillMaxWidth(),
                enabled = content.isNotBlank() && content.length <= 2000
            ) {
                Text("Post")
            }

            // Error
            if (createState is Resource.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (createState as Resource.Error).message ?: "Failed to create post",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
