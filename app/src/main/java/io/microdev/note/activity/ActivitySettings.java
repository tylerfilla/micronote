package io.microdev.note.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.microdev.note.R;
import io.microdev.note.util.AdRemovalUtil;

public class ActivitySettings extends AppCompatPreferenceActivity {
    
    private static final String BILLING_SKU_AD_REMOVAL = "ad_removal";
    private static final String PREF_NAME_UPGRADE = "pref_upgrade";
    
    private Toolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        
        // Add preferences
        this.addPreferencesFromResource(R.xml.pref);
        
        // Set content view
        this.setContentView(R.layout.activity_settings);
        
        // Configure toolbar
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.activitySettingsToolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        
        // Destroy ad removal utility
        AdRemovalUtil.destroy();
    }
    
}
