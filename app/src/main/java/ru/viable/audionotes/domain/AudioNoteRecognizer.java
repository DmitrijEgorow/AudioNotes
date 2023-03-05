package ru.viable.audionotes.domain;

import org.vosk.Model;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

public class AudioNoteRecognizer implements AudioRecognizer {

    private Model model;
    private SpeechService speechService;
    private RecognitionListener listener;

    public AudioNoteRecognizer(Model model, SpeechService speechService, RecognitionListener listener) {
        this.model = model;
        this.speechService = speechService;
        this.listener = listener;
    }

    @Override
    public void recognizeFile() {
    }

    @Override
    public void recognizeMicrophone(int timeout) {
        speechService.startListening(listener, timeout);
    }
}
