package com.gmail.tylerfilla.android.notes;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NoteEditActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_notes_edit);
        
        Log.d("", this.getIntent().getExtras().getString("noteFilePath"));
    }
    
}
