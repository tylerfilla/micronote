package com.gmail.tylerfilla.android.notes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.webkit.WebView;

public class NoteEditor extends WebView {
    
    private Note note;
    private Note pendingNote;
    
    private boolean editorLoaded;
    
    private String content;
    private float contentHeight;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.note = null;
        this.pendingNote = null;
        
        this.editorLoaded = false;
        
        this.content = null;
        
        try {
            this.loadEditor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        if (this.editorLoaded) {
            this.note = note;
        } else {
            this.pendingNote = note;
        }
    }
    
    private void loadEditor() throws IOException {
        this.getSettings().setJavaScriptEnabled(true);
        
        StringBuilder internalCodeBuilder = new StringBuilder();
        BufferedReader internalCodeReader = new BufferedReader(new InputStreamReader(this
                .getContext().getAssets().open("editor_html/editor.html")));
        String line = null;
        while ((line = internalCodeReader.readLine()) != null) {
            internalCodeBuilder.append(line).append('\n');
        }
        String internalCode = internalCodeBuilder.toString();
        
        this.loadDataWithBaseURL("file:///android_asset/", internalCode, "text/html", "utf-8", null);
    }
    
}
