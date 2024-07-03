package com.example.cheese_deux_api
    .component_classes

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AudioClass(context: Context, audioIndex: Int) {
    private val mediaPlayer = MediaPlayer.create(context, audioIndex)

    fun play(volume: Float) {
        CoroutineScope(context = kotlinx.coroutines.Dispatchers.Default).launch {
            mediaPlayer.setVolume(volume, volume)
            mediaPlayer.start()
        }
    }

    fun playLoop(volume: Float) {
        CoroutineScope(context = kotlinx.coroutines.Dispatchers.Default).launch {
            mediaPlayer.setVolume(volume, volume)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        }
    }

    fun pauseLoop() {
        mediaPlayer.pause()
    }

    fun resumeLoop() {
        mediaPlayer.start()
    }

    fun release() {
        mediaPlayer.release()
    }

}

enum class AudioType {
    BUTTON,
    ENTER,
    CHEESE_COLLECT,
    CHEESE_SHOOT,
    CHEESE_REVIVE,
    INVULNERABILITY,
    SPEEDUP,
    FIRST_HIT,
    GAME_OVER
}