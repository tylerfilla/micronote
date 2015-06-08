package com.gmail.tylerfilla.android.notes;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NoteEditor extends WebView {
    
    private static final String ASSET_PATH_EDITOR_HTML_INDEX = "file:///android_asset/editor_html/editor.html";
    
    private boolean editorLoaded;
    
    private Note note;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.editorLoaded = false;
        
        this.note = null;
        
        try {
            this.loadEditor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean getEditorLoaded() {
        return this.editorLoaded;
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
        
        // Send content to page
        this.sendAppMessage("~content=" + note.getContent());
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void loadEditor() throws IOException {
        /* Event listening */
        
        // Add a WebView client
        this.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageFinished(WebView view, String url) {
                NoteEditor.this.editorLoaded = true;
            }
    
        });
        
        // Add a Chrome client
        this.setWebChromeClient(new WebChromeClient() {
        
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                NoteEditor.this.onReceivePageMessage(message);
                return true;
            }
            
        });
        
        /* Settings */
        
        WebSettings settings = this.getSettings();
        
        // Enable JavaScript
        settings.setJavaScriptEnabled(true);
        
        /* Action */
        
        // Load editor document
        this.loadUrl(NoteEditor.ASSET_PATH_EDITOR_HTML_INDEX);
        
        // Set editorLoaded
        this.editorLoaded = true;
    }
    
    public void onReceivePageMessage(String message) {
        Log.d("onReceivePageMessage", message);
        
        if (message.startsWith("~")) {
            // TODO: Handle assignment
        } else if (message.startsWith("!")) {
            // TODO: Handle command
        }
    }
    
    public void sendAppMessage(String message) {
        while (!this.editorLoaded) {}
        this.loadUrl("javascript:onReceiveAppMessage(\"" + message + "\");");
    }
    
}
