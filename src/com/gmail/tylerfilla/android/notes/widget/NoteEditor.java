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
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteEditor extends WebView {
    
    private Note note;
    private Note pendingNote;
    
    private boolean editorLoaded;
    
    private String content;
    private int contentHeight;
    
    private int lineWidth;
    private int lineHeight;
    private int lineOffsetX;
    private int lineOffsetY;
    private final Paint linePaint;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.note = null;
        this.pendingNote = null;
        
        this.editorLoaded = false;
        
        this.content = null;
        this.contentHeight = 0;
        
        this.lineWidth = 0;
        this.lineHeight = 0;
        this.lineOffsetX = 0;
        this.lineOffsetY = 0;
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
        
        if (this.contentHeight > 0 && this.lineWidth > 0 && this.lineHeight > 0
                && this.lineOffsetX > 0 && this.lineOffsetY > 0) {
            for (int i = 1; i < Math.max(this.contentHeight / this.lineHeight,
                    this.content.length()); i++) {
                canvas.drawLine(this.lineOffsetX, this.lineOffsetY + this.lineHeight * i,
                        this.lineOffsetX + this.lineWidth, this.lineOffsetY + this.lineHeight * i,
                        this.linePaint);
            }
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
    
    public void performAction(Action action) {
        switch (action) {
        case CREATE_LIST_BULLET:
            this.loadUrl("javascript:createListUnordered();");
            break;
        case CREATE_LIST_NUMBER:
            this.loadUrl("javascript:createListOrdered();");
            break;
        }
    }
    
    private void loadEditor() throws IOException {
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        
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
                if (!message.equals("nop")) {
                    NoteEditor.this.handleReport(message);
                }
                
                result.confirm();
                return true;
            }
            
        });
    }
    
    private void handleReport(String report) {
        if (report.startsWith("content:") && report.length() > 8) {
            this.content = report.substring(8);
        } else if (report.startsWith("contentHeight:") && report.length() > 14) {
            this.contentHeight = Math.max(this.contentHeight,
                    (int) Double.parseDouble(report.substring(14)));
        } else if (report.startsWith("lineWidth:") && report.length() > 10) {
            this.lineWidth = (int) Double.parseDouble(report.substring(10));
        } else if (report.startsWith("lineHeight:") && report.length() > 11) {
            this.lineHeight = (int) Double.parseDouble(report.substring(11));
        } else if (report.startsWith("lineOffsetX:") && report.length() > 12) {
            this.lineOffsetX = (int) Double.parseDouble(report.substring(12));
        } else if (report.startsWith("lineOffsetY:") && report.length() > 12) {
            this.lineOffsetY = (int) Double.parseDouble(report.substring(12));
        }
    }
    
    private String getEditorContent() {
        return this.content;
    }
    
    private void setEditorContent(String editorContent) {
        this.content = editorContent;
        this.loadUrl("javascript:setEditorContent('" + editorContent + "');");
    }
    
    private void setHeaderContent(String headerContent) {
        this.loadUrl("javascript:setHeaderContent('" + headerContent + "');");
    }
    
    private String formatHeaderDate(Date date) {
        return new SimpleDateFormat("h:mm a 'on' M/dd/yyyy", Locale.US).format(date);
    }
    
    public static enum Action {
        
        CREATE_LIST_BULLET,
        
        CREATE_LIST_NUMBER,
        
    }
    
}
