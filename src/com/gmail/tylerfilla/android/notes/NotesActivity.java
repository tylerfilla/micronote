package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;

public class NotesActivity extends Activity {
    
    private View expandedNotesListEntry;
    private Note expandedNotesListEntryNote;
    
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
            Note note = null;
            
            try {
                note = NoteKeeper.readNoteFile(noteFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            LinearLayout listEntry = new LinearLayout(this);
            this.buildNoteListEntry(listEntry, note);
            
            ImageView listEntryDivider = new ImageView(this);
            listEntryDivider.setBackgroundResource(R.color.pad_line);
            listEntryDivider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 3));
            
            notesListLayout.addView(listEntry);
            notesListLayout.addView(listEntryDivider);
        }
        
        notesListScroll.invalidate();
        notesListScroll.requestLayout();
    }
    
    public void buildNoteListEntry(LinearLayout entryView, final Note entryNote) {
        entryView.setBackgroundResource(R.drawable.background_notes_list_entry);
        entryView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        entryView.setOrientation(LinearLayout.VERTICAL);
        entryView.setPadding(24, 36, 24, 36);
        entryView.setLongClickable(true);
        entryView.setTag("contracted");
        
        TextView listEntryTitle = new TextView(this);
        listEntryTitle.setTag("anchor");
        listEntryTitle.setTextAppearance(this, R.style.AppThemeNotesListEntryTitleText);
        listEntryTitle.setSingleLine();
        listEntryTitle.setEllipsize(TruncateAt.END);
        
        TextView listEntryPreview = new TextView(this);
        listEntryPreview.setTag("anchor");
        listEntryPreview.setTextAppearance(this, R.style.AppThemeNotesListEntryPreviewText);
        listEntryPreview.setSingleLine();
        listEntryPreview.setEllipsize(TruncateAt.END);
        
        if (entryNote != null) {
            listEntryTitle.setText(entryNote.getTitle());
            listEntryPreview.setText(this.generateNoteContentPreview(entryNote.getContent()));
            
            entryView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    noteListEntryClicked(v, entryNote);
                }
                
            });
            entryView.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    toggleNoteListEntryExpansion((LinearLayout) v, entryNote);
                    return true;
                }
                
            });
        }
        
        entryView.addView(listEntryTitle);
        entryView.addView(listEntryPreview);
    }
    
    public void toggleNoteListEntryExpansion(final LinearLayout entryView, Note entryNote) {
        if ("expanded".equals(entryView.getTag())) {
            entryView.removeAllViews();
            this.buildNoteListEntry(entryView, entryNote);
            
            ValueAnimator entryContractAnimation = ValueAnimator.ofInt(entryView.getHeight(),
                    entryView.getHeight() - 264);
            entryContractAnimation.setDuration(100l);
            entryContractAnimation.setInterpolator(new LinearInterpolator());
            
            entryContractAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LayoutParams layoutParams = entryView.getLayoutParams();
                    layoutParams.height = (Integer) animation.getAnimatedValue();
                    entryView.setLayoutParams(layoutParams);
                }
                
            });
            
            entryContractAnimation.start();
            
            entryView.setTag("contracted");
            
            this.expandedNotesListEntry = null;
            this.expandedNotesListEntryNote = null;
        } else {
            if (this.expandedNotesListEntry != null) {
                this.toggleNoteListEntryExpansion((LinearLayout) this.expandedNotesListEntry,
                        this.expandedNotesListEntryNote);
            }
            
            ValueAnimator entryExpandAnimation = ValueAnimator.ofInt(entryView.getHeight(),
                    entryView.getHeight() + 264);
            entryExpandAnimation.setDuration(100l);
            entryExpandAnimation.setInterpolator(new LinearInterpolator());
            
            entryExpandAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LayoutParams layoutParams = entryView.getLayoutParams();
                    layoutParams.height = (Integer) animation.getAnimatedValue();
                    entryView.setLayoutParams(layoutParams);
                }
                
            });
            
            entryExpandAnimation.start();
            
            TextView entryInfoAuthor = new TextView(this);
            entryInfoAuthor.setSingleLine();
            entryInfoAuthor.setEllipsize(TruncateAt.END);
            entryInfoAuthor.setText("Author: " + entryNote.getAuthor());
            entryInfoAuthor.setTextAppearance(this, R.style.AppThemeNotesListEntryInfoText);
            
            LinearLayout.LayoutParams entryInfoAuthorLayout = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            entryInfoAuthorLayout.setMargins(0, 40, 0, 0);
            entryInfoAuthor.setLayoutParams(entryInfoAuthorLayout);
            
            TextView entryInfoFilename = new TextView(this);
            entryInfoFilename.setSingleLine();
            entryInfoFilename.setEllipsize(TruncateAt.END);
            entryInfoFilename.setText("Filename: " + entryNote.getFile().getName());
            entryInfoFilename.setTextAppearance(this, R.style.AppThemeNotesListEntryInfoText);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
                    Locale.getDefault());
            
            TextView entryInfoDateCreated = new TextView(this);
            entryInfoDateCreated.setSingleLine();
            entryInfoDateCreated.setText("Created: "
                    + dateFormat.format(entryNote.getDateCreated()));
            entryInfoDateCreated.setTextAppearance(this, R.style.AppThemeNotesListEntryInfoText);
            
            TextView entryInfoDateModified = new TextView(this);
            entryInfoDateModified.setSingleLine();
            entryInfoDateModified.setText("Modified: "
                    + dateFormat.format(entryNote.getDateModified()));
            entryInfoDateModified.setTextAppearance(this, R.style.AppThemeNotesListEntryInfoText);
            
            entryView.addView(entryInfoAuthor);
            entryView.addView(entryInfoFilename);
            entryView.addView(entryInfoDateCreated);
            entryView.addView(entryInfoDateModified);
            
            entryView.setTag("expanded");
            
            this.expandedNotesListEntry = entryView;
            this.expandedNotesListEntryNote = entryNote;
        }
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
    
}
