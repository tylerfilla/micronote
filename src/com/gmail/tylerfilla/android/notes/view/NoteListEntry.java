package com.gmail.tylerfilla.android.notes.view;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteListEntry extends LinearLayout {
    
    private Note note;
    
    public NoteListEntry(Context context) {
        super(context);
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
        
        this.removeAllViews();
        
        this.setBackgroundResource(R.drawable.background_notes_list_entry);
        this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);
        this.setPadding(24, 36, 24, 36);
        
        TextView listEntryTitle = new TextView(this.getContext());
        listEntryTitle.setTextAppearance(this.getContext(), R.style.AppThemeNoteListEntryTitleText);
        listEntryTitle.setSingleLine();
        listEntryTitle.setEllipsize(TruncateAt.END);
        listEntryTitle.setText(note.getTitle() != null ? note.getTitle() : "No title available");
        
        TextView listEntryPreview = new TextView(this.getContext());
        listEntryPreview.setTextAppearance(this.getContext(),
                R.style.AppThemeNoteListEntryPreviewText);
        listEntryPreview.setSingleLine();
        listEntryPreview.setEllipsize(TruncateAt.END);
        listEntryPreview.setText(Html.fromHtml(note.getContent() != null ? note.getContent()
                : "No content preview available"));
        
        this.addView(listEntryTitle);
        this.addView(listEntryPreview);
    }
    
}
