package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

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
        
        if (this.noteKeeper.hasPersistentNote()) {
            note = this.noteKeeper.popPersistentNote();
        } else {
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
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.activityNoteEditEditor);
        this.noteEditor.setNote(note);
        
        this.noteEditor.setResponder(new NoteEditor.Responder() {
            
            @Override
            public void onIndentControlActive(boolean active) {
                if (active) {
                    NoteEditActivity.this.findViewById(R.id.activityNoteEditIndentControl)
                            .setVisibility(View.VISIBLE);
                } else {
                    NoteEditActivity.this.findViewById(R.id.activityNoteEditIndentControl)
                            .setVisibility(View.GONE);
                }
            }
            
        });
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
            this.noteKeeper.pushPersistentNote(this.noteEditor.getNote());
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
    
    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(this,
                this.findViewById(R.id.activityNoteEditActionbarButtonMenu));
        popupMenu.getMenuInflater().inflate(R.menu.activity_note_edit_menu, popupMenu.getMenu());
        
        try {
            Field mPopup = PopupMenu.class.getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            mPopup.get(popupMenu).getClass().getMethod("setForceShowIcon", boolean.class)
                    .invoke(mPopup.get(popupMenu), true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                NoteEditActivity.this.buttonMenuClicked(item);
                return true;
            }
            
        });
        
        popupMenu.show();
    }
    
    public void buttonActionClicked(View view) {
        if ("back".equals(view.getTag())) {
            this.finish();
        } else if ("menu".equals(view.getTag())) {
            this.showMenu();
        }
    }
    
    public void buttonEditClicked(View view) {
        if ("indent_increase".equals(view.getTag())) {
            this.noteEditor.performAction(NoteEditor.Action.INDENT_INCREASE);
        } else if ("indent_decrease".equals(view.getTag())) {
            this.noteEditor.performAction(NoteEditor.Action.INDENT_DECREASE);
        }
    }
    
    private void buttonMenuClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.activityNoteEditMenuUL:
            this.noteEditor.performAction(NoteEditor.Action.CREATE_LIST_BULLET);
            break;
        case R.id.activityNoteEditMenuOL:
            this.noteEditor.performAction(NoteEditor.Action.CREATE_LIST_NUMBER);
            break;
        case R.id.activityNoteEditMenuChecklist:
            Toast.makeText(this, "Checklists not yet implemented", Toast.LENGTH_SHORT).show();
            break;
        case R.id.activityNoteEditMenuClip:
            Toast.makeText(this, "Clipping not yet implemented", Toast.LENGTH_SHORT).show();
            break;
        }
    }
    
}
