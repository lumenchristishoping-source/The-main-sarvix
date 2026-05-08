package com.sarvix.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object ProfileSetup : Screen("profile_setup")
    
    object Main : Screen("main")
    object Home : Screen("home")
    object Chats : Screen("chats")
    object ChatDetail : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    object Matches : Screen("matches")
    object SarvixReads : Screen("sarvix_reads")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Privacy : Screen("privacy")
    object BlockedUsers : Screen("blocked_users")
    
    object NewPost : Screen("new_post")
    object UserProfile : Screen("user/{userId}") {
        fun createRoute(userId: String) = "user/$userId"
    }
}