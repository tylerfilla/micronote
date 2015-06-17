package com.gmail.tylerfilla.android.notes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

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
        
        // Enable JavaScript
        this.getSettings().setJavaScriptEnabled(true);
        
        /* Action */
        
        // Load editor document
        this.loadUrl(NoteEditor.ASSET_PATH_EDITOR_HTML_INDEX);
        
        /* Preferences */
    
        // Serialize all app preferences to JSON and send it
        this.enqueueAppMessage("~pref=" + new JSONObject(PreferenceManager.getDefaultSharedPreferences(this.getContext()).getAll()));
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
    
    private void handleUpdateRequest() {
        // Send app messages stored in queue
        while (!this.queueAppMessage.isEmpty()) {
            this.sendAppMessage(this.queueAppMessage.remove());
        }
    }
    
    public void onReceivePageMessage(String message) {
        char actionChar = message.charAt(0);
        message = message.substring(1);
    
        if (actionChar == '~') {
            // Content updates
            if ("content=".equals(message.substring(0, 8))) {
                this.handleContentUpdate(message.substring(8));
            }
        } else if (actionChar == '!') {
            // Update requests
            if ("request".equals(message.substring(0, 7))) {
                this.handleUpdateRequest();
            }
        }
    }
    
    public void sendAppMessage(String message) {
        // Do a ridiculous-looking quotation mark escape operation (must escape for Java String, then Java regex)
        message = message.replaceAll("\\\"", "\\\\\\\"");
    
        // Send message to JavaScript receiver function
        this.loadUrl("javascript:onReceiveAppMessage(\"" + message + "\");");
    }
    
    public void enqueueAppMessage(String message) {
        this.queueAppMessage.add(message);
    }
    
}
