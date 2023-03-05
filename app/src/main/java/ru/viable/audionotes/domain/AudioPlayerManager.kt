package ru.viable.audionotes.domain

import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

class AudioPlayerManager {
    private val tag = "Tests"
    var isPlayingAudio = false
        private set
    var mediaPlayer: MediaPlayer? = null

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        Log.i(tag, "Audio Player Released")
    }

    fun startPlaying(mFileName: String?, completionListener: AudioOnCompletionListener) {
        if (!isPlayingAudio) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
                setOnCompletionListener {
                    stopPlaying()
                    isPlayingAudio = false
                    if (mediaPlayer != null) {
                        completionListener.onCompletion(it)
                    }
                }
                try {
                    setDataSource(mFileName)
                    prepare()
                    if (!isPlaying) start()
                    Log.i(tag, "Audio Player Started")
                } catch (e: IOException) {
                    Log.e(tag, "prepare() failed")
                }
                isPlayingAudio = true
            }
        }
    }

    fun stopPlaying() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        Log.i(tag, "Audio Player Stopped")
        releaseMediaPlayer()
        isPlayingAudio = false
    }
}
