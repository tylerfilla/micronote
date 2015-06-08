package com.gmail.tylerfilla.android.notes.activity;

import java.lang.reflect.Field;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.NoteEditor;
import com.gmail.tylerfilla.android.notes.R;

public class ActivityEdit extends Activity {
    
    private NoteEditor noteEditor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* Handle intent */
        
        // Note to be edited
        Note note = null;
        
        // Handle intent data input
        String noteFilePath = this.getIntent().getDataString();
        if (noteFilePath == null) {
            note = Note.create();
        } else {
            // TODO: Load note from file
            note = Note.create();
        }
        
        /* Handle layout and appearance */
    
        this.getActionBar().setCustomView(R.layout.activity_edit_actionbar);
        this.setContentView(R.layout.activity_edit);
        
        String noteTitle = note.getTitle();
        
        // Initialize actionbar title
        TextView activityNoteEditActionbarTitle = (TextView) this.findViewById(R.id.activityNoteEditActionbarTitle);
        activityNoteEditActionbarTitle.setText(noteTitle);
        activityNoteEditActionbarTitle.setSelected(true);
        activityNoteEditActionbarTitle.setOnClickListener(new OnClickListener() {
    
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
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.activityNoteEditEditor);
        this.noteEditor.setNote(note);
        
        while (!this.noteEditor.getEditorLoaded()) {}
        this.noteEditor.setBackgroundColor(Color.TRANSPARENT);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        this.noteEditor.onPause();
    }
    
    @Override
    public void onResume() {
        super.onPause();
    
        this.noteEditor.onResume();
    }
    
    private void promptNewTitle() {
        AlertDialog.Builder titlePrompt = new AlertDialog.Builder(this);
        
        titlePrompt.setTitle("Edit Title");
        titlePrompt.setMessage("Enter new title:");
        
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
                    ActivityEdit.this.handleTitleChange(title);
                }
            }
            
        });
        
        titlePrompt.show();
    }
    
    private void handleTitleChange(String title) {
        // Set note title
        this.noteEditor.getNote().setTitle(title);
        
        // Set actionbar title
        ((TextView) ActivityEdit.this.findViewById(R.id.activityNoteEditActionbarTitle)).setText(title);
        
        // Update task description
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(title, null, this.getResources().getColor(R.color.task_primary)));
        }
    }
    
    private void handleClose() {
        // Lollipop gets special treatment here...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.finishAndRemoveTask();
        }
        
        // Finish normally
        this.finish();
    }
    
    private void handleMenu() {
        PopupMenu popupMenu = new PopupMenu(this,
                this.findViewById(R.id.activityNoteEditActionbarButtonMenu));
        popupMenu.getMenuInflater().inflate(R.menu.activity_edit_menu, popupMenu.getMenu());
        
        // Gettin' a bit hacky up in here...
        try {
            Field mPopup = PopupMenu.class.getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            mPopup.get(popupMenu).getClass().getMethod("setForceShowIcon", boolean.class).invoke(mPopup.get(popupMenu), true);
        } catch (Exception e) {
        }
        
        popupMenu.show();
    }
    
    public void onActionButtonClick(View view) {
        if (view.getTag() == null) {
            return;
        }
        
        // Delegate to appropriate handler
        if (view.getTag().equals("close")) {
            this.handleClose();
        } else if (view.getTag().equals("menu")) {
            this.handleMenu();
        }
    }
    
}
