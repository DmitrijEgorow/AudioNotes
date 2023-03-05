package ru.viable.audionotes

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi

class AudioNotesApplication : Application() {

    lateinit var mediaRecorder: MediaRecorderProvider

    override fun onCreate() {
        super.onCreate()

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorderProvider.BuildAboveOrS()
        } else {
            MediaRecorderProvider.BuildBelowS()
        }
    }
}

interface MediaRecorderProvider {
    fun getMediaRecorder(context: Context): MediaRecorder

    class BuildBelowS : MediaRecorderProvider {
        override fun getMediaRecorder(context: Context) = MediaRecorder()
    }

    class BuildAboveOrS : MediaRecorderProvider {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun getMediaRecorder(context: Context) = MediaRecorder(context)
    }
}
