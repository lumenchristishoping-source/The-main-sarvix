package com.sarvix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.ui.theme.Primary
import kotlinx.coroutines.delay
import javax.inject.Inject

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToProfileSetup: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    
    LaunchedEffect(key1 = true) {
        delay(1500) // Show splash for 1.5 seconds
        
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check if profile is complete
            // For now, navigate to main (profile check will happen there)
            onNavigateToMain()
        } else {
            onNavigateToLogin()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sarvix",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Height(8.dp))
            Text(
                text = "Clarity in Communication",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Height(32.dp))
            CircularProgressIndicator(
                color = Primary
            )
        }
    }
}