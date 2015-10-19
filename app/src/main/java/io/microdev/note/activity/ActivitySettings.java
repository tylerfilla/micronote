package io.microdev.note.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import io.microdev.note.R;
import io.microdev.note.util.AdRemovalUtil;

public class ActivitySettings extends PreferenceActivity {
    
    private static final String BILLING_SKU_AD_REMOVAL = "ad_removal";
    private static final String PREF_NAME_UPGRADE = "pref_upgrade";
    
    private AppCompatDelegate appCompatDelegate;
    
    private Toolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getAppCompatDelegate().installViewFactory();
        this.getAppCompatDelegate().onCreate(savedInstanceState);
        
        super.onCreate(savedInstanceState);
        
        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        
        // Add preferences
        this.addPreferencesFromResource(R.xml.pref);
        
        // Set content view
        this.setContentView(R.layout.activity_settings);
        
        // Configure toolbar
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.activitySettingsToolbar);
        this.getAppCompatDelegate().setSupportActionBar(toolbar);
        this.getAppCompatDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ActivitySettings.this.finish();
            }
            
        });
        
        // Configure ad removal utility
        AdRemovalUtil.create(this, new AdRemovalUtil.Callback() {
            
            @Override
            public void callback() {
                // Remove upgrade category if it shouldn't be shown
                if (AdRemovalUtil.checkAdRemovalStatus()) {
                    ActivitySettings.this.getPreferenceScreen().removePreference(ActivitySettings.this.findPreference(PREF_NAME_UPGRADE));
                }
            }
            
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        this.getAppCompatDelegate().onDestroy();
        
        // Destroy ad removal utility
        AdRemovalUtil.destroy();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        this.getAppCompatDelegate().onStop();
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        this.getAppCompatDelegate().onPostCreate(savedInstanceState);
    }
    
    @Override
    protected void onPostResume() {
        super.onPostResume();
        
        this.getAppCompatDelegate().onPostResume();
    }
    
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        this.getAppCompatDelegate().addContentView(view, params);
    }
    
    @Override
    public void setContentView(int layoutResID) {
        this.getAppCompatDelegate().setContentView(layoutResID);
    }
    
    @Override
    public void setContentView(View view) {
        this.getAppCompatDelegate().setContentView(view);
    }
    
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        this.getAppCompatDelegate().setContentView(view, params);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        this.getAppCompatDelegate().onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        
        this.getAppCompatDelegate().setTitle(title);
    }
    
    @Override
    public MenuInflater getMenuInflater() {
        return this.getAppCompatDelegate().getMenuInflater();
    }
    
    @Override
    public void invalidateOptionsMenu() {
        this.getAppCompatDelegate().invalidateOptionsMenu();
    }
    
    private AppCompatDelegate getAppCompatDelegate() {
        return this.appCompatDelegate == null ? this.appCompatDelegate = AppCompatDelegate.create(this, null) : this.appCompatDelegate;
    }
    
}
