package com.gmail.tylerfilla.android.notes.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.gmail.tylerfilla.android.notes.R;

public class NoteEditor extends EditText {
    
    private final Paint notepadLinePaint;
    private final Rect firstLineBounds;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.setGravity(Gravity.TOP | Gravity.LEFT);
        
        this.notepadLinePaint = new Paint();
        this.notepadLinePaint.setColor(this.getContext().getResources().getColor(R.color.pad_line));
        this.notepadLinePaint.setStrokeWidth(2);
        
        this.firstLineBounds = new Rect();
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
