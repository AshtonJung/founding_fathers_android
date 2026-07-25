package com.ashtonjung.foundingfathers.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

enum class BgmGroup(val files: List<String>) {
    HOME(listOf("anthem", "freedom", "hero", "marching")),
    QUIZ(listOf("soaring", "hero")),
    EXPLORE(listOf("national", "stars")),
    QUOTES(listOf("backgroundMusic", "national", "stars")),
    SETTINGS(listOf("backgroundMusic")),
    RESULTS(listOf("victorympion"))
}

enum class Sfx(val file: String) {
    CORRECT("correct"),
    WRONG("wrong"),
    TAP("tap")
}

class SoundManager(private val context: Context) {
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgmGroup: BgmGroup? = null

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<Sfx, Int>()

    var bgmEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) stopBgm()
        }
    var sfxEnabled: Boolean = true
    var bgmVolume: Float = 0.35f
        set(value) {
            field = value
            bgmPlayer?.setVolume(value, value)
        }
    var sfxVolume: Float = 0.9f

    init {
        for (sfx in Sfx.values()) {
            runCatching {
                context.assets.openFd("bgm/${sfx.file}.mp3").use { fd ->
                    val id = soundPool.load(fd, 1)
                    soundIds[sfx] = id
                }
            }
        }
    }

    fun playBgm(group: BgmGroup) {
        if (!bgmEnabled) return
        if (currentBgmGroup == group && bgmPlayer?.isPlaying == true) return
        stopBgm()
        val pick = group.files.random()
        runCatching {
            val fd = context.assets.openFd("bgm/$pick.mp3")
            val player = MediaPlayer()
            player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            fd.close()
            player.isLooping = true
            player.setVolume(bgmVolume, bgmVolume)
            player.prepare()
            player.start()
            bgmPlayer = player
            currentBgmGroup = group
        }
    }

    fun stopBgm() {
        bgmPlayer?.let {
            runCatching { it.stop() }
            it.release()
        }
        bgmPlayer = null
        currentBgmGroup = null
    }

    fun play(sfx: Sfx) {
        if (!sfxEnabled) return
        val id = soundIds[sfx] ?: return
        soundPool.play(id, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun release() {
        stopBgm()
        soundPool.release()
    }
}
