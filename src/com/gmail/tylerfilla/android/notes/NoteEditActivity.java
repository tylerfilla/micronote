package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;
import com.gmail.tylerfilla.android.notes.view.NoteEditor;

public class NoteEditActivity extends Activity {
    
    private NoteEditor noteEditor;
    private Note note;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_note_edit);
        
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
                return;
            }
        }
        
        ActionBar actionBar = this.getActionBar();
        actionBar.setCustomView(R.layout.actionbar_activity_note_edit);
        
        ((TextView) this.findViewById(R.id.actionbarActivityNoteEditTitle)).setText(this.note
                .getTitle());
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.noteEditor);
    }
    
    public void buttonActionClicked(View view) {
        if ("back".equals(view.getTag())) {
            this.finish();
        } else if ("clip".equals(view.getTag())) {
            Toast.makeText(this, "Clip media to note", Toast.LENGTH_SHORT).show();
        }
    }
    
}
