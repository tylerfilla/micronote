package com.gmail.tylerfilla.android.notes.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
                if (message != null && message.startsWith("report:")) {
                    NoteEditor.this.handleReport(message.substring(7));
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
                    result = "#" + Integer.toHexString(resources.getColor(resourceId) & 0x00FFFFFF);
                }
            }
        }
        
        return result;
    }
    
    private void handleReport(String report) {
        Log.d("NoteEditor-Report", report);
        
        String[] components;
        if (report.contains("=")) {
            components = report.split("=");
            
            String key = components[0];
            String value = components[1];
            
            try {
                Field keyField = NoteEditor.class.getDeclaredField(key);
                Type keyFieldType = keyField.getType();
                
                keyField.setAccessible(true);
                
                if (keyFieldType.equals(String.class)) {
                    keyField.set(this, value);
                } else if (keyFieldType.equals(Boolean.class)) {
                    keyField.setBoolean(this, Boolean.parseBoolean(value));
                } else if (keyFieldType.equals(Integer.class)) {
                    keyField.setInt(this, Integer.parseInt(value));
                } else if (keyFieldType.equals(Long.class)) {
                    keyField.setLong(this, Long.parseLong(value));
                } else if (keyFieldType.equals(Float.class)) {
                    keyField.setFloat(this, Float.parseFloat(value));
                } else if (keyFieldType.equals(Double.class)) {
                    keyField.setDouble(this, Double.parseDouble(value));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else if (report.startsWith("responder->") && report.contains(":")) {
            String methodName = report.substring(11, report.indexOf(":"));
            String[] methodParams = report.substring(report.indexOf(":")).split(",");
            
            for (Method method : Responder.class.getDeclaredMethods()) {
                method.setAccessible(true);
                
                if (method.getName().equals(methodName)) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    
                    for (int i = 0; i < paramTypes.length; i++) {
                        String methodParam = methodParams[i];
                        Class<?> paramType = paramTypes[i];
                        
                        try {
                            if (paramType.equals(String.class)) {
                                params[i] = methodParam;
                            } else if (paramType.equals(Boolean.class)) {
                                params[i] = Boolean.parseBoolean(methodParam);
                            } else if (paramType.equals(Integer.class)) {
                                params[i] = Integer.parseInt(methodParam);
                            } else if (paramType.equals(Long.class)) {
                                params[i] = Long.parseLong(methodParam);
                            } else if (paramType.equals(Float.class)) {
                                params[i] = Float.parseFloat(methodParam);
                            } else if (paramType.equals(Double.class)) {
                                params[i] = Double.parseDouble(methodParam);
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        
                        try {
                            method.invoke(this, params);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        if (report.startsWith("content=")) {
            this.content = report.substring(8);
        } else if (report.startsWith("contentHeight=") && report.length() > 14) {
            this.contentHeight = Float.parseFloat(report.substring(14));
        } else if (report.startsWith("lineWidth=") && report.length() > 10) {
            this.lineWidth = Float.parseFloat(report.substring(10));
        } else if (report.startsWith("lineHeight=") && report.length() > 11) {
            this.lineHeight = Float.parseFloat(report.substring(11));
        } else if (report.startsWith("lineOffsetX=") && report.length() > 12) {
            this.lineOffsetX = Float.parseFloat(report.substring(12));
        } else if (report.startsWith("lineOffsetY=") && report.length() > 12) {
            this.lineOffsetY = Float.parseFloat(report.substring(12));
        } else if (report.startsWith("responder->") && report.length() > 11) {
            String responderCommand = report.substring(11);
            if (responderCommand.startsWith("indentControlState=")
                    && responderCommand.length() > 19) {
                String[] subcomponents = responderCommand.substring(19).split(",");
                
                boolean controlActive = Boolean.parseBoolean(subcomponents[0]);
                boolean enableDecrease = Boolean.parseBoolean(subcomponents[1]);
                boolean enableIncrease = Boolean.parseBoolean(subcomponents[2]);
                
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
