package com.example.testnotes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.testnotes.R;
import com.example.testnotes.classes.Repository;
import com.example.testnotes.classes.NotificationPublisher;
import com.example.testnotes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.testnotes.activities.MainActivity.NOTIFICATION_CHANNEL_ID;
import static com.example.testnotes.activities.MainActivity.default_notification_channel_id;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText editNoteTitle, editNoteText;
    private TextView textDateTime, textNotification, textNotificationDateTime, textURL, textViewVoiceInput;
    private View viewSubtitleIndicator;
    private ImageView imageNote, imageVoiceInput;
    private LinearLayout layoutURL;
    private DatePicker datePickerSetNotification;
    private TimePicker timePickerSetNotification;

    private int selectedNoteColor;
    private String selectedImagePath;
    private Date notificationDate;
    private AlertDialog dialogAddURL, dialogDeleteNote, dialogSetNotification;

    private Note filledNote;
    private String appLanguage;
    private  Repository repository;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 4;
    private static final int REQUEST_CODE_SELECT_IMAGE = 5;
    private static final int REQUEST_CODE_AUDIO_PERMISSION = 6;
    private static final int REQUEST_CODE_INTERNET_PERMISSION = 7;

    private LinearLayout layoutMisc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        repository = Repository.getInstance(getApplication());

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editNoteTitle = findViewById(R.id.editNoteTitle);
        editNoteText = findViewById(R.id.editNoteText);
        textDateTime = findViewById(R.id.textViewDateTime);
        textNotificationDateTime = findViewById(R.id.textViewNotificationDateTime);
        textNotification = findViewById(R.id.textViewNotification);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textURL = findViewById(R.id.textURL);
        layoutURL = findViewById(R.id.layoutURL);

        String lang = getIntent().getStringExtra("appLanguage");
        if(lang != null) {
            appLanguage = lang;
        } else {
            appLanguage = "en";
        }

        textDateTime.setText(
                new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date())
        );

        ImageView imageDone = findViewById(R.id.imageDone);
        imageDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        textViewVoiceInput = findViewById(R.id.textViewVoiceInput);
        imageVoiceInput = findViewById(R.id.imageVoiceInput);
        imageVoiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.RECORD_AUDIO},
                            REQUEST_CODE_AUDIO_PERMISSION
                    );
                } else {
                    voiceInput();
                }
            }
        });

        selectedNoteColor = ContextCompat.getColor(
                getApplicationContext(),
                R.color.default_note_color
        );
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            filledNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.imageDeleteURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textURL.setText(null);
                layoutURL.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.imageDeleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageDeleteImage).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });



        initMiscLayout();
        setSubtitleIndicatorColor();

        if(getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                switch (type) {
                    case "image":
                        layoutMisc.findViewById(R.id.layoutAddImage).performClick();
                        break;
                    case "url":
                        layoutMisc.findViewById(R.id.layoutAddURL).performClick();
                        break;
                    case "voice":
                        imageVoiceInput.performClick();
                }
            }
        }
    }

    private void setViewOrUpdateNote() {
        editNoteTitle.setText(filledNote.getTitle());
        editNoteText.setText(filledNote.getNoteText());
        textDateTime.setText(filledNote.getDateTime());
        if (filledNote.getImagePath() != null){
            Bitmap imageFile = BitmapFactory.decodeFile(filledNote.getImagePath());
            if (imageFile != null) {
                imageNote.setImageBitmap(imageFile);
                imageNote.setVisibility(View.VISIBLE);
                findViewById(R.id.imageDeleteImage).setVisibility(View.VISIBLE);
                selectedImagePath = filledNote.getImagePath();
            }
        }

        if (filledNote.getWebLink() != null) {
            textURL.setText(filledNote.getWebLink());
            layoutURL.setVisibility(View.VISIBLE);
        }

        if(filledNote.getNotificationDateTime() != null){
            textNotification.setVisibility(View.VISIBLE);
            textNotificationDateTime.setVisibility(View.VISIBLE);
            textNotificationDateTime.setText(filledNote.getNotificationDateTime());
            SimpleDateFormat smp = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            try {
                notificationDate = smp.parse(filledNote.getNotificationDateTime());
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            }
        }
    }

    private void saveNote() {
        if(editNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.empty_title_hint, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        final Note note = new Note();
        note.setTitle(editNoteTitle.getText().toString());
        note.setNoteText(editNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(getColorStringFromColorInt(selectedNoteColor));

        if (selectedImagePath != null && !selectedImagePath.equals("")) {
            note.setImagePath(selectedImagePath);
        } else {
            note.setImagePath(null);
        }

        if (layoutURL.getVisibility() == View.VISIBLE) {
            note.setWebLink(textURL.getText().toString());
        }

        if(filledNote != null) {
            note.setId(filledNote.getId());
        }

        if(notificationDate != null) {
            note.setNotificationDateTime(
                    new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(notificationDate)
            );
        }

        long id = repository.insertNote(note);
        note.setId(id);
        filledNote = note;
        if (notificationDate != null) {
            setNotification();
        }
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initMiscLayout() {
        layoutMisc = findViewById(R.id.layoutMisc);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        layoutMisc.findViewById(R.id.textViewMisc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 = layoutMisc.findViewById(R.id.imageViewColor1);
        final ImageView imageColor2 = layoutMisc.findViewById(R.id.imageViewColor2);
        final ImageView imageColor3 = layoutMisc.findViewById(R.id.imageViewColor3);
        final ImageView imageColor4 = layoutMisc.findViewById(R.id.imageViewColor4);
        final ImageView imageColor5 = layoutMisc.findViewById(R.id.imageViewColor5);

        layoutMisc.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = ContextCompat.getColor(
                        getApplicationContext(),
                        R.color.default_note_color
                );
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = ContextCompat.getColor(
                        getApplicationContext(),
                        R.color.note_color2
                );
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = ContextCompat.getColor(
                        getApplicationContext(),
                        R.color.note_color3
                );
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = ContextCompat.getColor(
                        getApplicationContext(),
                        R.color.note_color4
                );
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = ContextCompat.getColor(
                        getApplicationContext(),
                        R.color.note_color5
                );
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            }
        });

        if(filledNote != null && filledNote.getColor() != null
                && !filledNote.getColor().trim().isEmpty()) {
            if (filledNote.getColor().equals(getColorStringFromColorInt(ContextCompat.getColor(
                    getApplicationContext(),
                    R.color.note_color2
            )))) {
                layoutMisc.findViewById(R.id.viewColor2).performClick();
            } else if (filledNote.getColor().equals(getColorStringFromColorInt(ContextCompat.getColor(
                    getApplicationContext(),
                    R.color.note_color3
            )))) {
                layoutMisc.findViewById(R.id.viewColor3).performClick();
            } else if (filledNote.getColor().equals(getColorStringFromColorInt(ContextCompat.getColor(
                    getApplicationContext(),
                    R.color.note_color4
            )))) {
                layoutMisc.findViewById(R.id.viewColor4).performClick();
            } else if (filledNote.getColor().equals(getColorStringFromColorInt(ContextCompat.getColor(
                    getApplicationContext(),
                    R.color.note_color5
            )))) {
                layoutMisc.findViewById(R.id.viewColor5).performClick();
            }
        }

        layoutMisc.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        });

        layoutMisc.findViewById(R.id.layoutAddURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        layoutMisc.findViewById(R.id.layoutSetNotification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showSetNotificationDialog();
            }
        });

        if (filledNote != null) {
            layoutMisc.findViewById(R.id.layoutDeleteNoteMisc).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layoutDeleteNoteMisc).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

    }

    private void setNotification() {
        scheduleNotification(getNotification() , notificationDate.getTime());
    }

    private void scheduleNotification (Notification notification , long delay) {
        if (new Date().getTime() < delay) {
            Intent notificationIntent = new Intent(this, NotificationPublisher.class);
            long notification_id;
            if (filledNote != null) {
                notification_id = filledNote.getId();
            } else {
                notification_id = 0;
            }
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notification_id);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            assert alarmManager != null;
            alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
        }
    }

    private Notification getNotification () {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_channel_id);
        builder.setContentTitle(editNoteTitle.getText().toString());
        builder.setContentText(editNoteText.getText().toString());
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(editNoteText.getText().toString()));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, CreateNoteActivity.class);
        resultIntent.putExtra("isViewOrUpdate", true);
        resultIntent.putExtra("note", filledNote);
        resultIntent.putExtra("appLanguage", appLanguage);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        return builder.build() ;
    }

    private void showSetNotificationDialog() {
        if (dialogSetNotification == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_set_notification,
                    (ViewGroup) findViewById(R.id.layoutSetNotificationContainer)
            );
            builder.setView(view);

            dialogSetNotification = builder.create();
            if (dialogSetNotification.getWindow() != null) {
                dialogSetNotification.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            datePickerSetNotification = view.findViewById(R.id.datePickerSetNotification);
            datePickerSetNotification.setMinDate(new Date().getTime());

            timePickerSetNotification = view.findViewById(R.id.timePickerSetNotification);
            timePickerSetNotification.setIs24HourView(true);

            if(notificationDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(notificationDate);
                timePickerSetNotification.setHour(cal.get(Calendar.HOUR_OF_DAY));
                timePickerSetNotification.setMinute(cal.get(Calendar.MINUTE));
                datePickerSetNotification.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }

            view.findViewById(R.id.textViewSetNotificationButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //TODO
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, datePickerSetNotification.getYear());
                    cal.set(Calendar.MONTH, datePickerSetNotification.getMonth());
                    cal.set(Calendar.DAY_OF_MONTH, datePickerSetNotification.getDayOfMonth());
                    cal.set(Calendar.HOUR_OF_DAY, timePickerSetNotification.getHour());
                    cal.set(Calendar.MINUTE, timePickerSetNotification.getMinute());
                    notificationDate = cal.getTime();
                    textNotification.setVisibility(View.VISIBLE);
                    textNotificationDateTime.setVisibility(View.VISIBLE);
                    textNotificationDateTime.setText(
                            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                    .format(notificationDate)
                    );
                    dialogSetNotification.dismiss();
                }
            });

            view.findViewById(R.id.textViewCancelNotificationButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogSetNotification.dismiss();
                }
            });
        }
        dialogSetNotification.show();
    }

    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);

            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textViewDeleteNoteButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                    repository.deleteNote(filledNote);
                    Intent intent = new Intent();
                    intent.putExtra("isNoteDeleted", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

            view.findViewById(R.id.textViewCancelDeleteNoteButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });


        }
        dialogDeleteNote.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void voiceInput() {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale(appLanguage));
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                imageVoiceInput.setImageTintList(getColorStateList(R.color.accent));
                textViewVoiceInput.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                imageVoiceInput.setImageTintList(getColorStateList(R.color.dark_icon_color));
                textViewVoiceInput.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), R.string.listening_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String resultText = editNoteText.getText().toString() + " " + data.get(0);
                editNoteText.setText(resultText);
                imageVoiceInput.setImageTintList(getColorStateList(R.color.dark_icon_color));
                textViewVoiceInput.setVisibility(View.GONE);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_AUDIO_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                voiceInput();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap imageFile = BitmapFactory.decodeStream(inputStream);
                        if (imageFile != null) {
                            imageNote.setImageBitmap(imageFile);
                            imageNote.setVisibility(View.VISIBLE);
                            findViewById(R.id.imageDeleteImage).setVisibility(View.VISIBLE);
                            selectedImagePath = getPathFromUri(selectedImageUri);
                        }
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(selectedNoteColor);
    }

    private String getPathFromUri(Uri uri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        } else {
            cursor.moveToFirst();
            int inx = cursor.getColumnIndex("_data");
            filePath = cursor.getString(inx);
            cursor.close();
        }
        return filePath;
    }

    private String getColorStringFromColorInt(int color_int) {
        return "#" + Integer.toHexString(color_int);
    }

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddURLContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText editURL = view.findViewById(R.id.editURL);
            editURL.requestFocus();

            view.findViewById(R.id.textViewAddURLButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(editURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(
                                CreateNoteActivity.this,
                                R.string.enter_url,
                                Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(editURL.getText().toString()).matches()){
                        Toast.makeText(
                                CreateNoteActivity.this,
                                R.string.enter_valid_url,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        textURL.setText(editURL.getText().toString());
                        layoutURL.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();
                    }

                }
            });

            view.findViewById(R.id.textViewCancelURLButton).setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }

}