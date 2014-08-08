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
    
    private boolean editorLoaded;
    
    private String content;
    private float contentHeight;
    
    private float lineWidth;
    private float lineHeight;
    private float lineOffsetX;
    private float lineOffsetY;
    private final Paint linePaint;
    
    private Responder responder;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.note = null;
        this.pendingNote = null;
        
        this.editorLoaded = false;
        
        this.content = null;
        this.contentHeight = 0.0f;
        
        this.lineWidth = 0.0f;
        this.lineHeight = 0.0f;
        this.lineOffsetX = 0.0f;
        this.lineOffsetY = 0.0f;
        this.linePaint = new Paint();
        
        this.responder = null;
        
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
        
        if (this.content != null) {
            if (this.contentHeight > 0.0f && this.lineWidth > 0.0f && this.lineHeight > 0.0f
                    && this.lineOffsetX > 0.0f && this.lineOffsetY > 0.0f) {
                for (int i = 1; i < Math.max(this.contentHeight / this.lineHeight,
                        this.content.length()); i++) {
                    canvas.drawLine(this.lineOffsetX, this.lineOffsetY + this.lineHeight * i,
                            this.lineOffsetX + this.lineWidth, this.lineOffsetY + this.lineHeight
                                    * i, this.linePaint);
                }
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
            this.content = note.getContent();
            
            this.setEditorContent(this.content);
            
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
        case CREATE_LIST_CHECKBOX:
            this.loadUrl("javascript:createListCheckbox();");
            break;
        case INDENT_INCREASE:
            this.loadUrl("javascript:indentIncrease();");
            break;
        case INDENT_DECREASE:
            this.loadUrl("javascript:indentDecrease();");
            break;
        }
    }
    
    public void setResponder(Responder responder) {
        this.responder = responder;
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
        
        this.loadDataWithBaseURL("file:///android_asset/",
                this.preprocessEditorDocument(internalCodeBuilder.toString()), "text/html",
                "utf-8", null);
        
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
                NoteEditor.this.handleReport(message);
                
                result.confirm();
                return true;
            }
            
        });
    }
    
    private String preprocessEditorDocument(String source) {
        return source.replace(
                "$BODY-BACKGROUND-COLOR$",
                "#"
                        + Integer.toHexString(this.getContext().getResources()
                                .getColor(R.color.background_pad) & 0x00FFFFFF));
    }
    
    private void handleReport(String report) {
        Log.d("NoteEditor-Report", report);
        
        if (report.startsWith("content:")) {
            if (report.length() == 8) {
                this.content = "";
            } else if (report.length() > 8) {
                this.content = report.substring(8);
            }
        } else if (report.startsWith("contentHeight:") && report.length() > 14) {
            this.contentHeight = Math.max(this.contentHeight,
                    Float.parseFloat(report.substring(14)));
        } else if (report.startsWith("lineWidth:") && report.length() > 10) {
            this.lineWidth = Float.parseFloat(report.substring(10));
        } else if (report.startsWith("lineHeight:") && report.length() > 11) {
            this.lineHeight = Float.parseFloat(report.substring(11));
        } else if (report.startsWith("lineOffsetX:") && report.length() > 12) {
            this.lineOffsetX = Float.parseFloat(report.substring(12));
        } else if (report.startsWith("lineOffsetY:") && report.length() > 12) {
            this.lineOffsetY = Float.parseFloat(report.substring(12));
        } else if (this.responder != null && report.startsWith("responder/")
                && report.length() > 10) {
            String responderCommand = report.substring(10);
            if (responderCommand.startsWith("indentControlState:")
                    && responderCommand.length() > 19) {
                String[] components = responderCommand.substring(19).split(",");
                
                boolean controlActive = Boolean.parseBoolean(components[0]);
                boolean enableDecrease = Boolean.parseBoolean(components[1]);
                boolean enableIncrease = Boolean.parseBoolean(components[2]);
                
                this.responder.updateIndentControlState(controlActive, enableDecrease,
                        enableIncrease);
            }
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
        CREATE_LIST_CHECKBOX,
        INDENT_INCREASE,
        INDENT_DECREASE,
        
    }
    
    public static interface Responder {
        
        public void updateIndentControlState(boolean controlActive, boolean enableDecrease,
                boolean enableIncrease);
        
    }
    
}
