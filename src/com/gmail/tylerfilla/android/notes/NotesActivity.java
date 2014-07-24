package com.gmail.tylerfilla.android.notes;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class NotesActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_notes);
        
        // Action bar customization
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle("");
        actionBar.setCustomView(R.layout.actionbar);
    }
    
    public void buttonNoteNewClicked(View view) {
    }
    
}
