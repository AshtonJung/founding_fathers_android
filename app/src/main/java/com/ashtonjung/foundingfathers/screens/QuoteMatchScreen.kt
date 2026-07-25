package com.ashtonjung.foundingfathers.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashtonjung.foundingfathers.audio.BgmGroup
import com.ashtonjung.foundingfathers.audio.Haptics
import com.ashtonjung.foundingfathers.audio.Sfx
import com.ashtonjung.foundingfathers.audio.SoundManager
import com.ashtonjung.foundingfathers.state.AppViewModel
import com.ashtonjung.foundingfathers.ui.theme.AppColors
import com.ashtonjung.foundingfathers.ui.theme.ChoiceRow
import com.ashtonjung.foundingfathers.ui.theme.ShinyButton
import com.ashtonjung.foundingfathers.ui.theme.gradientBackground
import com.ashtonjung.foundingfathers.ui.theme.themedCard

@Composable
fun QuoteMatchScreen(viewModel: AppViewModel, soundManager: SoundManager) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val quotes = com.ashtonjung.foundingfathers.data.SampleData.quotes
    val founderNames = state.fathers.map { it.name }

    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    var selected by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    fun prepareRound() {
        selected = null
        isAnswered = false
        val author = quotes[index].second
        val wrongs = (founderNames - author).shuffled().take(2)
        options = (wrongs + author).shuffled()
    }

    fun reset() {
        index = 0
        score = 0
        prepareRound()
    }

    LaunchedEffect(Unit) {
        reset()
        soundManager.playBgm(BgmGroup.QUOTES)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .padding(16.dp)
    ) {
        Text("Quotes", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (quotes.isEmpty()) {
            Text("No quotes available.", color = AppColors.TextSecondary)
        } else {
            val pair = quotes[index]
            Column(modifier = Modifier.fillMaxWidth().themedCard()) {
                Text(
                    "“${pair.first}”",
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { name ->
                    ChoiceRow(
                        text = name,
                        isSelected = selected == name,
                        isCorrect = if (isAnswered) name == pair.second else null,
                        onClick = {
                            if (!isAnswered) {
                                selected = name
                                soundManager.play(Sfx.TAP)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    if (isAnswered) (if (selected == pair.second) "Correct" else "Incorrect") else "",
                    color = AppColors.TextSecondary
                )
            }
            ShinyButton(
                text = if (isAnswered) "Next" else "Submit",
                onClick = {
                    if (isAnswered) {
                        soundManager.play(Sfx.TAP)
                        if (index + 1 < quotes.size) {
                            index += 1
                            prepareRound()
                        } else {
                            viewModel.setBestQuoteScore(score)
                            reset()
                        }
                    } else {
                        val sel = selected ?: return@ShinyButton
                        isAnswered = true
                        if (sel == pair.second) {
                            score += 1
                            Haptics.success(context)
                            soundManager.play(Sfx.CORRECT)
                        } else {
                            Haptics.error(context)
                            soundManager.play(Sfx.WRONG)
                        }
                        if (index == quotes.size - 1) viewModel.setBestQuoteScore(score)
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Score $score / ${quotes.size}  ·  Best ${state.bestQuoteScore}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
