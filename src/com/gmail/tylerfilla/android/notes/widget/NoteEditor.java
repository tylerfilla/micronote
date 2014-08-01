package com.gmail.tylerfilla.android.notes.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteEditor extends WebView {
    
    private Note note;
    private Note pendingNote;
    
    private String editorContent;
    private boolean editorLoaded;
    
    private int lineSize;
    private int lineOffset;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.note = null;
        this.editorLoaded = false;
        
        this.lineSize = 0;
        
        try {
            this.loadEditor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        Paint paint = new Paint();
        paint.setColor(0xFFFF0000);
        
        canvas.drawLine(8.0f, this.lineOffset + this.lineSize, 500.0f, this.lineOffset
                + this.lineSize, paint);
        canvas.drawLine(8.0f, this.lineOffset + this.lineSize * 2, 500.0f, this.lineOffset
                + this.lineSize * 2, paint);
        canvas.drawLine(8.0f, this.lineOffset + this.lineSize * 3, 500.0f, this.lineOffset
                + this.lineSize * 3, paint);
        canvas.drawLine(8.0f, this.lineOffset + this.lineSize * 4, 500.0f, this.lineOffset
                + this.lineSize * 4, paint);
    }
    
    public Note getNote() {
        if (this.note != null) {
            String editorContent = this.getEditorContent();
            if (!editorContent.equals(this.note.getContent())) {
                this.note.setContent(editorContent);
            }
        }
        
        return this.note;
    }
    
    public void setNote(Note note) {
        if (this.editorLoaded) {
            this.note = note;
            this.setEditorContent(note.getContent());
            
            if (note.getFile() == null) {
                this.setHeaderContent("New");
            } else {
                this.setHeaderContent(this
                        .formatHeaderDate(new Date(note.getFile().lastModified())));
            }
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
        
        this.loadDataWithBaseURL("file:///android_asset/", internalCodeBuilder.toString(),
                "text/html", "utf-8", null);
        
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
        
        this.setWebChromeClient(new WebChromeClient() {
            
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                NoteEditor.this.handleCommand(message);
                result.confirm();
                return true;
            }
            
        });
    }
    
    private void handleCommand(String command) {
        Log.d("", command);
        if (command.startsWith("content:") && command.length() > 8) {
            this.editorContent = command.substring(8);
        } else if (command.startsWith("lineSize:") && command.length() > 9) {
            this.lineSize = Integer.parseInt(command.substring(9));
        } else if (command.startsWith("lineOffset:") && command.length() > 11) {
            this.lineOffset = Integer.parseInt(command.substring(11));
        }
    }
    
    private String getEditorContent() {
        return this.editorContent;
    }
    
    private void setEditorContent(String editorContent) {
        this.editorContent = editorContent;
        this.loadUrl("javascript:setEditorContent('" + editorContent + "');");
    }
    
    private void setHeaderContent(String headerContent) {
        this.loadUrl("javascript:setHeaderContent('" + headerContent + "');");
    }
    
    private String formatHeaderDate(Date date) {
        return new SimpleDateFormat("h:mm a 'on' M/dd/yyyy", Locale.US).format(date);
    }
    
}
