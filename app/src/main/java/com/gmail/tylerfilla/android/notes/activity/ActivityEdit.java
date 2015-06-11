package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.NoteEditor;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.io.NoteIO;

import java.io.File;
import java.io.IOException;

public class ActivityEdit extends Activity {
    
    private File noteFile;
    
    private NoteEditor noteEditor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* Handle intent */
        
        // Note to be edited
        Note note = null;
        
        // Attempt to read note described by intent
        Uri noteFileUri = this.getIntent().getData();
        if (noteFileUri == null) {
            // Create a new file for the note
            this.noteFile = new File(new File(this.getFilesDir(), "notes"), "_" + String.valueOf(System.currentTimeMillis()) + ".note");
            this.noteFile.getParentFile().mkdirs();
            
            // Create a new note
            note = new Note();
            
            // Write data into intent for orientation changes
            this.getIntent().setData(Uri.fromFile(this.noteFile));
        } else {
            // Get the note file
            this.noteFile = new File(noteFileUri.getPath());
            
            // Try to read note from file
            if (this.noteFile.exists()) {
                try {
                    note = NoteIO.read(this.noteFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                note = new Note();
            }
        }
        
        /* Layout and appearance */
        
        this.getActionBar().setCustomView(R.layout.activity_edit_actionbar);
        this.setContentView(R.layout.activity_edit);
        
        String noteTitle = note.getTitle();
        
        // Initialize actionbar title
        TextView activityEditActionbarTitle = (TextView) this.findViewById(R.id.activityEditActionbarTitle);
        activityEditActionbarTitle.setText(noteTitle);
        activityEditActionbarTitle.setSelected(true);
        activityEditActionbarTitle.setOnClickListener(new OnClickListener() {
    
            @Override
            public void onClick(View v) {
                ActivityEdit.this.promptNewTitle();
            }
    
        });
        
        // Set task description
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(noteTitle, null, this.getResources().getColor(R.color.task_primary)));
        }
        
        /* Initialize editor */
        
        // Get editor reference
        this.noteEditor = (NoteEditor) this.findViewById(R.id.activityEditEditor);
        
        // Set transparent background
        this.noteEditor.setBackgroundColor(Color.TRANSPARENT);
        
        // Pass note to editor
        this.noteEditor.setNote(note);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Resume editor
        this.noteEditor.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Pause editor
        this.noteEditor.onPause();
    
        // Write note if changed
        if (this.noteEditor.getNote().getChanged()) {
            try {
                NoteIO.write(this.noteEditor.getNote(), this.noteFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void promptNewTitle() {
        AlertDialog.Builder titlePrompt = new AlertDialog.Builder(this);
        
        titlePrompt.setTitle("Edit Title");
        titlePrompt.setMessage("Please enter a new title below.");
        
        final EditText titlePromptInput = new EditText(this);
        titlePromptInput.setMaxLines(1);
        titlePromptInput.setHint(this.noteEditor.getNote().getTitle());
        titlePromptInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        titlePrompt.setView(titlePromptInput);
        
        titlePrompt.setNegativeButton("Cancel", null);
        titlePrompt.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = titlePromptInput.getText().toString();
        
                if (!title.isEmpty()) {
                    ActivityEdit.this.handleNoteTitleUpdate(title);
                }
            }
    
        });
        
        titlePrompt.show();
    }
    
    private void handleNoteTitleUpdate(String title) {
        // Set note title
        this.noteEditor.getNote().setTitle(title);
        
        // Set actionbar title
        ((TextView) ActivityEdit.this.findViewById(R.id.activityEditActionbarTitle)).setText(title);
        
        // Update task description
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(title, null, this.getResources().getColor(R.color.task_primary)));
        }
    }
    
    private void handleClose() {
        // Lollipop gets special treatment here...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.finishAndRemoveTask();
            return;
        }
        
        // Everyone else finishes normally
        this.finish();
    }
    
    /*
    
    private void handleMenu() {
        PopupMenu popupMenu = new PopupMenu(this, this.findViewById(R.id.activityEditActionbarButtonMenu));
        popupMenu.getMenuInflater().inflate(R.menu.activity_edit_menu, popupMenu.getMenu());
        
        // Gettin' a bit hacky up in here...
        try {
            Field mPopup = PopupMenu.class.getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            mPopup.get(popupMenu).getClass().getMethod("setForceShowIcon", boolean.class).invoke(mPopup.get(popupMenu), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        popupMenu.show();
    }
    
    */
    
    public void onActionButtonClick(View view) {
        if ("close".equals(view.getTag())) {
            this.handleClose();
        } else if ("menu".equals(view.getTag())) {
            //this.handleMenu();
        }
    }
    
}
