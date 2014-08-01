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

import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteEditor extends WebView {
    
    private Note note;
    private Note pendingNote;
    
    private String editorContent;
    private boolean editorLoaded;
    
    private int lineWidth;
    private int lineOffsetX;
    private int lineOffsetY;
    private int lineOffsetText;
    private final Paint linePaint;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.note = null;
        this.editorLoaded = false;
        
        this.lineWidth = 0;
        this.lineOffsetX = 0;
        this.lineOffsetY = 0;
        this.lineOffsetText = 0;
        this.linePaint = new Paint();
        
        this.linePaint.setColor(context.getResources().getColor(R.color.background_pad_line));
        this.linePaint.setStrokeWidth(2.0f);
        
        try {
            this.loadEditor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (int i = 1; i < 50; i++) {
            canvas.drawLine(this.lineOffsetX, this.lineOffsetY + this.lineOffsetText * i,
                    this.lineOffsetX + this.lineWidth, this.lineOffsetY + this.lineOffsetText * i,
                    this.linePaint);
        }
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
        } else if (command.startsWith("lineWidth:") && command.length() > 10) {
            this.lineWidth = Integer.parseInt(command.substring(10));
        } else if (command.startsWith("lineOffsetX:") && command.length() > 12) {
            this.lineOffsetX = Integer.parseInt(command.substring(12));
        } else if (command.startsWith("lineOffsetY:") && command.length() > 12) {
            this.lineOffsetY = Integer.parseInt(command.substring(12));
        } else if (command.startsWith("lineOffsetText:") && command.length() > 15) {
            this.lineOffsetText = Integer.parseInt(command.substring(15));
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
