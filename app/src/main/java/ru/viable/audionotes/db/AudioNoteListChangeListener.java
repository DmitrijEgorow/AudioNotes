package ru.viable.audionotes.db;

import android.widget.ProgressBar;

public interface AudioNoteListChangeListener {

    void updateFocusedItem(int id, ProgressBar progressBar);

    void updateTotalNumber(long totalNumber);
}
