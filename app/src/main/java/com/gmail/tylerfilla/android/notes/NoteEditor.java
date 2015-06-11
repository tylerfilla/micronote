package com.gmail.tylerfilla.android.notes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayDeque;

public class NoteEditor extends WebView {
    
    private static final String ASSET_PATH_EDITOR_HTML_INDEX = "file:///android_asset/editor_html/editor.html";
    
    private volatile boolean editorLoaded;
    
    private ArrayDeque<String> queueAppMessage;
    
    private Note note;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.editorLoaded = false;
        
        this.queueAppMessage = new ArrayDeque<>();
        
        this.note = null;
        
        this.loadEditor();
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void loadEditor() {
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
        this.enqueueAppMessage("~content=" + note.getContent());
        
        // Send last modified time to page
        this.enqueueAppMessage("~lastModified=" + note.getLastModified());
    }
    
    private void handleContentUpdate(String content) {
        // Set note content
        this.note.setContent(content);
    }
    
    private void handlePageUpdate() {
        // Send app messages stored in queue
        while (!this.queueAppMessage.isEmpty()) {
            this.sendAppMessage(this.queueAppMessage.remove());
        }
    }
    
    public void onReceivePageMessage(String message) {
        if (message.startsWith("~")) {
            message = message.substring(1);
            
            // Content updates
            if ("content".equals(message.substring(0, 7))) {
                this.handleContentUpdate(message.substring(8));
            }
        } else if (message.startsWith("!")) {
            message = message.substring(1);
            
            // Page updates
            if ("update".equals(message.substring(0, 6))) {
                this.handlePageUpdate();
            }
        }
    }
    
    public void sendAppMessage(String message) {
        this.loadUrl("javascript:onReceiveAppMessage(\"" + message + "\");");
    }
    
    public void enqueueAppMessage(String message) {
        this.queueAppMessage.add(message);
    }
    
}
