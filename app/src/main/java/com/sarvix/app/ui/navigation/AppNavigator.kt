package com.sarvix.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sarvix.app.ui.screens.SplashScreen
import com.sarvix.app.ui.screens.auth.ForgotPasswordScreen
import com.sarvix.app.ui.screens.auth.LoginScreen
import com.sarvix.app.ui.screens.auth.SignupScreen
import com.sarvix.app.ui.screens.chat.ChatDetailScreen
import com.sarvix.app.ui.screens.main.MainScreen
import com.sarvix.app.ui.screens.match.MatchDetailScreen
import com.sarvix.app.ui.screens.post.NewPostScreen
import com.sarvix.app.ui.screens.profile.EditProfileScreen
import com.sarvix.app.ui.screens.profile.ProfileSetupScreen
import com.sarvix.app.ui.screens.settings.*
import com.sarvix.app.ui.screens.user.UserProfileScreen
import com.sarvix.app.ui.theme.Background
import com.sarvix.app.ui.theme.SarvixTheme
import com.sarvix.app.viewmodel.AuthViewModel

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    SarvixTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Background
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
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
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToProfileSetup = {
                            navController.navigate(Screen.ProfileSetup.route)
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
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        viewModel = authViewModel,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }

                composable(Screen.ProfileSetup.route) {
                    ProfileSetupScreen(navController = navController)
                }

                composable(Screen.Main.route) {
                    MainScreen(navController = navController)
                }

                composable(
                    route = Screen.ChatDetail.route,
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                    ChatDetailScreen(
                        chatId = chatId,
                        navController = navController
                    )
                }

                composable(
                    route = Screen.MatchDetail.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    MatchDetailScreen(
                        userId = userId,
                        navController = navController
                    )
                }

                composable(
                    route = Screen.UserProfile.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    UserProfileScreen(
                        userId = userId,
                        navController = navController
                    )
                }

                composable(Screen.SarvixReads.route) {
                    MainScreen(navController = navController)
                }

                composable(Screen.NewPost.route) {
                    NewPostScreen(navController = navController)
                }

                composable(Screen.Profile.route) {
                    MainScreen(navController = navController)
                }

                composable(Screen.EditProfile.route) {
                    EditProfileScreen(navController = navController)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(navController = navController)
                }

                composable(Screen.ChangePassword.route) {
                    ChangePasswordScreen(navController = navController)
                }

                composable(Screen.EmailPreferences.route) {
                    EmailPreferencesScreen(navController = navController)
                }

                composable(Screen.BlockedUsers.route) {
                    BlockedUsersScreen(navController = navController)
                }

                composable(Screen.DataPrivacy.route) {
                    DataPrivacyScreen(navController = navController)
                }

                composable(Screen.TermsOfService.route) {
                    TermsOfServiceScreen(navController = navController)
                }

                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(navController = navController)
                }
            }
        }
    }
}
