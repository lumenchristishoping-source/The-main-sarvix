package com.sarvix.app.ui.theme

import androidx.compose.ui.graphics.Color

// === Core Backgrounds ===
val Background = Color(0xFF0A0A0A)
val Surface = Color(0xFF111111)
val SurfaceVariant = Color(0xFF1A1A1A)
val SurfaceTransparent = Color(0xCC111111)

// === Primary Palette ===
val Primary = Color(0xFF3D1A8F)
val PrimaryLight = Color(0xFF6B35FF)
val OnPrimary = Color(0xFFFFFFFF)

// === Accent Colors ===
val AccentCyan = Color(0xFF00CFFF)
val AccentPink = Color(0xFFFF1F8E)
val AccentOrange = Color(0xFFFF6B2B)

// === Text Colors ===
val OnSurface = Color(0xFFFFFFFF)
val OnSurfaceVariant = Color(0xFFB0B0B0)
val OnBackground = Color(0xFFFFFFFF)

// === Status Colors ===
val Error = Color(0xFFCF6679)
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFA726)

// === Chat Colors ===
val ChatBubbleSent = Color(0xFF3D1A8F)
val ChatBubbleReceived = Color(0xFF1A1A1A)
val ChatTextSent = Color(0xFFFFFFFF)
val ChatTextReceived = Color(0xFFFFFFFF)

// === Intent Tag Colors ===
val IntentJoke = Color(0xFFFFD700)
val IntentSerious = Color(0xFF708090)
val IntentAdvice = Color(0xFF32CD32)
val IntentVent = Color(0xFFFF6347)
val IntentRant = Color(0xFFFF4500)

// === Mood Colors ===
val MoodNeutral = Color(0xFFB0B0B0)
val MoodHappy = Color(0xFFFFD700)
val MoodExcited = Color(0xFFFF6B2B)
val MoodCalm = Color(0xFF00CFFF)
val MoodThoughtful = Color(0xFF6B35FF)
val MoodTired = Color(0xFF708090)
val MoodStressed = Color(0xFFFF4500)
val MoodInspired = Color(0xFFFF1F8E)
val MoodFocused = Color(0xFF3D1A8F)
val MoodSocial = Color(0xFF32CD32)
val MoodCreative = Color(0xFFFFA726)
val MoodReflective = Color(0xFF4A0E4E)

// === Gradient Colors ===
val GradientPurple = Color(0xFF3D1A8F)
val GradientPink = Color(0xFFFF1F8E)
val GradientCyan = Color(0xFF00CFFF)
val GradientOrange = Color(0xFFFF6B2B)

// === Divider & Borders ===
val DividerColor = Color(0xFF2A2A2A)

// === Active Nav Item ===
val NavActiveBg = Color(0xFF3D1A8F).copy(alpha = 0.15f)

// === Legacy aliases for backward compatibility during migration ===
// DO NOT use these in new code - use the colors above
@Deprecated("Use OnSurfaceVariant instead", ReplaceWith("OnSurfaceVariant"))
val TextSecondary = OnSurfaceVariant
