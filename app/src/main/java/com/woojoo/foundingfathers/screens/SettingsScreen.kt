package com.woojoo.foundingfathers.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.woojoo.foundingfathers.audio.BgmGroup
import com.woojoo.foundingfathers.audio.SoundManager
import com.woojoo.foundingfathers.notifications.NotificationScheduler
import com.woojoo.foundingfathers.state.AppViewModel
import com.woojoo.foundingfathers.ui.theme.AppColors
import com.woojoo.foundingfathers.ui.theme.SectionTitle
import com.woojoo.foundingfathers.ui.theme.ShinyButton
import com.woojoo.foundingfathers.ui.theme.gradientBackground
import com.woojoo.foundingfathers.ui.theme.themedCard

@Composable
fun SettingsScreen(viewModel: AppViewModel, soundManager: SoundManager) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationScheduler.scheduleDaily(context, state.dailyReminderHour, state.dailyReminderMinute)
            viewModel.setDailyReminder(true, state.dailyReminderHour, state.dailyReminderMinute)
        } else {
            viewModel.setDailyReminder(false, state.dailyReminderHour, state.dailyReminderMinute)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Settings", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)

        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            SectionTitle("Music")
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Background Music", color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.backgroundMusicEnabled,
                    onCheckedChange = {
                        viewModel.setBgmEnabled(it)
                        if (it) soundManager.playBgm(BgmGroup.SETTINGS) else soundManager.stopBgm()
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("BGM Volume", color = AppColors.TextSecondary, fontSize = 12.sp)
            Slider(
                value = state.bgmVolume,
                onValueChange = { viewModel.setBgmVolume(it) },
                valueRange = 0f..1f
            )
        }

        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            SectionTitle("Sound Effects")
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("SFX", color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Switch(checked = state.soundEnabled, onCheckedChange = { viewModel.setSfxEnabled(it) })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("SFX Volume", color = AppColors.TextSecondary, fontSize = 12.sp)
            Slider(
                value = state.sfxVolume,
                onValueChange = { viewModel.setSfxVolume(it) },
                valueRange = 0f..1f
            )
        }

        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            SectionTitle("Notifications")
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Daily Reminder", color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!granted) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@Switch
                                }
                            }
                            NotificationScheduler.scheduleDaily(context, state.dailyReminderHour, state.dailyReminderMinute)
                            viewModel.setDailyReminder(true, state.dailyReminderHour, state.dailyReminderMinute)
                        } else {
                            NotificationScheduler.cancel(context)
                            viewModel.setDailyReminder(false, state.dailyReminderHour, state.dailyReminderMinute)
                        }
                    }
                )
            }
            Text(
                "Reminder time: ${"%02d".format(state.dailyReminderHour)}:${"%02d".format(state.dailyReminderMinute)}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp
            )
        }

        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            SectionTitle("Onboarding")
            Spacer(modifier = Modifier.height(10.dp))
            ShinyButton(text = "Show Onboarding Again", onClick = { viewModel.replayOnboarding() })
        }

        Column(modifier = Modifier.fillMaxWidth().themedCard()) {
            Text("Voices of the Founders", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Interactive timelines, quizzes, and quote matching.", color = AppColors.TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("No accounts. No ads. No tracking.", color = AppColors.TextSecondary, fontSize = 11.sp)
        }
    }
}
