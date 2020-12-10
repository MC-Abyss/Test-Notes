package com.example.testnotes.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.testnotes.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("select * from NOTES order by id desc")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
