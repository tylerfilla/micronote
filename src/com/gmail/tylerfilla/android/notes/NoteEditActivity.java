package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;
import com.gmail.tylerfilla.android.notes.view.NoteEditor;

public class NoteEditActivity extends Activity {
    
    private static int AUTOSAVE_PERIOD = 5000;
    
    private NoteEditor noteEditor;
    private Note note;
    
    private Handler autosaveHandler;
    private Runnable autosaveHandlerRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_note_edit);
        
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirNotes = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "Notes");
            File dirCache = new File(this.getExternalCacheDir().getAbsolutePath(), "Notes");
            
            dirNotes.mkdir();
            dirCache.mkdir();
            
            NoteKeeper.setDirectories(dirNotes.getAbsolutePath(), dirCache.getAbsolutePath());
        } else {
            File dirNotes = new File(this.getFilesDir(), "Notes");
            File dirCache = new File(this.getFilesDir(), "TempMedia");
            
            dirNotes.mkdir();
            dirCache.mkdir();
            
            NoteKeeper.setDirectories(dirNotes.getAbsolutePath(), dirCache.getAbsolutePath());
        }
        
        String noteFilePath = null;
        
        Bundle startingIntentExtras = this.getIntent().getExtras();
        if (startingIntentExtras != null) {
            noteFilePath = startingIntentExtras.getString("noteFilePath");
        }
        
        if (noteFilePath == null) {
            this.note = NoteKeeper.createBlankNote();
        } else {
            try {
                this.note = NoteKeeper.readNoteFile(new File(noteFilePath));
            } catch (IOException e) {
                e.printStackTrace();
                this.finish();
            }
        }
        
        ActionBar actionBar = this.getActionBar();
        actionBar.setCustomView(R.layout.actionbar_activity_note_edit);
        
        ((TextView) this.findViewById(R.id.actionbarActivityNoteEditTitle)).setText(this.note
                .getTitle());
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.noteEditor);
        
        if (this.note.getContent() != null) {
            this.noteEditor.setNote(this.note);
        }
        
        final int autosavePeriodFinal = AUTOSAVE_PERIOD;
        final Handler autosaveHandler = new Handler();
        autosaveHandler.postDelayed((this.autosaveHandlerRunnable = new Runnable() {
            
            @Override
            public void run() {
                NoteEditActivity.this.noteEditor.writeNoteContent();
                
                try {
                    NoteKeeper.writeNote(NoteEditActivity.this.note);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(NoteEditActivity.this, "Autosave failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                
                autosaveHandler.postDelayed(this, autosavePeriodFinal);
            }
            
        }), AUTOSAVE_PERIOD);
        this.autosaveHandler = autosaveHandler;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        this.autosaveHandler.removeCallbacks(this.autosaveHandlerRunnable);
        
        this.noteEditor.writeNoteContent();
        try {
            NoteKeeper.writeNote(this.note);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void buttonActionClicked(View view) {
        if ("back".equals(view.getTag())) {
            this.finish();
        } else if ("more".equals(view.getTag())) {
            InputMethodManager inputMethodManager = (InputMethodManager) this
                    .getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
}
