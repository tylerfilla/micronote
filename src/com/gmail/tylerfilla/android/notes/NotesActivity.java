package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;
import com.gmail.tylerfilla.android.notes.view.NoteListEntry;

public class NotesActivity extends Activity {
    
    private NoteKeeper noteKeeper;
    private HashSet<NoteListEntry> noteListEntrySet;
    private boolean selectionMode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        this.noteListEntrySet = new HashSet<NoteListEntry>();
        this.selectionMode = false;
        
        this.setContentView(R.layout.activity_notes);
        this.getActionBar().setCustomView(R.layout.actionbar_activity_notes);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        this.refreshNoteList();
    }
    
    private void refreshNoteList() {
        LinearLayout noteListLayout = (LinearLayout) this.findViewById(R.id.noteListLayout);
        ScrollView noteListScroll = (ScrollView) this.findViewById(R.id.noteListScroll);
        LinearLayout noteListEmpty = (LinearLayout) this.findViewById(R.id.noteListEmpty);
        ImageButton buttonActionSearch = (ImageButton) this
                .findViewById(R.id.actionbarActivityButtonSearch);
        
        noteListLayout.removeAllViews();
        this.noteListEntrySet.clear();
        
        File[] noteFiles = this.noteKeeper.listNoteFiles();
        
        if (noteFiles.length > 0) {
            noteListEmpty.setVisibility(View.GONE);
            
            for (File noteFile : noteFiles) {
                Note note = null;
                
                try {
                    note = this.noteKeeper.readNote(noteFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                if (note != null) {
                    final NoteListEntry noteListEntry = new NoteListEntry(this);
                    noteListEntry.setNote(note);
                    
                    this.noteListEntrySet.add(noteListEntry);
                    
                    noteListEntry.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            noteListEntryClicked(noteListEntry);
                        }
                        
                    });
                    
                    noteListEntry.setLongClickable(true);
                    noteListEntry.setOnLongClickListener(new OnLongClickListener() {
                        
                        @Override
                        public boolean onLongClick(View v) {
                            noteListEntryLongClicked(noteListEntry);
                            return true;
                        }
                        
                    });
                    
                    View noteListEntryDivider = new View(this);
                    noteListEntryDivider.setBackgroundResource(R.color.pad_line);
                    noteListEntryDivider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 3));
                    
                    noteListLayout.addView(noteListEntry);
                    noteListLayout.addView(noteListEntryDivider);
                }
            }
            
            noteListScroll.invalidate();
            noteListScroll.requestLayout();
        } else {
            noteListEmpty.setVisibility(View.VISIBLE);
        }
        
        if (noteFiles.length >= 2) {
            buttonActionSearch.setVisibility(View.VISIBLE);
        } else {
            buttonActionSearch.setVisibility(View.GONE);
        }
    }
    
    private void selectionModeEnter() {
        this.selectionMode = true;
        
        for (NoteListEntry noteListEntry : this.noteListEntrySet) {
            noteListEntry.setSelected(false);
            noteListEntry.setLongClickable(true);
        }
        
        this.findViewById(R.id.actionbarActivityButtonSettings).setVisibility(View.GONE);
        this.findViewById(R.id.actionbarActivityButtonSearch).setVisibility(View.GONE);
        this.findViewById(R.id.actionbarActivityButtonNoteNew).setVisibility(View.GONE);
        
        this.findViewById(R.id.actionbarActivityButtonSelectDone).setVisibility(View.VISIBLE);
        this.findViewById(R.id.actionbarActivityButtonSelectDelete).setVisibility(View.VISIBLE);
        
        this.selectionModeUpdate();
    }
    
    private void selectionModeUpdate() {
        TextView actionbarActivityNotesTitle = (TextView) this
                .findViewById(R.id.actionbarActivityNotesTitle);
        
        if (this.selectionMode) {
            int count = 0;
            for (NoteListEntry noteListEntry : this.noteListEntrySet) {
                if (noteListEntry.getSelected()) {
                    count++;
                }
            }
            
            actionbarActivityNotesTitle.setText(count + " selected");
        } else {
            actionbarActivityNotesTitle.setText(this
                    .getString(R.string.actionbar_activity_notes_title));
        }
    }
    
    private void selectionModeExit() {
        this.selectionMode = false;
        
        for (NoteListEntry noteListEntry : this.noteListEntrySet) {
            noteListEntry.setSelected(false);
            noteListEntry.setLongClickable(true);
        }
        
        this.findViewById(R.id.actionbarActivityButtonSettings).setVisibility(View.VISIBLE);
        this.findViewById(R.id.actionbarActivityButtonSearch).setVisibility(View.VISIBLE);
        this.findViewById(R.id.actionbarActivityButtonNoteNew).setVisibility(View.VISIBLE);
        
        this.findViewById(R.id.actionbarActivityButtonSelectDone).setVisibility(View.GONE);
        this.findViewById(R.id.actionbarActivityButtonSelectDelete).setVisibility(View.GONE);
        
        this.selectionModeUpdate();
    }
    
    public void buttonActionClicked(View view) {
        if ("settings".equals(view.getTag())) {
            Toast.makeText(this, "Settings not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("search".equals(view.getTag())) {
            Toast.makeText(this, "Search not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("new".equals(view.getTag())) {
            this.enterNoteEditor(null);
        } else if ("select_done".equals(view.getTag())) {
            this.selectionModeExit();
        } else if ("select_delete".equals(view.getTag())) {
            // TODO: Delete notes represented by selection
            this.selectionModeExit();
        }
    }
    
    public void noteListEntryClicked(NoteListEntry noteListEntry) {
        if (this.selectionMode) {
            noteListEntry.setSelected(!noteListEntry.getSelected());
            this.selectionModeUpdate();
        } else {
            this.enterNoteEditor(noteListEntry.getNote().getFile());
        }
    }
    
    public void noteListEntryLongClicked(NoteListEntry noteListEntry) {
        if (!this.selectionMode) {
            this.selectionModeEnter();
            noteListEntry.setSelected(true);
        }
    }
    
    private void enterNoteEditor(File noteFile) {
        Intent noteEditIntent = new Intent("com.gmail.tylerfilla.android.notes.ACTION_EDIT_NOTE");
        
        if (noteFile != null) {
            noteEditIntent.putExtra("noteFilePath", noteFile.getAbsolutePath());
        }
        
        this.startActivity(noteEditIntent);
    }
    
}
