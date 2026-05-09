package com.sarvix.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")
    data object ProfileSetup : Screen("profile_setup")
    data object Main : Screen("main")
    data object Chats : Screen("chats")
    data object Matches : Screen("matches")
    data object MatchDetail : Screen("match_detail/{userId}") {
        fun createRoute(userId: String) = "match_detail/$userId"
    }
    data object SarvixReads : Screen("sarvix_reads")
    data object NewPost : Screen("new_post")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    data object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    data object Settings : Screen("settings")
    data object ChangePassword : Screen("change_password")
    data object EmailPreferences : Screen("email_preferences")
    data object BlockedUsers : Screen("blocked_users")
    data object DataPrivacy : Screen("data_privacy")
    data object TermsOfService : Screen("terms_of_service")
    data object PrivacyPolicy : Screen("privacy_policy")
}
