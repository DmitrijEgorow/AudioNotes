package ru.viable.audionotes.db;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmManager {

    private final String TAG = "Tests";

    RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().name(Realm.DEFAULT_REALM_NAME).schemaVersion(0).allowWritesOnUiThread(true).deleteRealmIfMigrationNeeded().build();
    Realm realm = Realm.getInstance(realmConfiguration);

    public void insertNote(@NonNull AudioNote task) {
        int nextId = getNextId();
        task.setNoteId(nextId);
        Log.d(TAG, "insert note " + nextId);
        realm.beginTransaction();
        realm.copyToRealm(task);
        realm.commitTransaction();
    }

    public int getNextId(){
        Number number = realm.where(AudioNote.class).max("noteId");
        final int nextId;

        if (number == null) {
            nextId = 0;
        } else {
            nextId = number.intValue() + 1;
        }
        Log.d(TAG, "get next id " + nextId);
        return nextId;
    }

    public @Nullable List<AudioNote> getData() {
        return realm.where(AudioNote.class).findAll();
    }

    public long getNumberOfNotes() {
        return realm.where(AudioNote.class).count();
    }

    public void deleteNote(int Task_id) {
        final AudioNote model = realm.where(AudioNote.class).equalTo("noteId", Task_id).findFirst();
        realm.executeTransaction(realm -> {
            if (model != null) {
                model.deleteFromRealm();
            }
        });
    }
}
