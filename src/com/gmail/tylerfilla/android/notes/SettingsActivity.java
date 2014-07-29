package com.gmail.tylerfilla.android.notes;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
        
        this.getActionBar().setCustomView(R.layout.activity_settings_actionbar);
        this.setContentView(R.layout.activity_settings);
    }
    
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
    
    public static class SettingsFragment extends PreferenceFragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.addPreferencesFromResource(R.xml.pref);
        }
        
    }
    
}
