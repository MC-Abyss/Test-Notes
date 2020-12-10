package com.example.testnotes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.testnotes.R;
import com.example.testnotes.adapters.NotesAdapter;
import com.example.testnotes.classes.Repository;
import com.example.testnotes.database.NotesDatabase;
import com.example.testnotes.entities.Note;
import com.example.testnotes.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    public static final String NOTIFICATION_CHANNEL_ID = "test_notes_id";
    public final static String default_notification_channel_id = "default";

    private RecyclerView recyclerViewNotes;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private Repository repository;

    private int noteClickedPosition = -1;

    private String appLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = Repository.getInstance(getApplication());

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("appLanguage", appLanguage);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);

        recyclerViewNotes.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        recyclerViewNotes.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        EditText editSearch = findViewById(R.id.editSearch);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });

        findViewById(R.id.imageAddNoteQuickAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("appLanguage", appLanguage);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });

        findViewById(R.id.imageAddImageQuickAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("isFromQuickActions", true);
                intent.putExtra("quickActionType", "image");
                intent.putExtra("appLanguage", appLanguage);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });

        findViewById(R.id.imageAddURLQuickAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("isFromQuickActions", true);
                intent.putExtra("quickActionType", "url");
                intent.putExtra("appLanguage", appLanguage);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });

        findViewById(R.id.imageChangeLangQuickAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appLanguage.equals("en")) {
                    appLanguage = "ru";
                    setLocale(appLanguage, false);
                } else {
                    appLanguage = "en";
                    setLocale(appLanguage, false);
                }
            }
        });

        findViewById(R.id.imageVoiceInputQuickAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("isFromQuickActions", true);
                intent.putExtra("quickActionType", "voice");
                intent.putExtra("appLanguage", appLanguage);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });
    }

    //changing locale
    private void setLocale(String lang, boolean isStartUp) {
        saveLocale(lang);
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        if (!isStartUp) {
            //recreate();
            EditText editSearch = findViewById(R.id.editSearch);
            editSearch.setHint(R.string.search_hint);
        }
    }

    //saving locale
    public void saveLocale(String lang) {
        String langPreference = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPreference, lang);
        editor.apply();
    }

    //loading locale
    public void loadLocale() {
        String langPreference = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        String language = prefs.getString(langPreference, "en");
        appLanguage = language;
        setLocale(language, true);
    }

    //Note editing
    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        intent.putExtra("appLanguage", appLanguage);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    //Getting list of notes from db to display
    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        if(requestCode == REQUEST_CODE_SHOW_NOTES) {
            noteList.addAll(repository.getAllNotes());
            notesAdapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
            noteList.add(0, repository.getAllNotes().get(0));
            notesAdapter.notifyItemInserted(0);
            recyclerViewNotes.smoothScrollToPosition(0);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
            noteList.remove(noteClickedPosition);
            if (isNoteDeleted) {
                notesAdapter.notifyItemRemoved(noteClickedPosition);
            } else {
                noteList.add(noteClickedPosition, repository.getAllNotes().get(noteClickedPosition));
                notesAdapter.notifyItemChanged(noteClickedPosition);
            }
        }
    }

    //Getting result of CreateNoteActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(
                        REQUEST_CODE_UPDATE_NOTE,
                        data.getBooleanExtra("isNoteDeleted", false)
                );
            }
        }
    }
}