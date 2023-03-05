package ru.viable.audionotes.domain;

public interface AudioRecognizer {

    void recognizeFile();

    void recognizeMicrophone(int timeout);
}
