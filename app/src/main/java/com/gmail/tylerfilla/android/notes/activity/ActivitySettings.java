package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.gmail.tylerfilla.android.notes.R;

public class ActivitySettings extends PreferenceActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        
        this.getActionBar().setCustomView(R.layout.activity_settings_actionbar);
        this.setContentView(R.layout.activity_settings);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(this.getTitle().toString(), null, this.getResources().getColor(R.color.task_primary)));
        }
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
            
            // Get version name
            String versionName = null;
            try {
                Activity activity = this.getActivity();
                versionName = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            
            // Set version name in pref_about
            if (versionName != null) {
                Preference prefAbout = this.findPreference("pref_about");
                prefAbout.setTitle(prefAbout.getTitle().toString().replace("{version}", versionName));
            }
        }
    }
    
}
