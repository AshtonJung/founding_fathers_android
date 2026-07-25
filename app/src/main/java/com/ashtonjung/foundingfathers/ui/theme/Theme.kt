package com.ashtonjung.foundingfathers.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    val PrimaryBlue = Color(0xFF2563EB)
    val AccentRed = Color(0xFFDB2626)
    val AccentGold = Color(0xFFFAC024)
    val Ink = Color(0xFF12172A)
    val SecondaryInk = Color(0xFF383F54)
    val CanvasBG = Color(0xFF12142B)

    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.75f)

    val CardFill = Color.Black.copy(alpha = 0.35f)
    val CardStroke = Color.White.copy(alpha = 0.22f)
    val CardShadow = Color.Black.copy(alpha = 0.5f)

    val BgGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF12142B),
            Color(0xFF1C1A3D),
            Color(0xFF2E214D)
        )
    )
    val FlagGradient = Brush.linearGradient(colors = listOf(AccentRed, PrimaryBlue))
}

private val DarkColors = darkColorScheme(
    primary = AppColors.PrimaryBlue,
    secondary = AppColors.AccentGold,
    background = AppColors.CanvasBG,
    surface = AppColors.CanvasBG,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun FoundingFathersTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}

fun Modifier.gradientBackground(): Modifier = this.background(AppColors.BgGradient)

fun Modifier.themedCard(): Modifier = this
    .shadow(12.dp, RoundedCornerShape(18.dp), clip = false, ambientColor = AppColors.CardShadow, spotColor = AppColors.CardShadow)
    .clip(RoundedCornerShape(18.dp))
    .background(AppColors.CardFill)
    .border(1.dp, AppColors.CardStroke, RoundedCornerShape(18.dp))
    .padding(16.dp)
