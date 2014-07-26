package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;

public class NotesActivity extends Activity {
    
    private View expandedNotesListEntry;
    private Note expandedNotesListEntryNote;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_notes);
        
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirNotes = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "Notes");
            File dirCache = new File(this.getExternalCacheDir().getAbsolutePath(), "Notes");
            
            dirNotes.mkdir();
            dirCache.mkdir();
            
            NoteKeeper.setDirectories(dirNotes.getAbsolutePath(), dirCache.getAbsolutePath());
        } else {
            File dirNotes = new File(this.getFilesDir(), "Notes");
            File dirCache = new File(this.getFilesDir(), "TempMedia");
            
            dirNotes.mkdir();
            dirCache.mkdir();
            
            NoteKeeper.setDirectories(dirNotes.getAbsolutePath(), dirCache.getAbsolutePath());
        }
        
        ActionBar actionBar = this.getActionBar();
        actionBar.setCustomView(R.layout.actionbar_activity_notes);
        
        this.populateNoteList();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        NoteKeeper.clearTempFiles();
    }
    
    private void populateNoteList() {
        LinearLayout notesListLayout = (LinearLayout) this.findViewById(R.id.notesListLayout);
        ScrollView notesListScroll = (ScrollView) this.findViewById(R.id.notesListScroll);
        LinearLayout notesListEmpty = (LinearLayout) this.findViewById(R.id.notesListEmpty);
        ImageButton buttonActionSearch = (ImageButton) this.findViewById(R.id.buttonActionSearch);
        
        notesListLayout.removeAllViews();
        
        File[] noteFiles = NoteKeeper.listNoteFiles();
        
        if (noteFiles.length > 0) {
            notesListEmpty.setVisibility(View.GONE);
            
            for (File noteFile : noteFiles) {
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
        } else {
            notesListEmpty.setVisibility(View.VISIBLE);
        }
        
        if (noteFiles.length < 2) {
            buttonActionSearch.setVisibility(View.GONE);
        }
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
            listEntryPreview.setText(Html.fromHtml(entryNote.getContent()));
            
            entryView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    noteListEntryClicked((LinearLayout) v, entryNote);
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
            entryView.setTag("contracting");
            
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
            
            entryContractAnimation.addListener(new AnimatorListener() {
                
                @Override
                public void onAnimationStart(Animator animation) {
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    entryView.setTag("contracted");
                }
                
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
                
            });
            
            entryContractAnimation.start();
            
            this.expandedNotesListEntry = null;
            this.expandedNotesListEntryNote = null;
        } else if ("contracted".equals(entryView.getTag())) {
            entryView.setTag("expanding");
            
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
            
            entryExpandAnimation.addListener(new AnimatorListener() {
                
                @Override
                public void onAnimationStart(Animator animation) {
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    entryView.setTag("expanded");
                }
                
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                
                @Override
                public void onAnimationRepeat(Animator animation) {
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
        if ("settings".equals(view.getTag())) {
            Toast.makeText(this, "Settings not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("search".equals(view.getTag())) {
            Toast.makeText(this, "Search not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("new".equals(view.getTag())) {
            this.startActivity(new Intent("com.gmail.tylerfilla.android.notes.ACTION_EDIT_NOTE"));
        }
    }
    
    public void noteListEntryClicked(LinearLayout view, Note note) {
        Intent noteEditIntent = new Intent("com.gmail.tylerfilla.android.notes.ACTION_EDIT_NOTE");
        noteEditIntent.putExtra("noteFilePath", note.getFile().getAbsolutePath());
        this.startActivity(noteEditIntent);
    }
    
}
