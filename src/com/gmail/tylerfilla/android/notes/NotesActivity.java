package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.View;
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
        
        // Action bar customization
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle("");
        actionBar.setCustomView(R.layout.actionbar);
        
        this.populateNoteList();
    }
    
    public void buttonActionClicked(View view) {
    }
    
    private void populateNoteList() {
        ScrollView notesListScroll = (ScrollView) this.findViewById(R.id.notesListScroll);
        LinearLayout notesListLayout = (LinearLayout) this.findViewById(R.id.notesListLayout);
        
        for (File noteFile : NoteKeeper.listNoteFiles()) {
            LinearLayout listEntry = new LinearLayout(this);
            listEntry.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            listEntry.setPadding(20, 20, 20, 20);
            listEntry.setOrientation(LinearLayout.VERTICAL);
            
            TextView listEntryTitle = new TextView(this);
            listEntryTitle.setSingleLine();
            listEntryTitle.setEllipsize(TruncateAt.END);
            listEntryTitle.setTextSize(26.0f);
            listEntryTitle.setTextColor(Color.DKGRAY);
            
            TextView listEntryPreview = new TextView(this);
            listEntryPreview.setSingleLine();
            listEntryPreview.setEllipsize(TruncateAt.END);
            listEntryPreview.setTextSize(18.0f);
            listEntryPreview.setTextColor(Color.GRAY);
            
            ImageView listEntryDivider = new ImageView(this);
            listEntryDivider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 3));
            this.setViewBackground(listEntryDivider,
                    this.getResources().getDrawable(R.color.pad_line));
            
            try {
                Note note = NoteKeeper.readNoteFile(noteFile);
                
                listEntryTitle.setText(note.getTitle());
                listEntryPreview.setText(this.generateNoteContentPreview(note.getContent()));
            } catch (IOException e) {
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
    
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setViewBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }
    
}
