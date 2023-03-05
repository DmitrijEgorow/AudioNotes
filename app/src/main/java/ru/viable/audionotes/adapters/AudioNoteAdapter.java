package ru.viable.audionotes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ru.viable.audionotes.R;
import ru.viable.audionotes.db.AudioNote;
import ru.viable.audionotes.db.AudioNoteListChangeListener;
import ru.viable.audionotes.db.RealmManager;

public class AudioNoteAdapter extends RealmRecyclerViewAdapter<AudioNote, AudioNoteAdapter.AudioNotesViewHolder> {

    OrderedRealmCollection<AudioNote> data;
    AudioNoteListChangeListener listener;

    public AudioNoteAdapter(@NonNull OrderedRealmCollection<AudioNote> data, @NonNull AudioNoteListChangeListener listener) {
        super(data, true);
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AudioNotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_row, parent, false);
        return new AudioNotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioNotesViewHolder holder, int position) {
        final AudioNote model = getItem(position);
        if (model != null) {
            String noteText = model.getNoteContent();
            String noteDate = model.getDate();
            holder.tvNoteContents.setText(noteText);
            holder.tvNoteDate.setText(noteDate);
        }

        holder.itemView.setOnClickListener(v -> {
            int holderPosition = holder.getAdapterPosition();
            listener.updateFocusedItem(data.get(holderPosition).getNoteId(), holder.progressBar);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int holderPosition = holder.getAdapterPosition();
            RealmManager realmManager = new RealmManager();
            realmManager.deleteNote(data.get(holderPosition).getNoteId());
            listener.updateTotalNumber(realmManager.getNumberOfNotes());
            return true;
        });
    }

    @Override
    public long getItemId(int index) {
        AudioNote task = getItem(index);
        if (task != null) {
            return task.getNoteId();
        } else {
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class AudioNotesViewHolder extends RecyclerView.ViewHolder {

        TextView tvNoteContents;
        TextView tvNoteDate;
        ProgressBar progressBar;

        public AudioNotesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteContents = itemView.findViewById(R.id.audionote_contents);
            tvNoteDate = itemView.findViewById(R.id.audionote_date);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}

