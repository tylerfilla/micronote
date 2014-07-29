package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;
import com.gmail.tylerfilla.android.notes.widget.NoteEditor;

public class NoteEditActivity extends Activity {
    
    private NoteKeeper noteKeeper;
    private NoteEditor noteEditor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.getActionBar().setCustomView(R.layout.activity_note_edit_actionbar);
        this.setContentView(R.layout.activity_note_edit);
        
        Note note = null;
        String noteFilePath = null;
        
        Bundle startingIntentExtras = this.getIntent().getExtras();
        if (startingIntentExtras != null) {
            noteFilePath = startingIntentExtras.getString("file");
        }
        
        if (noteFilePath == null) {
            note = Note.blank();
        } else {
            try {
                note = this.noteKeeper.readNote(new File(noteFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        TextView actionbarActivityNoteEditTitle = (TextView) this
                .findViewById(R.id.activityNoteEditActionbarTitle);
        actionbarActivityNoteEditTitle.setText(note.getTitle());
        actionbarActivityNoteEditTitle.setSelected(true);
        actionbarActivityNoteEditTitle.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                NoteEditActivity.this.editNoteTitle();
            }
            
        });
        
        TextView activityNoteEditEditorHeader = (TextView) this
                .findViewById(R.id.activityNoteEditEditorHeader);
        if (note.getFile() == null) {
            activityNoteEditEditorHeader.setText("New note");
        } else {
            DateFormat dateFormatLastModified = DateFormat.getDateInstance();
            activityNoteEditEditorHeader.setText(String.valueOf(dateFormatLastModified
                    .format(new Date(note.getFile().lastModified()))));
        }
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.activityNoteEditEditor);
        this.noteEditor.setNote(note);
        
        if (note.getFile() == null) {
            this.noteEditor.requestFocus();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        this.noteEditor.clearComposingText();
        Note note = this.noteEditor.getNote();
        if (note != null && note.getContent() != null && !note.getContent().isEmpty()) {
            try {
                this.noteKeeper.writeNote(note);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void editNoteTitle() {
        AlertDialog.Builder titlePrompt = new AlertDialog.Builder(this);
        
        titlePrompt.setTitle("Edit Title");
        titlePrompt.setMessage("Please enter a new title for this note:");
        
        final EditText titlePromptInput = new EditText(this);
        titlePromptInput.setMaxLines(1);
        titlePromptInput.setHint(this.noteEditor.getNote().getTitle());
        titlePromptInput.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        titlePrompt.setView(titlePromptInput);
        
        titlePrompt.setNegativeButton("Cancel", null);
        titlePrompt.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = titlePromptInput.getText().toString();
                
                if (!title.isEmpty()) {
                    NoteEditActivity.this.noteEditor.getNote().setTitle(title);
                    ((TextView) NoteEditActivity.this
                            .findViewById(R.id.activityNoteEditActionbarTitle)).setText(title);
                }
            }
            
        });
        
        titlePrompt.show();
    }
    
    public void buttonActionClicked(View view) {
        if ("back".equals(view.getTag())) {
            this.finish();
        }
    }
    
}
