package com.sarvix.app.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.ui.theme.*

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
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

@Composable
fun PillHeader(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
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
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            actions()
        }
    }
}

@Composable
fun AnimatedGradientBorder(
    modifier: Modifier = Modifier,
    borderWidth: Dp = 2.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    rightEdgeOnly: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    val color1 by infiniteTransition.animateColor(
        initialValue = NeonCyan,
        targetValue = NeonPurple,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color1"
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = NeonPurple,
        targetValue = NeonPink,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color2"
    )

    val color3 by infiniteTransition.animateColor(
        initialValue = NeonPink,
        targetValue = WarmOrange,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color3"
    )

    val brush = Brush.linearGradient(
        colors = listOf(color1, color2, color3, color1)
    )

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    if (rightEdgeOnly) {
                        drawContent()
                        drawRect(
                            brush = brush,
                            topLeft = Offset(size.width - borderWidth.toPx(), 0f),
                            size = Size(borderWidth.toPx(), size.height)
                        )
                    } else {
                        // Full border logic
                        drawContent()
                        // Top
                        drawRect(brush, topLeft = Offset.Zero, size = Size(size.width, borderWidth.toPx()))
                        // Bottom
                        drawRect(brush, topLeft = Offset(0f, size.height - borderWidth.toPx()), size = Size(size.width, borderWidth.toPx()))
                        // Left
                        drawRect(brush, topLeft = Offset.Zero, size = Size(borderWidth.toPx(), size.height))
                        // Right
                        drawRect(brush, topLeft = Offset(size.width - borderWidth.toPx(), 0f), size = Size(borderWidth.toPx(), size.height))
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btnGradient")

    val color1 by infiniteTransition.animateColor(
        initialValue = NeonPurple,
        targetValue = NeonCyan,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c1"
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = NeonPink,
        targetValue = NeonPurple,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c2"
    )

    val color3 by infiniteTransition.animateColor(
        initialValue = WarmOrange,
        targetValue = NeonPink,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c3"
    )

    val brush = Brush.linearGradient(
        colors = listOf(color1, color2, color3, color1)
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .then(
                if (enabled) Modifier.background(brush)
                else Modifier.background(Color.Gray.copy(alpha = 0.5f))
            )
            .clickable(enabled = enabled && !loading) { onClick() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (loading) "" else text,
            style = MaterialTheme.typography.titleMedium,
            color = OnPrimary
        )
    }
}

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
