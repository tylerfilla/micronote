package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.gmail.tylerfilla.android.notes.core.NoteKeeper;

public class NotesActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_notes);
        
        // Action bar customization
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle("");
        actionBar.setCustomView(R.layout.actionbar);
        
        this.populateNoteList();
    }
    
    public void buttonNoteNewClicked(View view) {
    }
    
    private void populateNoteList() {
        try {
            for (File noteFile : NoteKeeper.listNoteFiles()) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
