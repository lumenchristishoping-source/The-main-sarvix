package com.sarvix.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sarvix.app.ui.screens.*
import com.sarvix.app.ui.screens.chat.ChatDetailScreen
import com.sarvix.app.ui.screens.chat.ChatsScreen
import com.sarvix.app.ui.screens.main.MainScreen
import com.sarvix.app.ui.screens.match.MatchesScreen
import com.sarvix.app.ui.screens.post.NewPostScreen
import com.sarvix.app.ui.screens.post.SarvixReadsScreen
import com.sarvix.app.ui.screens.profile.EditProfileScreen
import com.sarvix.app.ui.screens.profile.ProfileScreen
import com.sarvix.app.ui.screens.profile.ProfileSetupScreen
import com.sarvix.app.viewmodel.AuthViewModel
import com.sarvix.app.viewmodel.ProfileViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    
    val loginState by authViewModel.loginState.collectAsState()
    val signupState by authViewModel.signupState.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigateUp()
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                viewModel = profileViewModel,
                onSetupComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main Screen with Navigation Drawer
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController
            )
        }
        
        // Chat Detail
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatDetailScreen(
                chatId = chatId,
                navController = navController
            )
        }
        
        // New Post
        composable(Screen.NewPost.route) {
            NewPostScreen(
                navController = navController
            )
        }
        
        // Edit Profile
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = profileViewModel,
                navController = navController
            )
        }
        
        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // User Profile (other users)
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(
                userId = userId,
                navController = navController
            )
        }
    }
}