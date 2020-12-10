package com.example.testnotes.classes;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.testnotes.database.NotesDatabase;
import com.example.testnotes.entities.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Repository {

    private static Repository instance;
    final private NotesDatabase notesDatabase;
    final private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Repository(Application application) {
        notesDatabase = NotesDatabase.getDatabase(application.getApplicationContext());
    }

    public static Repository getInstance(Application application) {
        if (instance == null) {
            instance = new Repository(application);
        }
        return instance;
    }


    public long insertNote(Note note) {
        Future<Long> future = executor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return notesDatabase.noteDao().insertNote(note);
            }
        });
        try {
            return future.get();
        } catch (Exception e) {
            return -1;
        }
    }

    public void deleteNote(Note note) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                notesDatabase.noteDao().deleteNote(note);
            }
        });
    }

    public List<Note> getAllNotes() {
        Future<List<Note>> future = executor.submit(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                return notesDatabase.noteDao().getAllNotes();
            }
        });
        try {
            return future.get();
        } catch (Exception e) {
           return null;
        }
    }

}