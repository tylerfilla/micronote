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
        // FIXME: NoteEditor should be implementation agnostic; add a proxy of some sort
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
    
    public void unload() {
        // Call the unload event handler in JavaScript
        this.loadUrl("javascript:window.onunload();");
    }
    
    private void handleIncomingAssignment(String key, String value) {
        // Debug log
        System.out.println("pagemessage: assign '" + key + "' as '" + value + "'");
        
        // Execute appropriate handler function for key
        if ("content".equals(key)) {
            this.handleIncomingAssignmentContent(value);
        }
    }
    
    private void handleIncomingAssignmentContent(String value) {
        // Set note content
        this.note.setContent(value);
    }
    
    private void handleIncomingCommand(String command) {
        // Debug log
        System.out.println("pagemessage: command '" + command + "'");
        
        if ("request".equals(command)) {
            this.handleIncomingCommandRequest();
        }
    }
    
    private void handleIncomingCommandRequest() {
        // Send all app messages waiting in queue
        while (!this.queueAppMessage.isEmpty()) {
            this.sendAppMessage(this.queueAppMessage.remove());
        }
    }
    
    private void onReceivePageMessage(String message) {
        // Validate message
        if (message == null || message.length() <= 1) {
            System.err.println("pagemessage: protocol error: invalid message");
            return;
        }
        
        // Remove and save action character
        char action = message.charAt(0);
        message = message.substring(1);
        
        // Check action character and act accordingly
        if (action == '~') {
            if (message.contains("=")) {
                String key = message.substring(0, message.indexOf('='));
                String value = message.substring(message.indexOf('=') + 1);
                
                this.handleIncomingAssignment(key, value);
            } else {
                System.err.println("pagemessage: protocol error: no assignment operator");
            }
        } else if (action == '!') {
            this.handleIncomingCommand(message);
        } else {
            System.err.println("pagemessage: protocol error: invalid action character '" + action + "'");
        }
    }
    
    private void sendAppMessage(String message) {
        // Debug log
        System.out.println("appmessage: sending outgoing message '" + message + "'");
        
        // Do a ridiculous-looking quotation mark escape operation (must escape for Java String, then Java regex)
        message = message.replaceAll("\\\"", "\\\\\\\"");
        
        // Send message to JavaScript receiver function
        this.loadUrl("javascript:onReceiveAppMessage(\"" + message + "\");");
    }
    
    private void enqueueAppMessage(String message) {
        // Debug log
        System.out.println("appmessage: enqueuing outgoing message '" + message + "'");
        
        // Add message to queue
        this.queueAppMessage.add(message);
    }
    
}
