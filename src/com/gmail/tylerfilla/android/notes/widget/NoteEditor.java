package com.gmail.tylerfilla.android.notes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteEditor extends WebView {
    
    private static final String internalCode = "<html><body onLoad=\"editarea.document.designMode = 'on';\" style=\"width: 100%; height: 100%;\">"
            + "<iframe name=\"editarea\" id=\"editarea\" style=\"border: none; position: absolute; top: 0; left: 0; width: 100%; height: 100%;\"></iframe>"
            + "</body></html>";
    
    private Note note;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.getSettings().setJavaScriptEnabled(true);
        this.loadData(internalCode, "text/html", "UTF-8");
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
    }
    
}
