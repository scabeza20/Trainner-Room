package com.example.trainer_room.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.example.trainer_room.R

interface IAlarmSoundPlayer {
    fun playAlarm(durationSeconds: Int, onComplete: (() -> Unit)? = null)
    fun stop()
    fun release()
}

class AlarmSoundPlayer(private val context: Context) : IAlarmSoundPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    override fun playAlarm(durationSeconds: Int, onComplete: (() -> Unit)?) {
        stop()

        try {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.nuclear_alarm)?.apply {
                isLooping = true
                start()
            }

            stopRunnable = Runnable {
                stop()
                onComplete?.invoke()
            }
            handler.postDelayed(stopRunnable!!, (durationSeconds.coerceIn(1, 10) * 1000L))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = null
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }

    override fun release() {
        stop()
    }
}
