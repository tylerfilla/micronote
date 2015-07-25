package com.gmail.tylerfilla.android.notes.widget.pref;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class SwitchPreferenceCompat extends CheckBoxPreference {
    
    public SwitchPreferenceCompat(Context context) {
        super(context);
    }
    
    public SwitchPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public SwitchPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwitchPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    @Override
    protected View onCreateView(ViewGroup parent) {
        // Create view with checkbox
        View view = super.onCreateView(parent);
        
        // Get widget frame
        ViewGroup widgetFrame = (ViewGroup) view.findViewById(android.R.id.widget_frame);
        
        // Remove all views within widget frame
        widgetFrame.removeAllViews();
        
        // Create switch and pass it off as a checkbox
        SwitchCompat switchCompat = new SwitchCompat(this.getContext());
        switchCompat.setId(android.R.id.checkbox);
        switchCompat.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        switchCompat.setBackgroundDrawable(null);
        switchCompat.setClickable(false);
        switchCompat.setFocusable(false);
        
        // Add switch to widget frame
        widgetFrame.addView(switchCompat);
        
        return view;
    }
    
}
