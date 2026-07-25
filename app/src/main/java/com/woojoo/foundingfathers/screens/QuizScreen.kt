package com.woojoo.foundingfathers.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woojoo.foundingfathers.audio.BgmGroup
import com.woojoo.foundingfathers.audio.Haptics
import com.woojoo.foundingfathers.audio.Sfx
import com.woojoo.foundingfathers.audio.SoundManager
import com.woojoo.foundingfathers.data.QuizQuestion
import com.woojoo.foundingfathers.data.Tier
import com.woojoo.foundingfathers.state.AppViewModel
import com.woojoo.foundingfathers.ui.theme.AppColors
import com.woojoo.foundingfathers.ui.theme.ChoiceRow
import com.woojoo.foundingfathers.ui.theme.ConfettiBurst
import com.woojoo.foundingfathers.ui.theme.GradientProgressBar
import com.woojoo.foundingfathers.ui.theme.MedalView
import com.woojoo.foundingfathers.ui.theme.ShinyButton
import com.woojoo.foundingfathers.ui.theme.gradientBackground
import com.woojoo.foundingfathers.ui.theme.themedCard

private const val FIFTY_FIFTY_COST = 15
private const val SKIP_COST = 10

@Composable
fun QuizScreen(viewModel: AppViewModel, soundManager: SoundManager) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var eliminated by remember { mutableStateOf<Set<Int>>(emptySet()) }

    fun restart() {
        questions = viewModel.beginQuizSession(10)
        index = 0
        score = 0
        selected = null
        isAnswered = false
        eliminated = emptySet()
    }

    LaunchedEffect(Unit) {
        restart()
        soundManager.playBgm(BgmGroup.QUIZ)
    }

    val isCompleted = index >= questions.size && questions.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .gradientBackground()
                .padding(16.dp)
        ) {
            if (isCompleted) {
                LaunchedEffect(score) { viewModel.setBestQuizScore(score) }
                val ratio = if (questions.isNotEmpty()) score.toFloat() / questions.size else 0f
                val tier = when {
                    ratio >= 0.9f -> Tier.GOLD
                    ratio >= 0.75f -> Tier.SILVER
                    ratio >= 0.6f -> Tier.BRONZE
                    else -> Tier.NONE
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (tier != Tier.NONE) {
                        MedalView(tier = tier, size = 100.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("Result", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$score out of ${questions.size} correct", color = AppColors.TextPrimary, fontSize = 17.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Best ${state.bestQuizScore}", color = AppColors.TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    ShinyButton(text = "Retry", onClick = { restart() })
                }
            } else if (questions.isEmpty()) {
                Text("Loading questions...", color = AppColors.TextSecondary)
            } else {
                val q = questions[index]
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Score $score", color = AppColors.TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = AppColors.AccentGold, modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${state.coins}", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Text("${index + 1}/${questions.size}", color = AppColors.TextSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                GradientProgressBar(progress = (index + 1).toFloat() / questions.size.toFloat())
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        (slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280))) togetherWith
                            (slideOutHorizontally(tween(200)) { -it / 3 } + fadeOut(tween(200)))
                    },
                    label = "questionCard"
                ) { _ ->
                    Column(modifier = Modifier.fillMaxWidth().themedCard()) {
                        if (q.stimulus != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        AppColors.AccentGold.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "PRIMARY SOURCE",
                                    color = AppColors.AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    q.stimulus,
                                    color = AppColors.TextPrimary,
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(
                            q.prompt,
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (!isAnswered) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        val fiftyAvailable = eliminated.isEmpty() && state.coins >= FIFTY_FIFTY_COST
                        val skipAvailable = state.coins >= SKIP_COST
                        com.woojoo.foundingfathers.ui.theme.SecondaryOutlineButton(
                            text = "50/50 (${FIFTY_FIFTY_COST})",
                            onClick = {
                                if (fiftyAvailable && viewModel.spendCoins(FIFTY_FIFTY_COST)) {
                                    val wrongIndices = q.choices.indices.filter { it != q.correctIndex }
                                    eliminated = wrongIndices.shuffled().take(2).toSet()
                                    soundManager.play(Sfx.TAP)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        com.woojoo.foundingfathers.ui.theme.SecondaryOutlineButton(
                            text = "Skip (${SKIP_COST})",
                            onClick = {
                                if (skipAvailable && viewModel.spendCoins(SKIP_COST)) {
                                    soundManager.play(Sfx.TAP)
                                    index += 1
                                    selected = null
                                    isAnswered = false
                                    eliminated = emptySet()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    q.choices.indices.forEach { i ->
                        ChoiceRow(
                            text = q.choices[i],
                            isSelected = selected == i,
                            isCorrect = if (isAnswered) i == q.correctIndex else null,
                            eliminated = i in eliminated,
                            onClick = {
                                if (!isAnswered && i !in eliminated) {
                                    selected = i
                                    soundManager.play(Sfx.TAP)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                AnimatedVisibility(visible = isAnswered) {
                    Column {
                        Text(q.explain, color = AppColors.TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                ShinyButton(
                    text = if (isAnswered) "Next" else "Submit",
                    onClick = {
                        if (isAnswered) {
                            soundManager.play(Sfx.TAP)
                            index += 1
                            selected = null
                            isAnswered = false
                            eliminated = emptySet()
                        } else {
                            val sel = selected ?: return@ShinyButton
                            viewModel.recordAnswer(q, sel)
                            isAnswered = true
                            val correct = sel == q.correctIndex
                            if (correct) {
                                score += 1
                                Haptics.success(context)
                                soundManager.play(Sfx.CORRECT)
                                viewModel.addXp(5)
                                viewModel.addCoins(2)
                                confettiTrigger += 1
                            } else {
                                Haptics.error(context)
                                soundManager.play(Sfx.WRONG)
                            }
                        }
                    }
                )
            }
        }

        ConfettiBurst(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}
