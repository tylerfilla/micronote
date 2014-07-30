package com.gmail.tylerfilla.android.notes.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class CheckboxSpan extends ReplacementSpan {
    
    private final TextView textView;
    private boolean checked;
    
    public CheckboxSpan(TextView textView) {
        this.textView = textView;
        this.checked = false;
    }
    
    public boolean getChecked() {
        return this.checked;
    }
    
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    
    public void onClick(View view) {
        this.checked = !this.checked;
        this.textView.postInvalidate();
        ((View) this.textView.getParent()).postInvalidate();
        Log.d("", "clicked");
    }
    
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        return 0;
    }
    
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
            int bottom, Paint paint) {
        Log.d("", "draw!!!");
        
        if (this.checked) {
            paint.setColor(0xFFFF0000);
        } else {
            paint.setColor(0xFF000000);
        }
        
        canvas.drawRect(x, top, x + (bottom - top), y, paint);
    }
    
}
