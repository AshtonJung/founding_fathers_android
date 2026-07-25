package com.woojoo.foundingfathers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.woojoo.foundingfathers.audio.SoundManager
import com.woojoo.foundingfathers.navigation.AppRoot
import com.woojoo.foundingfathers.state.AppViewModel
import com.woojoo.foundingfathers.ui.theme.FoundingFathersTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()
    lateinit var soundManager: SoundManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(applicationContext)

        setContent {
            FoundingFathersTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.state.collectAsState()

                    LaunchedEffect(state.backgroundMusicEnabled, state.bgmVolume) {
                        soundManager.bgmEnabled = state.backgroundMusicEnabled
                        soundManager.bgmVolume = state.bgmVolume
                    }
                    LaunchedEffect(state.soundEnabled, state.sfxVolume) {
                        soundManager.sfxEnabled = state.soundEnabled
                        soundManager.sfxVolume = state.sfxVolume
                    }

                    if (state.loaded) {
                        AppRoot(viewModel = viewModel, soundManager = soundManager)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
