package com.sarvix.app.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.ui.theme.*

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
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "gradient")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(3000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "angle"
    )

    val brush = androidx.compose.ui.graphics.Brush.sweepGradient(
        colors = listOf(NeonCyan, NeonPurple, NeonPink, NeonCyan),
    )

    Surface(
        modifier = modifier.padding(borderWidth),
        shape = shape,
        color = MaterialTheme.colorScheme.surface
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(borderWidth)
                .drawWithContent {
                    rotate(angle) {
                        drawCircle(
                            brush = brush,
                            radius = size.width,
                            blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn
                        )
                    }
                    drawContent()
                }
        ) {
            content()
        }
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
