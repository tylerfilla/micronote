package com.gmail.tylerfilla.android.notes.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
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
    
    private static final Pattern editorDocumentPreprocessorDirectivePattern = Pattern
            .compile("(?s)<!--\\[\\[(.*?)\\]\\]-->");
    
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
                this.setHeaderContent(new SimpleDateFormat("h:mm a 'on' M/dd/yyyy", Locale.US)
                        .format(new Date(note.getFile().lastModified())));
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
        case FOCUS:
            this.loadUrl("javascript:contentAreaFocus();");
            break;
        case BLUR:
            this.loadUrl("javascript:contentAreaBlur();");
            break;
        }
    }
    
    public Responder getResponder() {
        return this.responder;
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
        String internalCode = internalCodeBuilder.toString();
        
        this.loadDataWithBaseURL("file:///android_asset/",
                this.preprocessEditorDocument(internalCode), "text/html", "utf-8", null);
        
        this.setWebViewClient(new WebViewClient() {
            
            @Override
            public void onPageFinished(WebView view, String url) {
                NoteEditor.this.editorLoaded = true;
                
                if (NoteEditor.this.pendingNote != null) {
                    NoteEditor.this.setNote(NoteEditor.this.pendingNote);
                    NoteEditor.this.pendingNote = null;
                }
                
                if (NoteEditor.this.responder != null) {
                    NoteEditor.this.responder.onPageLoad(url);
                }
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (NoteEditor.this.responder != null) {
                    NoteEditor.this.responder.onExternalRequest(url);
                }
                
                return true;
            }
            
        });
        
        this.setWebChromeClient(new WebChromeClient() {
            
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (message != null) {
                    NoteEditor.this.handleReport(message);
                }
                
                result.confirm();
                return true;
            }
            
        });
    }
    
    private String preprocessEditorDocument(String source) {
        Matcher matcher = NoteEditor.editorDocumentPreprocessorDirectivePattern.matcher(source);
        
        int offset = 0;
        while (matcher.find()) {
            String replacement = this.handleEditorDocumentPreprocessorDirective(matcher.group(1)
                    .trim());
            
            int lengthBefore = source.length();
            source = source.substring(0, matcher.start() + offset) + replacement
                    + source.substring(matcher.end() + offset);
            offset += source.length() - lengthBefore;
        }
        Log.d("", source);
        return source;
    }
    
    private String handleEditorDocumentPreprocessorDirective(String directive) {
        String result = "<!-- An internal error has occurred -->";
        
        String[] components = directive.split(" ");
        if (components.length > 0) {
            if (components[0].equals("res") && components.length == 4) {
                String type = components[1];
                String format = components[2];
                String resource = components[3];
                
                int resourceId = -1;
                
                for (Class<?> memberClass : R.class.getClasses()) {
                    if (memberClass.getName().endsWith(type)) {
                        try {
                            resourceId = memberClass.getField(resource).getInt(null);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                Resources resources = this.getContext().getResources();
                if (format.equals("hexcolor")) {
                    result = "#" + Integer.toHexString(resources.getColor(resourceId));
                }
            }
        }
        
        return result;
    }
    
    private void handleReport(String report) {
        if (report.startsWith("content:")) {
            this.content = report.substring(8);
        } else if (report.startsWith("contentHeight:") && report.length() > 14) {
            this.contentHeight = Float.parseFloat(report.substring(14));
        } else if (report.startsWith("lineWidth:") && report.length() > 10) {
            this.lineWidth = Float.parseFloat(report.substring(10));
        } else if (report.startsWith("lineHeight:") && report.length() > 11) {
            this.lineHeight = Float.parseFloat(report.substring(11));
        } else if (report.startsWith("lineOffsetX:") && report.length() > 12) {
            this.lineOffsetX = Float.parseFloat(report.substring(12));
        } else if (report.startsWith("lineOffsetY:") && report.length() > 12) {
            this.lineOffsetY = Float.parseFloat(report.substring(12));
        } else if (report.startsWith("responder/") && report.length() > 10) {
            String responderCommand = report.substring(10);
            if (responderCommand.startsWith("indentControlState:")
                    && responderCommand.length() > 19) {
                String[] components = responderCommand.substring(19).split(",");
                
                boolean controlActive = Boolean.parseBoolean(components[0]);
                boolean enableDecrease = Boolean.parseBoolean(components[1]);
                boolean enableIncrease = Boolean.parseBoolean(components[2]);
                
                if (this.responder != null) {
                    this.responder.onUpdateIndentControlState(controlActive, enableDecrease,
                            enableIncrease);
                }
            }
        }
    }
    
    private void setEditorContent(String editorContent) {
        this.loadUrl("javascript:setEditorContent('" + editorContent + "');");
    }
    
    private void setHeaderContent(String headerContent) {
        this.loadUrl("javascript:setHeaderContent('" + headerContent + "');");
    }
    
    public static enum Action {
        
        FOCUS,
        BLUR,
        CREATE_LIST_BULLET,
        CREATE_LIST_NUMBER,
        CREATE_LIST_CHECKBOX,
        INDENT_INCREASE,
        INDENT_DECREASE,
        
    }
    
    public static interface Responder {
        
        public void onExternalRequest(String request);
        
        public void onPageLoad(String url);
        
        public void onUpdateIndentControlState(boolean controlActive, boolean enableDecrease,
                boolean enableIncrease);
        
    }
    
}
