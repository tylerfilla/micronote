package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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
                    NoteListEntry noteListEntry = new NoteListEntry(this);
                    noteListEntry.setNote(note);
                    
                    this.buildNoteListEntry(noteListEntry);
                    
                    ImageView noteListEntryDivider = new ImageView(this);
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
    
    public void buildNoteListEntry(final NoteListEntry noteListEntry) {
        Note note = noteListEntry.getNote();
        
        noteListEntry.setBackgroundResource(R.drawable.background_notes_list_entry);
        noteListEntry.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        noteListEntry.setOrientation(LinearLayout.VERTICAL);
        noteListEntry.setPadding(24, 36, 24, 36);
        noteListEntry.setLongClickable(true);
        
        TextView listEntryTitle = new TextView(this);
        listEntryTitle.setTextAppearance(this, R.style.AppThemeNoteListEntryTitleText);
        listEntryTitle.setSingleLine();
        listEntryTitle.setEllipsize(TruncateAt.END);
        
        TextView listEntryPreview = new TextView(this);
        listEntryPreview.setTextAppearance(this, R.style.AppThemeNoteListEntryPreviewText);
        listEntryPreview.setSingleLine();
        listEntryPreview.setEllipsize(TruncateAt.END);
        
        listEntryTitle.setText(note.getTitle());
        listEntryPreview.setText(Html.fromHtml(note.getContent()));
        
        noteListEntry.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                noteListEntryClicked(noteListEntry);
            }
            
        });
        noteListEntry.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO: Enter multi-select mode
                return true;
            }
            
        });
        
        noteListEntry.addView(listEntryTitle);
        noteListEntry.addView(listEntryPreview);
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
        if ("settings".equals(view.getTag())) {
            Toast.makeText(this, "Settings not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("search".equals(view.getTag())) {
            Toast.makeText(this, "Search not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("new".equals(view.getTag())) {
            this.startActivity(new Intent("com.gmail.tylerfilla.android.notes.ACTION_EDIT_NOTE"));
        }
    }
    
    public void noteListEntryClicked(NoteListEntry noteListEntry) {
    }
    
    private void enterNoteComposer(Note note) {
        Intent noteEditIntent = new Intent(this.getString(R.string.intent_edit_note));
        noteEditIntent.putExtra("noteFilePath", note.getFile().getAbsolutePath());
        this.startActivity(noteEditIntent);
    }
    
}
