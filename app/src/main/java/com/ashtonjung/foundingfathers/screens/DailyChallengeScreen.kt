package com.ashtonjung.foundingfathers.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashtonjung.foundingfathers.audio.BgmGroup
import com.ashtonjung.foundingfathers.audio.Haptics
import com.ashtonjung.foundingfathers.audio.Sfx
import com.ashtonjung.foundingfathers.audio.SoundManager
import com.ashtonjung.foundingfathers.state.AppViewModel
import com.ashtonjung.foundingfathers.ui.theme.AppColors
import com.ashtonjung.foundingfathers.ui.theme.ChoiceRow
import com.ashtonjung.foundingfathers.ui.theme.ConfettiBurst
import com.ashtonjung.foundingfathers.ui.theme.GradientProgressBar
import com.ashtonjung.foundingfathers.ui.theme.ShinyButton
import com.ashtonjung.foundingfathers.ui.theme.gradientBackground
import com.ashtonjung.foundingfathers.ui.theme.themedCard
import androidx.compose.ui.platform.LocalContext

@Composable
fun DailyChallengeScreen(
    viewModel: AppViewModel,
    soundManager: SoundManager,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val questions = state.dailyQuestions
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var confettiTrigger by remember { mutableIntStateOf(0) }

    val goal = maxOf(1, state.dailyGoal)
    val doneForToday = state.dailyDone && state.dailyCorrect >= goal

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Daily Challenge", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Today's Goal", color = AppColors.TextSecondary)
            Text("${state.dailyCorrect} / $goal", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        GradientProgressBar(progress = state.dailyCorrect.toFloat() / goal.toFloat())
        Spacer(modifier = Modifier.height(20.dp))

        if (doneForToday) {
            val entrance = remember { Animatable(0.6f) }
            LaunchedEffect(Unit) {
                entrance.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            }
            Column(
                modifier = Modifier.fillMaxWidth().scale(entrance.value).themedCard(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.height(56.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Daily challenge complete!", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Come back tomorrow for a new set.", color = AppColors.TextSecondary)
                Spacer(modifier = Modifier.height(14.dp))
                ShinyButton(text = "Great!", onClick = onClose)
            }
        } else if (index < questions.size) {
            val q = questions[index]
            Column(modifier = Modifier.fillMaxWidth().themedCard()) {
                Text(q.prompt, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                q.choices.indices.forEach { i ->
                    ChoiceRow(
                        text = q.choices[i],
                        isSelected = selected == i,
                        isCorrect = if (isAnswered) i == q.correctIndex else null,
                        onClick = {
                            if (!isAnswered) {
                                selected = i
                                soundManager.play(Sfx.TAP)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            AnimatedVisibility(visible = isAnswered) {
                val sel = selected
                Column {
                    Text(
                        if (sel == q.correctIndex) "Correct!" else q.explain,
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Question ${index + 1} of ${questions.size}", color = AppColors.TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ShinyButton(
                text = if (isAnswered) "Next" else "Submit",
                onClick = {
                    if (isAnswered) {
                        soundManager.play(Sfx.TAP)
                        if (index + 1 < questions.size) {
                            index += 1
                            selected = null
                            isAnswered = false
                        }
                    } else {
                        val sel = selected ?: return@ShinyButton
                        isAnswered = true
                        val correct = sel == q.correctIndex
                        viewModel.applyDailyAnswer(correct)
                        if (correct) {
                            Haptics.success(context)
                            soundManager.play(Sfx.CORRECT)
                            viewModel.addXp(10)
                            confettiTrigger += 1
                        } else {
                            Haptics.error(context)
                            soundManager.play(Sfx.WRONG)
                        }
                    }
                }
            )
        } else {
            Text("Loading…", color = AppColors.TextSecondary)
        }
    }

        ConfettiBurst(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        soundManager.playBgm(BgmGroup.QUIZ)
    }
}
