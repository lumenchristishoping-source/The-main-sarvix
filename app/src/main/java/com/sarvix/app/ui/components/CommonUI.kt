package com.sarvix.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.ui.theme.*

// === Animated Gradient Brush - reusable infinite cycling gradient ===
// Cycles through: #3D1A8F -> #FF1F8E -> #00CFFF -> #FF6B2B -> #3D1A8F
@Composable
fun animatedGradientBrush(
    colors: List<Color> = listOf(GradientPurple, AccentPink, AccentCyan, AccentOrange, GradientPurple),
    durationMillis: Int = 4000
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientOffset"
    )
    return Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset.Infinite.copy(x = Float.POSITIVE_INFINITY * offset, y = Float.POSITIVE_INFINITY),
        tileMode = TileMode.Mirror
    )
}

// === Animated Gradient Border Modifier - thin border only, NO spinning wheel ===
fun Modifier.animatedGradientBorder(
    borderWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 32.dp,
    durationMillis: Int = 4000
) = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderOffset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(GradientPurple, AccentPink, AccentCyan, AccentOrange, GradientPurple),
        start = Offset(offset * 1000, 0f),
        end = Offset(offset * 1000 + 500, 500f),
        tileMode = TileMode.Mirror
    )
    this.border(borderWidth, brush, RoundedCornerShape(cornerRadius))
}

// === Gradient Button - primary action buttons with animated gradient fill ===
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btnGradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "btnOffset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(GradientPurple, AccentPink, AccentCyan, AccentOrange, GradientPurple),
        start = Offset(offset * 500, 0f),
        end = Offset(offset * 500 + 400, 200f),
        tileMode = TileMode.Mirror
    )
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = OnPrimary,
            disabledContainerColor = SurfaceVariant,
            disabledContentColor = OnSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

// === Floating Pill TopAppBar ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingPillTopAppBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    mood: MoodStatus? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animatedGradientBorder(borderWidth = 1.5.dp, cornerRadius = 32.dp)
            .shadow(8.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceTransparent)
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = AccentCyan
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Mood display
            mood?.let {
                Text(
                    text = "${it.emoji} @",
                    style = MaterialTheme.typography.labelMedium,
                    color = getMoodColor(it),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            actions()
        }
    }
}

// === Fixed Pill Header (simpler variant without mood) ===
@Composable
fun PillHeader(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animatedGradientBorder(borderWidth = 1.5.dp, cornerRadius = 32.dp)
            .shadow(8.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceTransparent)
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            actions()
        }
    }
}

// === Animated Gradient Border (wrapper for content - thin border ONLY) ===
// FIXED: No spinning gradient wheel inside - just a thin animated border
@Composable
fun AnimatedGradientBorder(
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderOffset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(GradientPurple, AccentPink, AccentCyan, AccentOrange, GradientPurple),
        start = Offset(offset * 800, 0f),
        end = Offset(offset * 800 + 400, 400f),
        tileMode = TileMode.Mirror
    )
    Box(
        modifier = modifier
            .border(borderWidth, brush, shape)
    ) {
        content()
    }
}

// === Mood Color Helper ===
fun getMoodColor(mood: MoodStatus): Color {
    return when (mood) {
        MoodStatus.HAPPY -> MoodHappy
        MoodStatus.EXCITED -> MoodExcited
        MoodStatus.CALM -> MoodCalm
        MoodStatus.THOUGHTFUL -> MoodThoughtful
        MoodStatus.TIRED -> MoodTired
        MoodStatus.STRESSED -> MoodStressed
        MoodStatus.INSPIRED -> MoodInspired
        MoodStatus.FOCUSED -> MoodFocused
        MoodStatus.SOCIAL -> MoodSocial
        MoodStatus.CREATIVE -> MoodCreative
        MoodStatus.REFLECTIVE -> MoodReflective
        MoodStatus.NEUTRAL -> MoodNeutral
    }
}

// === Mood Gradient Brush - returns a gradient based on mood ===
@Composable
fun moodGradientBrush(mood: MoodStatus): Brush {
    val baseColor = getMoodColor(mood)
    return Brush.linearGradient(
        colors = listOf(baseColor.copy(alpha = 0.8f), baseColor.copy(alpha = 0.4f)),
        start = Offset.Zero,
        end = Offset.Infinite
    )
}

// === Flow Row (simplified) ===
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

// === Loading Blank (replaces CircularProgressIndicator) ===
@Composable
fun LoadingBlank() {
    // Intentionally blank - no loading spinner as per requirements
    Box(modifier = Modifier.fillMaxSize())
}
