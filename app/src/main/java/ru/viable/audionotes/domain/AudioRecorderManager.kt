package ru.viable.audionotes.domain

import android.media.MediaRecorder
import android.util.Log
import java.io.IOException

class AudioRecorderManager {
    private val tag = "Tests"
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    fun startRecording(recorder: MediaRecorder, mFileName: String) {
        if (!isRecording) {
            recorder.apply {
                mediaRecorder = recorder
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(mFileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e(tag, "prepare() failed")
                }
                start()
                Log.i(tag, "Recording Started")
                isRecording = true
            }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            Log.i(tag, "Recording Stopped")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder?.reset()
            releaseMediaRecorder()
        }
    }

    fun releaseMediaRecorder() {
        mediaRecorder?.release()
        mediaRecorder = null
        Log.i(tag, "Recorder Released")

        isRecording = false
    }
}
