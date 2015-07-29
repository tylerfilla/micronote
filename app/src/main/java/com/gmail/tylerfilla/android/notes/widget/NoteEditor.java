package com.gmail.tylerfilla.android.notes.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.util.JSONUtil;

import org.json.JSONException;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NoteEditor extends WebView {
    
    private static final String ASSET_PATH_EDITOR_HTML_INDEX = "file:///android_asset/editor_html/editor.html";
    
    private Note note;
    
    private Configuration configuration;
    private Map<String, Object> res;
    
    private OnInitializedListener onInitializedListener;
    
    private ArrayDeque<String> queueAppMessage;
    
    private boolean initialized;
    
    public NoteEditor(Context context) {
        super(context);
        
        // Configuration
        this.configuration = new Configuration();
        
        // Res
        this.res = new HashMap<>();
        
        // App message queue
        this.queueAppMessage = new ArrayDeque<>();
        
        // Initialize backing WebView
        this.initializeWebView();
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
        
        // Reload
        this.load();
    }
    
    public Configuration getConfiguration() {
        return this.configuration;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        
        // Reload
        this.load();
    }
    
    public Map<String, Object> getRes() {
        return this.res;
    }
    
    public void setRes(Map<String, Object> res) {
        this.res = res;
        
        // Reload
        this.load();
    }
    
    public void setOnInitializedListener(OnInitializedListener onInitializedListener) {
        this.onInitializedListener = onInitializedListener;
    }
    
    public void unload() {
        // Call the unload event handler in JavaScript
        this.loadUrl("javascript:window.onunload(null);");
    }
    
    private void load() {
        // Load res
        this.loadRes();
        
        // Load configuration
        this.loadConfiguration();
        
        // Load note
        this.loadNote();
    }
    
    private void loadNote() {
        // Sanity check
        if (this.note == null) {
            return;
        }
        
        // Map for note data
        Map<String, Object> map = new HashMap<>();
        
        // Put note data
        map.put("content", this.note.getContent());
        map.put("title", this.note.getTitle());
        map.put("lastModified", this.note.getLastModified());
        
        // Send serialized copy of note data
        try {
            this.enqueueAppMessage("~note=" + JSONUtil.convertMapToJSONObject(map));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void loadRes() {
        // Sanity check
        if (this.res == null) {
            return;
        }
        
        // Send serialized copy of resource data
        try {
            this.enqueueAppMessage("~res=" + JSONUtil.convertMapToJSONObject(this.res));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void loadConfiguration() {
        // Sanity check
        if (this.configuration == null) {
            return;
        }
        
        // Map for configuration data
        Map<String, Object> map = new HashMap<>();
        
        // Put configuration data
        map.put("backgroundColor", this.configuration.backgroundColor);
        map.put("textColor", this.configuration.textColor);
        map.put("textSize", this.configuration.textSize);
        map.put("showNotepadLines", this.configuration.showNotepadLines);
        map.put("formatDate", this.configuration.formatDate.name());
        map.put("formatTime", this.configuration.formatTime.name());
        map.put("locale", this.configuration.locale);
        map.put("localizedStrings", this.configuration.localizedStrings);
        
        // Send serialized copy of configuration data
        try {
            this.enqueueAppMessage("~config=" + JSONUtil.convertMapToJSONObject(map));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        // Add a WebView client to handle initialized event
        this.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!NoteEditor.this.initialized && NoteEditor.this.onInitializedListener != null) {
                    // Set initialized flag
                    NoteEditor.this.initialized = true;
                    
                    // Callback
                    NoteEditor.this.onInitializedListener.onInitialized();
                }
            }
            
        });
        
        // Add a Chrome client to intercept "alert dialogs"
        this.setWebChromeClient(new WebChromeClient() {
            
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                // Acknowledge alert to resume execution
                result.confirm();
                
                // Send alert message to receiver method
                NoteEditor.this.onReceivePageMessage(message);
                
                return true;
            }
            
        });
        
        // Enable JavaScript
        this.getSettings().setJavaScriptEnabled(true);
        
        // Load editor document
        this.loadUrl(NoteEditor.ASSET_PATH_EDITOR_HTML_INDEX);
        
        // Background color workaround
        this.setBackgroundColor(0);
        
        // Load default configuration
        this.loadConfiguration();
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
    
    public static class Configuration {
        
        private static final int DEFAULT_BACKGROUND_COLOR = 0xfffaf6bb;
        private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
        private static final int DEFAULT_TEXT_SIZE = 22;
        private static final boolean DEFAULT_SHOW_NOTEPAD_LINES = true;
        private static final EnumFormatDate DEFAULT_FORMAT_DATE = EnumFormatDate.values()[0];
        private static final EnumFormatTime DEFAULT_FORMAT_TIME = EnumFormatTime.values()[0];
        
        public int backgroundColor;
        public int textColor;
        public int textSize;
        public boolean showNotepadLines;
        public EnumFormatDate formatDate;
        public EnumFormatTime formatTime;
        
        public Locale locale;
        public HashMap<String, String> localizedStrings;
        
        private Configuration() {
            /* Defaults */
            
            this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
            this.textColor = DEFAULT_TEXT_COLOR;
            this.textSize = DEFAULT_TEXT_SIZE;
            this.showNotepadLines = DEFAULT_SHOW_NOTEPAD_LINES;
            this.formatDate = DEFAULT_FORMAT_DATE;
            this.formatTime = DEFAULT_FORMAT_TIME;
        }
        
        public enum EnumFormatDate {
            
            MONTH_D_YYYY,
            MONTH_D_YY,
            M_DD_YYYY_SLASH,
            M_DD_YYYY_DASH,
            M_DD_YY_SLASH,
            M_DD_YY_DASH,
            DD_M_YYYY_SLASH,
            DD_M_YYYY_DASH,
            DD_M_YY_SLASH,
            DD_M_YY_DASH,
            
        }
        
        public enum EnumFormatTime {
            
            _12_HOUR,
            _24_HOUR,
            
        }
        
    }
    
    public interface OnInitializedListener {
        
        void onInitialized();
        
    }
    
}
