package io.microdev.note.core;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class NoteEditor extends EditText {
    
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
