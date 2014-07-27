package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
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
    
    private NoteKeeper noteKeeper;
    
    private NoteEditor noteEditor;
    private Note note;
    
    private Handler autosaveHandler;
    private Runnable autosaveHandlerRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.setContentView(R.layout.activity_note_edit);
        this.getActionBar().setCustomView(R.layout.actionbar_activity_note_edit);
        
        String noteFilePath = null;
        
        Bundle startingIntentExtras = this.getIntent().getExtras();
        if (startingIntentExtras != null) {
            noteFilePath = startingIntentExtras.getString("noteFilePath");
        }
        
        if (noteFilePath == null) {
            this.note = new Note();
        } else {
            try {
                this.note = this.noteKeeper.readNote(new File(noteFilePath));
            } catch (IOException e) {
                e.printStackTrace();
                this.finish();
            }
        }
        
        ((TextView) this.findViewById(R.id.actionbarActivityNoteEditTitle)).setText(this.note
                .getTitle());
        ((TextView) this.findViewById(R.id.actionbarActivityNoteEditTitle)).setSelected(true);
        
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
                
                if (NoteEditActivity.this.note.getChanged()) {
                    try {
                        NoteEditActivity.this.noteKeeper.writeNote(NoteEditActivity.this.note);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(NoteEditActivity.this, "Autosave failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
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
        if (this.note != null) {
            try {
                this.noteKeeper.writeNote(this.note);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
