package com.gmail.tylerfilla.android.notes.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteEditor extends WebView {
    
    private Note note;
    private Note pendingNote;
    
    private boolean editorLoaded;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.note = null;
        this.editorLoaded = false;
        
        try {
            this.loadEditor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Note getNote() {
        if (this.note != null) {
            String newContent = this.getEditorContent();
            if (!newContent.equals(this.note.getContent())) {
                this.note.setContent(newContent);
            }
        }
        
        return this.note;
    }
    
    public void setNote(Note note) {
        if (this.editorLoaded) {
            this.note = note;
            this.setEditorContent(note.getContent());
        } else {
            this.pendingNote = note;
        }
    }
    
    private void loadEditor() throws IOException {
        this.getSettings().setJavaScriptEnabled(true);
        
        StringBuilder internalCodeBuilder = new StringBuilder();
        BufferedReader internalCodeReader = new BufferedReader(new InputStreamReader(this
                .getContext().getAssets().open("editor.html")));
        
        String line = null;
        while ((line = internalCodeReader.readLine()) != null) {
            internalCodeBuilder.append(line).append('\n');
        }
        
        this.loadData(internalCodeBuilder.toString(), "text/html", "utf-8");
        
        this.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageFinished(WebView view, String url) {
                NoteEditor.this.editorLoaded = true;
                
                if (NoteEditor.this.pendingNote != null) {
                    NoteEditor.this.setNote(NoteEditor.this.pendingNote);
                    NoteEditor.this.pendingNote = null;
                }
            }
            
        });
    }
    
    private String getEditorContent() {
        final String[] content = new String[1];
        
        this.addJavascriptInterface(new Object() {
            
            @JavascriptInterface
            public void respondEditorContent(String editorContent) {
                Log.d("", editorContent);
            }
            
        }, "NoteEditor");
        
        this.loadUrl("javascript:getEditorContent();");
        
        return content[0];
    }
    
    private void setEditorContent(String editorContent) {
        Log.d("", editorContent);
        this.loadUrl("javascript:setEditorContent('" + editorContent + "');");
    }
    
}
