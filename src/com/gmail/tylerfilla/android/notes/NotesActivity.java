package com.gmail.tylerfilla.android.notes;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;

public class NotesActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_notes);
        
        // Action bar
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle("");
        actionBar.setCustomView(R.layout.actionbar);
        
        // Scan for notes and add entries to notes list
        this.populateNoteList();
    }
    
    private void populateNoteList() {
        ScrollView notesListScroll = (ScrollView) this.findViewById(R.id.notesListScroll);
        LinearLayout notesListLayout = (LinearLayout) this.findViewById(R.id.notesListLayout);
        
        notesListLayout.removeAllViews();
        
        for (File noteFile : NoteKeeper.listNoteFiles()) {
            LinearLayout listEntry = new LinearLayout(this);
            listEntry.setBackgroundResource(R.drawable.background_notes_list_entry);
            listEntry.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            listEntry.setOrientation(LinearLayout.VERTICAL);
            listEntry.setPadding(20, 20, 20, 20);
            listEntry.setLongClickable(true);
            
            TextView listEntryTitle = new TextView(this);
            listEntryTitle.setTextAppearance(this, R.style.AppThemeNotesListEntryTitleText);
            listEntryTitle.setSingleLine();
            listEntryTitle.setEllipsize(TruncateAt.END);
            
            TextView listEntryPreview = new TextView(this);
            listEntryPreview.setTextAppearance(this, R.style.AppThemeNotesListEntryPreviewText);
            listEntryPreview.setSingleLine();
            listEntryPreview.setEllipsize(TruncateAt.END);
            
            ImageView listEntryDivider = new ImageView(this);
            listEntryDivider.setBackgroundResource(R.color.pad_line);
            listEntryDivider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 3));
            
            try {
                final Note note = NoteKeeper.readNoteFile(noteFile);
                
                if (note != null) {
                    listEntryTitle.setText(note.getTitle());
                    listEntryPreview.setText(this.generateNoteContentPreview(note.getContent()));
                    
                    listEntry.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            noteListEntryClicked(v, note);
                        }
                        
                    });
                    listEntry.setOnLongClickListener(new OnLongClickListener() {
                        
                        @Override
                        public boolean onLongClick(View v) {
                            noteListEntryLongClicked(v, note);
                            return true;
                        }
                        
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                
                listEntryTitle.setText(noteFile.getName());
                listEntryPreview.setText("Unable to read note: " + e.getMessage());
            }
            
            listEntry.addView(listEntryTitle);
            listEntry.addView(listEntryPreview);
            
            notesListLayout.addView(listEntry);
            notesListLayout.addView(listEntryDivider);
        }
        
        notesListScroll.invalidate();
        notesListScroll.requestLayout();
    }
    
    public String generateNoteContentPreview(String noteContent) {
        String preview = "";
        
        boolean escaped = false;
        boolean bracketed = false;
        
        for (int ci = 0; ci < noteContent.length(); ci++) {
            char c = noteContent.charAt(ci);
            
            if (bracketed) {
                if (c == ']') {
                    bracketed = false;
                }
                continue;
            }
            
            if (escaped) {
                preview += c;
            } else {
                if (c == '[') {
                    bracketed = true;
                } else if (c == '\\') {
                    escaped = true;
                } else {
                    preview += c;
                }
            }
        }
        
        return preview;
    }
    
    public void buttonActionClicked(View view) {
    }
    
    public void noteListEntryClicked(View view, Note note) {
    }
    
    public void noteListEntryLongClicked(View view, Note note) {
    }
    
}
