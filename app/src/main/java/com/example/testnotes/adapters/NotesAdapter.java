package com.example.testnotes.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testnotes.R;
import com.example.testnotes.entities.Note;
import com.example.testnotes.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;


    public NotesAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        this.notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewNoteText, textViewURLNote, textViewDateTimeNote;
        LinearLayout layoutNote;
        ImageView imageNoteContainer;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewNoteText = itemView.findViewById(R.id.textViewNoteText);
            textViewDateTimeNote = itemView.findViewById(R.id.textViewDateTimeNote);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNoteContainer = itemView.findViewById(R.id.imageNoteContainer);
            textViewURLNote = itemView.findViewById(R.id.textViewURLNote);

        }

        void setNote(Note note) {
            textViewTitle.setText(note.getTitle());
            if (note.getNoteText().trim().isEmpty()) {
                textViewNoteText.setVisibility(View.GONE);
            } else {
                textViewNoteText.setText(note.getNoteText());
                textViewNoteText.setVisibility(View.VISIBLE);
            }
            if (note.getWebLink() == null){
                textViewURLNote.setVisibility(View.GONE);
            } else {
                textViewURLNote.setText(note.getWebLink());
                textViewURLNote.setVisibility(View.VISIBLE);
            }
            textViewDateTimeNote.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(ResourcesCompat.getColor(
                        itemView.getResources(),
                        R.color.default_note_color,
                        null)
                );
            }

            if (note.getImagePath() != null) {
                Bitmap imageFile = BitmapFactory.decodeFile(note.getImagePath());
                if (imageFile != null) {
                    imageNoteContainer.setImageBitmap(imageFile);
                    imageNoteContainer.setVisibility(View.VISIBLE);
                } else {
                    imageNoteContainer.setVisibility(View.GONE);
                }
            } else {
                imageNoteContainer.setVisibility(View.GONE);
            }
        }
    }

    public void searchNotes(final String searchKeywords) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeywords.trim().isEmpty()) {
                    notes = notesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeywords.toLowerCase())
                        || note.getNoteText().toLowerCase().contains(searchKeywords.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
