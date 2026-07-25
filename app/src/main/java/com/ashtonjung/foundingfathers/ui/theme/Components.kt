package com.ashtonjung.foundingfathers.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashtonjung.foundingfathers.data.Tier
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ShinyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) AppColors.PrimaryBlue else AppColors.PrimaryBlue.copy(alpha = 0.5f))
            .border(1.dp, AppColors.CardStroke, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun SecondaryOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AppColors.PrimaryBlue.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun GradientProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "progressBar"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.18f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(AppColors.PrimaryBlue)
        )
    }
}

@Composable
fun ChoiceRow(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    eliminated: Boolean = false
) {
    val targetBg = when {
        isCorrect == true -> AppColors.AccentGold
        isCorrect == false -> AppColors.AccentRed
        isSelected -> AppColors.PrimaryBlue
        else -> AppColors.CardFill
    }
    val bg by animateColorAsState(targetValue = targetBg, animationSpec = tween(220), label = "choiceBg")
    val rowAlpha by animateFloatAsState(targetValue = if (eliminated) 0.3f else 1f, animationSpec = tween(300), label = "eliminatedAlpha")
    val icon = when {
        isCorrect == true -> Icons.Filled.Check
        isCorrect == false -> Icons.Filled.Check
        isSelected -> Icons.Filled.Circle
        else -> Icons.Filled.Circle
    }

    // Little "pop" whenever this row becomes the correct/selected answer -
    // makes right/wrong feedback feel more alive than an instant color swap.
    val scale = remember { Animatable(1f) }
    LaunchedEffect(isCorrect, isSelected) {
        if (isCorrect != null || isSelected) {
            scale.animateTo(1.05f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale.value)
            .alpha(rowAlpha)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(2.dp, if (isCorrect == null && !isSelected) AppColors.CardStroke else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(
                enabled = !eliminated,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = if (isSelected || isCorrect != null) 1f else 0.6f))
        Text(
            text,
            color = Color.White,
            fontWeight = if (isSelected || isCorrect == true) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier
        )
    }
}

@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "progressRing"
    )
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 6.dp.toPx()
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        drawArc(
            color = AppColors.PrimaryBlue,
            startAngle = -90f,
            sweepAngle = 360f * animated,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun TierRibbon(tier: Tier, modifier: Modifier = Modifier) {
    if (tier == Tier.NONE) return
    val (label, color) = when (tier) {
        Tier.GOLD -> "GOLD" to AppColors.AccentGold
        Tier.SILVER -> "SILVER" to Color.Gray
        Tier.BRONZE -> "BRONZE" to Color(0xFFC77A47)
        Tier.NONE -> "" to Color.Transparent
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.22f))
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MedalView(tier: Tier, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val gradient = when (tier) {
        Tier.GOLD -> Brush.linearGradient(listOf(AppColors.AccentGold, Color(0xFFD9C173)))
        Tier.SILVER -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.92f), Color.Gray.copy(alpha = 0.55f)))
        else -> Brush.linearGradient(listOf(Color(0xFFD18449), Color(0xFF945C33)))
    }

    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    // gentle continuous shimmer/rotation on the star so the result screen feels alive, not static
    val shimmer = rememberInfiniteTransition(label = "medalShimmer")
    val rotation by shimmer.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "medalRotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(entrance.value)
            .clip(CircleShape)
            .background(gradient)
            .border(3.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = "Medal",
            tint = Color.White,
            modifier = Modifier
                .size(size * 0.36f)
                .graphicsLayer { rotationZ = rotation }
        )
    }
}

@Composable
fun PulsingDot(modifier: Modifier = Modifier, color: Color = AppColors.AccentGold) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    Box(modifier = modifier.size(10.dp).scale(scale).clip(CircleShape).background(color))
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = AppColors.TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = modifier
    )
}

private val confettiColors = listOf(
    Color(0xFFE8262B), AppColors.PrimaryBlue, AppColors.AccentGold,
    Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFEC407A)
)

/** A short-lived burst of falling confetti squares, replayed each time [trigger] changes
 * (increment a counter to fire it) - a plain Boolean toggle would skip every other play. */
@Composable
fun ConfettiBurst(trigger: Int, modifier: Modifier = Modifier, particleCount: Int = 26) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            t.snapTo(0f)
            t.animateTo(1f, animationSpec = tween(durationMillis = 1100, easing = LinearEasing))
        }
    }
    if (t.value in 0f..0.999f && t.value > 0f) {
        val seed = remember { Random(System.nanoTime()) }
        androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.22f
            for (i in 0 until particleCount) {
                val baseAngle = (i * (360f / particleCount)) + seed.nextInt(-10, 10)
                val angleRad = Math.toRadians((baseAngle + t.value * 90f).toDouble())
                val radius = size.width * 0.14f + sin(t.value * 6.283f + i) * 10f
                val x = cx + (cos(angleRad).toFloat() * radius * 2.6f)
                val y = cy + (sin(angleRad).toFloat() * radius * 1.4f) + t.value * size.height * 0.85f
                val alpha = (1f - t.value).coerceIn(0f, 1f)
                rotate(degrees = t.value * 360f + i * 12f, pivot = Offset(x, y)) {
                    drawRect(
                        color = confettiColors[i % confettiColors.size].copy(alpha = alpha),
                        topLeft = Offset(x - 5f, y - 5f),
                        size = androidx.compose.ui.geometry.Size(10f, 10f)
                    )
                }
            }
        }
    }
}
