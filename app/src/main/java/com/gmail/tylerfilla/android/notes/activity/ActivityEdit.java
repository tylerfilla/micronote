package com.gmail.tylerfilla.android.notes.activity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

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
        
        TextView activityNoteEditActionbarTitle = (TextView) this
                .findViewById(R.id.activityNoteEditActionbarTitle);
        activityNoteEditActionbarTitle.setText(note.getTitle());
        activityNoteEditActionbarTitle.setSelected(true);
        activityNoteEditActionbarTitle.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ActivityEdit.this.editNoteTitle();
            }
            
        });
        
        this.noteEditor = (NoteEditor) this.findViewById(R.id.activityNoteEditEditor);
        this.noteEditor.setNote(note);
        
        this.noteEditor.setResponder(new NoteEditor.Responder() {
            
            @Override
            public void onExternalRequest(String request) {
                ActivityEdit.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(request)));
            }
            
            @Override
            public void onPageLoad(String url) {
                ActivityEdit.this.findViewById(R.id.activityNoteEditEditorCover).setVisibility(
                        View.GONE);
            }
            
            @Override
            public void onUpdateIndentControlState(boolean controlActive, boolean enableDecrease,
                    boolean enableIncrease) {
                ((LinearLayout) ActivityEdit.this
                        .findViewById(R.id.activityNoteEditIndentControl))
                        .setVisibility(controlActive ? View.VISIBLE : View.GONE);
                ((ImageButton) ActivityEdit.this
                        .findViewById(R.id.activityNoteEditIndentControlButtonDecrease))
                        .setEnabled(enableDecrease);
                ((ImageButton) ActivityEdit.this
                        .findViewById(R.id.activityNoteEditIndentControlButtonIncrease))
                        .setEnabled(enableIncrease);
            }
            
        });
        /*
         * final View activityNoteEditView = this.findViewById(android.R.id.content);
         * activityNoteEditView.getViewTreeObserver().addOnGlobalLayoutListener( new
         * OnGlobalLayoutListener() {
         * 
         * @Override public void onGlobalLayout() { if
         * (activityNoteEditView.getRootView().getHeight() - activityNoteEditView.getHeight() > 400)
         * { NoteEditActivity.this.noteEditor.performAction(NoteEditor.Action.FOCUS); } else {
         * NoteEditActivity.this.noteEditor.performAction(NoteEditor.Action.BLUR); }
         * 
         * NoteEditActivity.this.noteEditor.postInvalidate(); }
         * 
         * });
         */
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
                    ActivityEdit.this.noteEditor.getNote().setTitle(title);
                    ((TextView) ActivityEdit.this
                            .findViewById(R.id.activityNoteEditActionbarTitle)).setText(title);
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
                ActivityEdit.this.buttonMenuClicked(item);
                return true;
            }
            
        });
        
        popupMenu.show();
    }
    
    public void onActionButtonClick(View view) {
        if ("back".equals(view.getTag())) {
            this.finish();
        } else if ("menu".equals(view.getTag())) {
            this.showMenu();
        }
    }
    
    public void onIndentControlButtonClick(View view) {
        if ("increase".equals(view.getTag())) {
            this.noteEditor.performAction(NoteEditor.Action.INDENT_INCREASE);
        } else if ("decrease".equals(view.getTag())) {
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
            this.noteEditor.performAction(NoteEditor.Action.CREATE_LIST_CHECKBOX);
            break;
        case R.id.activityNoteEditMenuClip:
            Toast.makeText(this, "Clipping not yet implemented", Toast.LENGTH_SHORT).show();
            break;
        }
    }
    
}
