package com.gmail.tylerfilla.android.notes.activity;

import java.io.File;
import java.io.IOException;
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
import com.gmail.tylerfilla.android.notes.NoteKeeper;
import com.gmail.tylerfilla.android.notes.R;

public class ActivityEdit extends Activity {
    
    private static Note persistentNote;
    
    private NoteKeeper noteKeeper;
    private NoteEditor noteEditor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.getActionBar().setCustomView(R.layout.activity_edit_actionbar);
        this.setContentView(R.layout.activity_edit);
        
        Note note = null;
        
        if (ActivityEdit.persistentNote != null) {
            note = ActivityEdit.persistentNote;
        } else {
            String noteFilePath = this.getIntent().getDataString();
            
            if (noteFilePath == null) {
                note = Note.blank();
            } else {
                try {
                    note = this.noteKeeper.readNote(new File(noteFilePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (note == null) {
            note = Note.blank();
        }
        
        TextView activityNoteEditActionbarTitle = (TextView) this.findViewById(R.id.activityNoteEditActionbarTitle);
        activityNoteEditActionbarTitle.setText(note.getTitle());
        activityNoteEditActionbarTitle.setSelected(true);
        activityNoteEditActionbarTitle.setOnClickListener(new OnClickListener() {
    
            @Override
            public void onClick(View v) {
                ActivityEdit.this.promptNewTitle();
            }
    
        });
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(note.getTitle(), null, this.getResources().getColor(R.color.task_primary)));
        }
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.activityNoteEditEditor);
        this.noteEditor.setNote(note);
        
        while (!this.noteEditor.getEditorLoaded()) {}
        this.noteEditor.setBackgroundColor(Color.TRANSPARENT);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        Note note = this.noteEditor.getNote();
        if (note != null) {
            try {
                this.noteKeeper.writeNote(note);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (!this.isFinishing()) {
            ActivityEdit.persistentNote = this.noteEditor.getNote();
        }
    }
    
    private void promptNewTitle() {
        AlertDialog.Builder titlePrompt = new AlertDialog.Builder(this);
        
        titlePrompt.setTitle("Edit Title");
        titlePrompt.setMessage("Please enter a new title for this note:");
        
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
                    ActivityEdit.this.noteEditor.getNote().setTitle(title);
                    ((TextView) ActivityEdit.this.findViewById(R.id.activityNoteEditActionbarTitle)).setText(title);
                }
            }
            
        });
        
        titlePrompt.show();
    }
    
    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(this,
                this.findViewById(R.id.activityNoteEditActionbarButtonMenu));
        popupMenu.getMenuInflater().inflate(R.menu.activity_edit_menu, popupMenu.getMenu());
        
        try {
            Field mPopup = PopupMenu.class.getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            mPopup.get(popupMenu).getClass().getMethod("setForceShowIcon", boolean.class)
                    .invoke(mPopup.get(popupMenu), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        popupMenu.show();
    }
    
    public void onActionButtonClick(View view) {
        if ("back".equals(view.getTag())) {
            this.finish();
        } else if ("menu".equals(view.getTag())) {
            this.showMenu();
        }
    }
    
}
