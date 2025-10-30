package com.kkdev.crackie.ui.home

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.kkdev.crackie.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundPlayer(context: Context) {

    private var soundPool: SoundPool? = null
    private var tapSoundId: Int = 0
    private var revealSoundId: Int = 0
    private var isLoaded = false

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

            tapSoundId = soundPool?.load(context, R.raw.crumb, 1) ?: 0
            revealSoundId = soundPool?.load(context, R.raw.paper, 1) ?: 0

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isLoaded = true
                }
            }
        }
    }

    fun playTapSound() {
        if (isLoaded) {
            soundPool?.play(tapSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playRevealSound() {
        if (isLoaded) {
            soundPool?.play(revealSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
