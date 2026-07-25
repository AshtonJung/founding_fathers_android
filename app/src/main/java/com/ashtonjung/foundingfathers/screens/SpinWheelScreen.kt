package com.ashtonjung.foundingfathers.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashtonjung.foundingfathers.audio.Haptics
import com.ashtonjung.foundingfathers.audio.Sfx
import com.ashtonjung.foundingfathers.audio.SoundManager
import com.ashtonjung.foundingfathers.state.AppViewModel
import com.ashtonjung.foundingfathers.ui.theme.AppColors
import com.ashtonjung.foundingfathers.ui.theme.ConfettiBurst
import com.ashtonjung.foundingfathers.ui.theme.ShinyButton
import com.ashtonjung.foundingfathers.ui.theme.gradientBackground
import com.ashtonjung.foundingfathers.ui.theme.themedCard
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class WheelReward(val label: String, val coins: Int, val xp: Int, val color: Color)

private val wheelRewards = listOf(
    WheelReward("+5 Coins", 5, 0, Color(0xFFDB2626)),
    WheelReward("+10 XP", 0, 10, AppColors.PrimaryBlue),
    WheelReward("+10 Coins", 10, 0, AppColors.AccentGold),
    WheelReward("+20 XP", 0, 20, Color(0xFF4CAF50)),
    WheelReward("+15 Coins", 15, 0, Color(0xFFDB2626)),
    WheelReward("+5 XP", 0, 5, AppColors.PrimaryBlue),
    WheelReward("+25 Coins", 25, 0, AppColors.AccentGold),
    WheelReward("JACKPOT +50", 50, 20, Color(0xFFEC407A))
)

@Composable
fun SpinWheelScreen(
    viewModel: AppViewModel,
    soundManager: SoundManager,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var spinning by remember { mutableStateOf(false) }
    var resultIndex by remember { mutableStateOf<Int?>(null) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    val rotation = remember { Animatable(0f) }
    val today = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }
    // Reactive: recomputed whenever state.lastSpinDate changes (e.g. right after claimDailySpin),
    // unlike a one-shot `remember` which would keep the button enabled after spinning.
    val alreadySpunToday = state.lastSpinDate == today

    val segCount = wheelRewards.size
    val segAngle = 360f / segCount

    fun spin() {
        if (spinning || alreadySpunToday) return
        spinning = true
        resultIndex = null
        val chosen = Random.nextInt(segCount)
        // Pointer is fixed at the top (12 o'clock / 0deg). Spin several full turns, then
        // land so the chosen segment's center ends up under the pointer.
        val landingOffset = 360f - (chosen * segAngle + segAngle / 2f)
        val target = rotation.value + 360f * 6 + ((landingOffset - rotation.value % 360f + 360f) % 360f)
        scope.launch {
            rotation.animateTo(target, animationSpec = tween(durationMillis = 3400, easing = FastOutSlowInEasing))
            resultIndex = chosen
            spinning = false
            val reward = wheelRewards[chosen]
            viewModel.claimDailySpin(reward.coins, reward.xp)
            soundManager.play(Sfx.CORRECT)
            Haptics.success(context)
            confettiTrigger += 1
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .gradientBackground()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Daily Spin", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = AppColors.AccentGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${state.coins}", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Spin once a day for free coins and XP!",
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .rotate(rotation.value)
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val paint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = radius * 0.14f
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    wheelRewards.forEachIndexed { i, reward ->
                        val startAngle = i * segAngle - 90f
                        drawArc(
                            color = reward.color,
                            startAngle = startAngle,
                            sweepAngle = segAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                        )
                        val midAngleRad = Math.toRadians((startAngle + segAngle / 2f).toDouble())
                        val textRadius = radius * 0.62f
                        val tx = center.x + (kotlin.math.cos(midAngleRad) * textRadius).toFloat()
                        val ty = center.y + (kotlin.math.sin(midAngleRad) * textRadius).toFloat()
                        drawContext.canvas.nativeCanvas.save()
                        drawContext.canvas.nativeCanvas.rotate(
                            (startAngle + segAngle / 2f + 90f),
                            tx, ty
                        )
                        drawContext.canvas.nativeCanvas.drawText(reward.label, tx, ty, paint)
                        drawContext.canvas.nativeCanvas.restore()
                    }
                    drawCircle(color = Color.White.copy(alpha = 0.9f), radius = radius, style = Stroke(width = 6f))
                }

                // Fixed pointer at the top, doesn't rotate with the wheel
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .size(28.dp)
                ) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width / 2f, size.height)
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = resultIndex != null && !spinning) {
                resultIndex?.let { idx ->
                    Column(
                        modifier = Modifier.fillMaxWidth().themedCard(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("You won!", color = AppColors.TextSecondary, fontSize = 13.sp)
                        Text(
                            wheelRewards[idx].label,
                            color = AppColors.AccentGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            ShinyButton(
                text = when {
                    spinning -> "Spinning..."
                    alreadySpunToday -> "Come back tomorrow"
                    else -> "Spin!"
                },
                enabled = !spinning && !alreadySpunToday,
                onClick = { spin() }
            )
        }

        ConfettiBurst(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}
