package com.gmail.tylerfilla.android.notes.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
    
}
