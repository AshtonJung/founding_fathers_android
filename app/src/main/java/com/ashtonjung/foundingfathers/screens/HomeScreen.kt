package com.ashtonjung.foundingfathers.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashtonjung.foundingfathers.audio.BgmGroup
import com.ashtonjung.foundingfathers.audio.Sfx
import com.ashtonjung.foundingfathers.audio.SoundManager
import com.ashtonjung.foundingfathers.data.tierFor
import com.ashtonjung.foundingfathers.state.AppViewModel
import com.ashtonjung.foundingfathers.ui.theme.AppColors
import com.ashtonjung.foundingfathers.ui.theme.FounderCard
import com.ashtonjung.foundingfathers.ui.theme.GradientProgressBar
import com.ashtonjung.foundingfathers.ui.theme.SectionTitle
import com.ashtonjung.foundingfathers.ui.theme.ShinyButton
import com.ashtonjung.foundingfathers.ui.theme.gradientBackground
import com.ashtonjung.foundingfathers.ui.theme.themedCard

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    soundManager: SoundManager,
    onOpenFounder: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDaily by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showSpin by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val ctaEntrance = androidx.compose.runtime.remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.ensureDailyFresh()
        soundManager.playBgm(BgmGroup.HOME)
        ctaEntrance.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    if (showDaily) {
        DailyChallengeScreen(
            viewModel = viewModel,
            soundManager = soundManager,
            onClose = { showDaily = false }
        )
        return
    }

    if (showSpin) {
        SpinWheelScreen(
            viewModel = viewModel,
            soundManager = soundManager,
            onClose = { showSpin = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header metrics
        Row(
            modifier = Modifier.fillMaxWidth().themedCard(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricPill(icon = Icons.Filled.Star, label = "XP", value = "${state.xp}", tint = AppColors.AccentGold)
            Spacer(modifier = Modifier.width(8.dp))
            MetricPill(icon = Icons.Filled.Refresh, label = "Streak", value = "${state.streak}d", tint = AppColors.PrimaryBlue)
            Spacer(modifier = Modifier.width(8.dp))
            MetricPill(icon = Icons.Filled.MonetizationOn, label = "Coins", value = "${state.coins}", tint = AppColors.AccentGold)
            Spacer(modifier = Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = onOpenHistory) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Review", color = Color.White)
            }
        }

        // Start CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .scale(0.98f + 0.02f * ctaEntrance.value)
                .alpha(ctaEntrance.value)
                .themedCard()
        ) {
            Text("Ready to play?", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(10.dp))
            ShinyButton(text = "Start Quiz", onClick = {
                viewModel.markPlayed()
                soundManager.play(Sfx.TAP)
                onStartQuiz()
            })
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Answer questions to earn XP and keep your streak alive.",
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
        }

        // Daily challenge card
        val isDailyDone = state.dailyDone && state.dailyCorrect >= state.dailyGoal
        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isDailyDone) Icons.Filled.CheckCircle else Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = if (isDailyDone) Color(0xFF4CAF50) else AppColors.AccentGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Challenge", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (isDailyDone) "Complete" else "Due Today",
                    color = if (isDailyDone) Color(0xFF4CAF50) else AppColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Get ${state.dailyGoal} correct answers today",
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            GradientProgressBar(progress = state.dailyCorrect.toFloat() / maxOf(1, state.dailyGoal).toFloat())
            Spacer(modifier = Modifier.height(12.dp))
            ShinyButton(
                text = if (isDailyDone) "Come back tomorrow" else "Do now",
                enabled = !isDailyDone,
                onClick = {
                    if (!isDailyDone) {
                        viewModel.ensureDailyFresh()
                        showDaily = true
                    }
                }
            )
        }

        // Daily spin wheel card
        val canSpinToday = viewModel.canSpinToday()
        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = AppColors.AccentGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Spin", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (canSpinToday) "Free Spin" else "Spun Today",
                    color = if (canSpinToday) AppColors.AccentGold else AppColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Spin the wheel once a day for bonus coins and XP.",
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShinyButton(
                text = if (canSpinToday) "Spin Now" else "Come back tomorrow",
                enabled = canSpinToday,
                onClick = { if (canSpinToday) showSpin = true }
            )
        }

        // Founders grid
        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            SectionTitle("Founders")
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(((state.fathers.size + 1) / 2 * 180).dp)
            ) {
                items(state.fathers) { father ->
                    val progress = viewModel.masteryProgress(father)
                    val wrongCount = viewModel.wrongAnswersFor(father).size
                    FounderCard(
                        father = father,
                        progress = progress,
                        tier = tierFor(progress),
                        hasWrong = wrongCount > 0,
                        onClick = { onOpenFounder(father.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint)
        Column {
            Text(label, color = AppColors.TextSecondary, fontSize = 11.sp)
            Text(value, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
