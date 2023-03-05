package ru.viable.audionotes.db;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;

import androidx.annotation.NonNull;

import io.realm.RealmObject;

public class AudioNote extends RealmObject {

    int noteId;
    String noteContent;

    String date;

    public AudioNote() {
    }

    public AudioNote(@NonNull String audioNoteContents, @NonNull String date) {
        this.noteContent = audioNoteContents;
        this.date = date;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getDate() {
        return date;
    }

    public @NonNull String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(@NonNull String noteContent) {
        this.noteContent = noteContent;
    }
}
