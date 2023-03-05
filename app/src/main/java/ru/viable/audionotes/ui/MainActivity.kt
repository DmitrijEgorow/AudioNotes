package ru.viable.audionotes.ui

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import io.realm.OrderedRealmCollection
import io.realm.Realm
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import ru.viable.audionotes.AudioNotesApplication
import ru.viable.audionotes.R
import ru.viable.audionotes.adapters.AudioNoteAdapter
import ru.viable.audionotes.databinding.ActivityMainBinding
import ru.viable.audionotes.db.AudioNote
import ru.viable.audionotes.db.AudioNoteListChangeListener
import ru.viable.audionotes.db.RealmManager
import ru.viable.audionotes.domain.AudioNoteRecognitionListener
import ru.viable.audionotes.domain.AudioNoteRecognizer
import ru.viable.audionotes.domain.AudioOnCompletionListener
import ru.viable.audionotes.domain.AudioPlayerManager
import ru.viable.audionotes.domain.AudioRecorderManager
import ru.viable.audionotes.domain.RecognizedAudioNote
import java.io.IOException
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private val tag = "Tests"
    private val requestCode = 1

    private lateinit var binding: ActivityMainBinding

    private lateinit var fab: FloatingActionButton

    private lateinit var realmManager: RealmManager
    private var adapter: AudioNoteAdapter? = null

    private lateinit var calendar: Calendar
    private val timeout = 10_000

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private lateinit var audioNoteRecognitionListener: AudioNoteRecognitionListener
    private val sampleRate = 16000.0f

    private var isInProgress: Boolean = false

    private val fileNamePrefix = "note"
    private var audioRecorderManager = AudioRecorderManager()
    private var audioPlayerManager = AudioPlayerManager()
    private var recorder: MediaRecorder? = null
    private var recordingFileName = ""
    private val audioTimer = AudioTimer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fab = binding.fabAddNewAudionote
        setUiElementsBusy(false)

        Realm.init(applicationContext)
        realmManager = RealmManager()

        recorder = (application as AudioNotesApplication).mediaRecorder.getMediaRecorder(applicationContext)

        calendar = Calendar.getInstance(TimeZone.getDefault())

        setUiState(false)
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                requestCode,
            )
        } else {
            initModel()
        }

        audioNoteRecognitionListener = object : AudioNoteRecognitionListener() {
            override fun onResult(hypothesis: String?) {
                super.onResult(hypothesis)
                Log.d(tag, hypothesis.toString())
            }

            override fun onFinalResult(hypothesis: String?) {
                super.onFinalResult(hypothesis)
                val recognizedNote =
                    Gson().fromJson(hypothesis.toString(), RecognizedAudioNote::class.java)
                calendar = Calendar.getInstance(TimeZone.getDefault())
                val now = calendar.time
                val df = SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault())
                val formattedDate: String = df.format(now)
                Log.d(tag, "$now $formattedDate $recognizedNote")
                if (recognizedNote.text.isNotEmpty()) {
                    addNewAudioNote(recognizedNote.text, formattedDate)
                }
            }

            override fun onError(exception: java.lang.Exception?) {
                super.onError(exception)
                Snackbar.make(fab, R.string.recording_error, Snackbar.LENGTH_SHORT).show()
                setUiElementsBusy(false)
            }

            override fun onTimeout() {
                super.onTimeout()
                Snackbar.make(fab, R.string.recording_timeout, Snackbar.LENGTH_SHORT).show()
                setUiElementsBusy(false)
            }
        }

        fab.setOnClickListener { v ->
            if (!isInProgress) {
                val rec = Recognizer(model, sampleRate)
                speechService = SpeechService(rec, sampleRate)
                AudioNoteRecognizer(
                    model,
                    speechService,
                    audioNoteRecognitionListener,
                ).recognizeMicrophone(timeout)
                setUiElementsBusy()

                recorder = (application as AudioNotesApplication).mediaRecorder.getMediaRecorder(applicationContext)
                recordingFileName = "$dataDir/$fileNamePrefix${realmManager.nextId}.wav"
                if (recorder != null) {
                    audioRecorderManager.startRecording(recorder!!, recordingFileName)
                }
            } else {
                if (speechService != null) {
                    speechService!!.stop()
                    speechService = null
                }
                audioRecorderManager.stopRecording()
                setUiElementsBusy(false)
            }
        }

        adapter = AudioNoteAdapter(
            (realmManager.data as OrderedRealmCollection<AudioNote>?)!!,
            object : AudioNoteListChangeListener {
                override fun updateFocusedItem(selectedNoteId: Int, progressBar: ProgressBar) {
                    playAudio("$dataDir/$fileNamePrefix$selectedNoteId.wav", progressBar)
                }

                override fun updateTotalNumber(totalNumber: Long) {
                }
            },
        )

        binding.recyclerAudionotesList.layoutManager = LinearLayoutManager(this)
        binding.recyclerAudionotesList.adapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initModel()
            }
        }
    }

    private fun addNewAudioNote(text: String, date: String) {
        realmManager.insertNote(AudioNote(text, date))
    }

    private fun setUiElementsBusy(isInProgress: Boolean = true) {
        fab.isActivated = isInProgress
        this.isInProgress = isInProgress
    }

    private fun initModel() {
        StorageService.unpack(
            this,
            getString(R.string.model_en_us),
            "model",
            { model: Model ->
                this.model = model
                setUiState(true)
            },
        ) { exception: IOException ->
            Log.d(tag, "Failed to unpack ${exception.message}")
        }
    }

    private fun setUiState(isReady: Boolean) {
        binding.fabAddNewAudionote.isEnabled = isReady
    }

    private fun playAudio(fileName: String, progressBar: ProgressBar) {
        if (audioPlayerManager.isPlayingAudio) {
            audioPlayerManager.stopPlaying()
            audioTimer.cancel()
        } else {
            audioTimer.cancel()
            audioPlayerManager.startPlaying(
                fileName,
                object : AudioOnCompletionListener {
                    override fun onCompletion(mediaPlayer: MediaPlayer) {
                        progressBar.progress = 0
                        audioTimer.cancel()
                    }
                },
            )
            audioTimer.runTimer(audioPlayerManager.mediaPlayer, progressBar)
        }
    }

    class AudioTimer {
        private var timerTask = Timer()
        fun runTimer(mPlayer: MediaPlayer?, progressBar: ProgressBar) {
            val timer: Pair<Int, Int>
            if (mPlayer != null) {
                timer = Pair(mPlayer.currentPosition, mPlayer.duration)
                timerTask = Timer()
                progressBar.max = timer.second
                timerTask.schedule(
                    object : TimerTask() {
                        override fun run() {
                            var i = 0
                            try {
                                if (mPlayer.isPlaying) {
                                    i = mPlayer.currentPosition
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            progressBar.progress = i
                        }
                    },
                    0,
                    200,
                )
            }
        }

        fun cancel() {
            timerTask.cancel()
        }
    }

    public override fun onPause() {
        super.onPause()
        audioRecorderManager.releaseMediaRecorder()
        audioPlayerManager.releaseMediaPlayer()
    }
}
