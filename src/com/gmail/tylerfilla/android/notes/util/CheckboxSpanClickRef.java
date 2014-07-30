package com.gmail.tylerfilla.android.notes.util;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class CheckboxSpanClickRef extends ClickableSpan {
    
    private final CheckboxSpan checkboxSpan;
    
    public CheckboxSpanClickRef(CheckboxSpan checkboxSpan) {
        this.checkboxSpan = checkboxSpan;
    }
    
    @Override
    public void onClick(View widget) {
        this.checkboxSpan.onClick(widget);
    }
    
    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setTextScaleX(0.0f);
    }
    
}
