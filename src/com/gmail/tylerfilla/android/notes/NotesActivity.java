package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;
import com.gmail.tylerfilla.android.notes.view.NoteListEntry;

public class NotesActivity extends Activity {
    
    private NoteKeeper noteKeeper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.setContentView(R.layout.activity_notes);
        this.getActionBar().setCustomView(R.layout.actionbar_activity_notes);
        
        this.refreshNoteList();
    }
    
    private void refreshNoteList() {
        LinearLayout noteListLayout = (LinearLayout) this.findViewById(R.id.noteListLayout);
        ScrollView noteListScroll = (ScrollView) this.findViewById(R.id.noteListScroll);
        LinearLayout noteListEmpty = (LinearLayout) this.findViewById(R.id.noteListEmpty);
        ImageButton buttonActionSearch = (ImageButton) this.findViewById(R.id.buttonActionSearch);
        
        noteListLayout.removeAllViews();
        
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
                    
                    noteListEntry.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            noteListEntryClicked(noteListEntry);
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
    
    public void buttonActionClicked(View view) {
        if ("settings".equals(view.getTag())) {
            Toast.makeText(this, "Settings not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("search".equals(view.getTag())) {
            Toast.makeText(this, "Search not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("new".equals(view.getTag())) {
            this.enterNoteEditor(null);
        }
    }
    
    public void noteListEntryClicked(NoteListEntry noteListEntry) {
        this.enterNoteEditor(noteListEntry.getNote().getFile());
    }
    
    private void enterNoteEditor(File noteFile) {
        Intent noteEditIntent = new Intent("com.gmail.tylerfilla.android.notes.ACTION_EDIT_NOTE");
        
        if (noteFile != null) {
            noteEditIntent.putExtra("noteFilePath", noteFile.getAbsolutePath());
        }
        
        this.startActivity(noteEditIntent);
    }
    
}
