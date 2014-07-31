package com.gmail.tylerfilla.android.notes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteEditor extends WebView {
    
    private Note note;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
    }
    
}
