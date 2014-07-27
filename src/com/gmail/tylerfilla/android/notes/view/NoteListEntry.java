package com.gmail.tylerfilla.android.notes.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteListEntry extends LinearLayout {
    
    private Note note;
    
    public NoteListEntry(Context context) {
        super(context);
    }
    
    public NoteListEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public NoteListEntry(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
    }
    
}
