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
    private boolean selected;
    
    public NoteListEntry(Context context) {
        super(context);
        
        this.note = null;
        this.selected = false;
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
        this.rebuild();
    }
    
    public boolean getSelected() {
        return this.selected;
    }
    
    @Override
    public void setSelected(boolean selected) {
        boolean changing = this.selected != selected;
        
        this.selected = selected;
        
        if (changing) {
            this.rebuild();
        }
    }
    
    private void rebuild() {
        this.removeAllViews();
        
        String noteTitle = this.note != null ? this.note.getTitle() != null ? this.note.getTitle()
                : "No title" : "No note";
        String noteContent = this.note != null ? this.note.getContent() != null ? this.note
                .getContent() : "No content" : "No note";
        
        if (this.selected) {
            this.setBackgroundResource(R.color.background_note_list_entry_selected);
        } else {
            this.setBackgroundResource(R.drawable.background_notes_list_entry);
        }
        
        this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);
        this.setPadding(24, 36, 24, 36);
        
        TextView listEntryTitle = new TextView(this.getContext());
        listEntryTitle.setTextAppearance(this.getContext(), R.style.AppThemeNoteListEntryTitleText);
        listEntryTitle.setSingleLine();
        listEntryTitle.setEllipsize(TruncateAt.END);
        listEntryTitle.setText(noteTitle);
        
        TextView listEntryPreview = new TextView(this.getContext());
        listEntryPreview.setTextAppearance(this.getContext(),
                R.style.AppThemeNoteListEntryPreviewText);
        listEntryPreview.setSingleLine();
        listEntryPreview.setEllipsize(TruncateAt.END);
        listEntryPreview.setText(Html.fromHtml(noteContent));
        
        this.addView(listEntryTitle);
        this.addView(listEntryPreview);
    }
    
}
