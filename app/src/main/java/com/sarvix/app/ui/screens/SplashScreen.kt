package com.sarvix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.sarvix.app.ui.theme.Background
import com.sarvix.app.ui.theme.PrimaryLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(key1 = true) {
        delay(1000) // Short 1 second splash - no animation
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onNavigateToMain()
        } else {
            onNavigateToLogin()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(PrimaryLight, androidx.compose.foundation.shape.RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Sarvix",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Clarity in Communication",
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color(0xFFB0B0B0)
            )
        }
    }
}
