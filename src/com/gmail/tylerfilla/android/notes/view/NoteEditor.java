package com.gmail.tylerfilla.android.notes.view;

import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.EditText;

import com.gmail.tylerfilla.android.notes.R;

public class NoteEditor extends EditText {
    
    private final Paint notepadLinePaint;
    private final Rect firstLineBounds;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.notepadLinePaint = new Paint();
        this.firstLineBounds = new Rect();
        
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.NoteEditor);
        
        for (int i = 0; i < styledAttrs.getIndexCount(); i++) {
            int attr = styledAttrs.getIndex(i);
            switch (attr) {
            case R.styleable.NoteEditor_notepadLineColor:
                this.notepadLinePaint.setColor(styledAttrs.getColor(attr, 0x000000));
                break;
            case R.styleable.NoteEditor_notepadLineStrokeWidth:
                this.notepadLinePaint.setStrokeWidth(styledAttrs.getDimension(attr, 0.0f));
                break;
            }
        }
        
        styledAttrs.recycle();
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        this.getLineBounds(0, firstLineBounds);
        
        for (int i = 1; i < this.getLineCount() + this.getHeight() / this.getLineHeight(); i++) {
            int linePos = this.getLineHeight() * i + this.firstLineBounds.bottom
                    - this.getLineHeight();
            
            canvas.drawLine(0.0f, linePos, this.getWidth(), linePos, this.notepadLinePaint);
        }
        
        super.onDraw(canvas);
    }
    
    public String getNoteContent() {
        Editable text = this.getText();
        if (text instanceof Spannable) {
            return this.spannableToNoteContent(text);
        } else {
            return text.toString();
        }
    }
    
    public void setNoteContent(String noteContent) {
        this.setText(this.noteContentToSpannable(noteContent));
    }
    
    private Spannable noteContentToSpannable(String noteContent) {
        SpannableStringBuilder contentBuilder = new SpannableStringBuilder();
        
        boolean escaped = false;
        boolean bracketed = false;
        
        int bracketBegin = -1;
        int bracketEnd = -1;
        String bracketSequence = "";
        
        for (int ci = 0; ci < noteContent.length(); ci++) {
            char c = noteContent.charAt(ci);
            
            if (bracketed) {
                if (c == ']') {
                    bracketed = false;
                    bracketEnd = ci;
                    
                    this.handleBracketSequence(contentBuilder, bracketSequence, bracketBegin,
                            bracketEnd);
                    
                    bracketSequence = "";
                } else {
                    bracketBegin = ci;
                    bracketSequence += c;
                }
                continue;
            } else {
                if (escaped) {
                    contentBuilder.append(c);
                    escaped = false;
                } else {
                    if (c == '[') {
                        bracketed = true;
                    } else if (c == '\\') {
                        escaped = true;
                    } else {
                        contentBuilder.append(c);
                    }
                }
            }
        }
        
        return contentBuilder;
    }
    
    private String spannableToNoteContent(Spannable spannable) {
        return null;
    }
    
    private void handleBracketSequence(SpannableStringBuilder contentBuilder,
            String bracketSequence, int begin, int end) {
        String name = "";
        HashMap<String, String> attributes = new HashMap<String, String>();
        
    }
    
}
