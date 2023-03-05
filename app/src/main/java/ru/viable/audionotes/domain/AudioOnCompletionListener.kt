package ru.viable.audionotes.domain

import android.media.MediaPlayer

interface AudioOnCompletionListener : MediaPlayer.OnCompletionListener {
    override fun onCompletion(mediaPlayer: MediaPlayer)
}
