package com.gmail.tylerfilla.android.notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.view.NoteEditor;

public class NoteEditActivity extends Activity {
    
    private NoteEditor noteEditor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_note_edit);
        
        ActionBar actionBar = this.getActionBar();
        actionBar.setCustomView(R.layout.actionbar_activity_note_edit);
        
        ((TextView) this.findViewById(R.id.actionbarActivityNoteEditTitle)).setText("Hello");
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.noteEditor);
    }
    
    public void buttonActionClicked(View view) {
        if ("settings".equals(view.getTag())) {
            Toast.makeText(this, "Settings not implemented", Toast.LENGTH_SHORT).show();
        } else if ("search".equals(view.getTag())) {
            Toast.makeText(this, "Search not implemented", Toast.LENGTH_SHORT).show();
        } else if ("new_note".equals(view.getTag())) {
            this.startActivity(new Intent("com.gmail.tylerfilla.android.notes.ACTION_EDIT_NOTE"));
        }
    }
    
}
