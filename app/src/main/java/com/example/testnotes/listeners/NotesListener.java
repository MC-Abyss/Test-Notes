package com.example.testnotes.listeners;

import com.example.testnotes.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
