package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;
import com.gmail.tylerfilla.android.notes.view.NoteEditor;

public class NoteEditActivity extends Activity {
    
    private NoteKeeper noteKeeper;
    private NoteEditor noteEditor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.setContentView(R.layout.activity_note_edit);
        this.getActionBar().setCustomView(R.layout.actionbar_activity_note_edit);
        
        Note note = null;
        String noteFilePath = null;
        
        Bundle startingIntentExtras = this.getIntent().getExtras();
        if (startingIntentExtras != null) {
            noteFilePath = startingIntentExtras.getString("noteFilePath");
        }
        
        if (noteFilePath == null) {
            note = Note.createBlank();
        } else {
            try {
                note = this.noteKeeper.readNote(new File(noteFilePath));
            } catch (IOException e) {
                e.printStackTrace();
                this.finish();
            }
        }
        
        TextView actionbarActivityNoteEditTitle = (TextView) this
                .findViewById(R.id.actionbarActivityNoteEditTitle);
        actionbarActivityNoteEditTitle.setText(note.getTitle());
        actionbarActivityNoteEditTitle.setSelected(true);
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.noteEditor);
        this.noteEditor.setNote(note);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        try {
            this.noteKeeper.writeNote(this.noteEditor.getNote());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void buttonActionClicked(View view) {
        if ("back".equals(view.getTag())) {
            InputMethodManager inputMethodManager = (InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            
            this.finish();
        }
    }
    
}
