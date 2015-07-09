package com.gmail.tylerfilla.android.notes.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gmail.tylerfilla.android.notes.R;

public class ActivitySettings extends PreferenceActivity {
    
    private Toolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        
        // If pre-Honeycomb
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Root for toolbar insertion
            ViewGroup root = (ViewGroup) this.findViewById(android.R.id.content);
            View list = root.getChildAt(0);
            
            // Remove all views from root
            root.removeAllViews();
            
            // Inflate toolbar
            this.toolbar = (Toolbar) this.getLayoutInflater().inflate(R.layout.include_toolbar, root, false);
            
            // Measure toolbar before it is drawn
            this.toolbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            
            // Make room for toolbar
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) list.getLayoutParams();
            layoutParams.topMargin = this.toolbar.getMeasuredHeight();
            list.setLayoutParams(layoutParams);
            
            // Add toolbar and list
            root.addView(this.toolbar);
            root.addView(list);
            
            // Directly add preferences 
            this.addPreferencesFromResource(R.xml.pref);
        } else {
            // Set content view
            this.setContentView(R.layout.activity_settings);
            
            // Get reference to toolbar
            this.toolbar = (Toolbar) this.findViewById(R.id.activitySettingsToolbar);
        }
        
        // Toolbar title
        this.toolbar.setTitle(this.getString(R.string.activity_settings_toolbar_title));
        
        // Toolbar navigation icon
        this.toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        
        // Toolbar navigation click listener
        this.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // Finish settings activity
                ActivitySettings.this.finish();
            }
            
        });
    }
    
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // If pre-Lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            switch (name) {
            case "EditText":
                return new AppCompatEditText(this, attrs);
            case "Spinner":
                return new AppCompatSpinner(this, attrs);
            case "Checkbox":
                return new AppCompatCheckBox(this, attrs);
            case "RadioButton":
                return new AppCompatRadioButton(this, attrs);
            case "CheckedTextView":
                return new AppCompatCheckedTextView(this, attrs);
            }
        }
        
        return super.onCreateView(name, context, attrs);
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FragmentSettings extends PreferenceFragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load preferences
            this.addPreferencesFromResource(R.xml.pref);
        }
        
    }
    
}
