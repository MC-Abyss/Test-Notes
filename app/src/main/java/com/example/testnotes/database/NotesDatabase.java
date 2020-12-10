package com.example.testnotes.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.testnotes.dao.NoteDao;
import com.example.testnotes.entities.Note;

@Database(entities = Note.class, version = 3, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase notesDatabase;

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE NOTES ADD COLUMN notification_date_time TEXT");
        }
    };

    public static synchronized NotesDatabase getDatabase(Context context) {
        if(notesDatabase == null) {
            notesDatabase = Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    "notesdb"
            ).fallbackToDestructiveMigrationFrom(1)
                    .addMigrations(NotesDatabase.MIGRATION_2_3)
                    .build();
        }
        return notesDatabase;
    }

    public abstract NoteDao noteDao();
}
