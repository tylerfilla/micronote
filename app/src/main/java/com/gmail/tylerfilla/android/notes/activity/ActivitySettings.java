package com.gmail.tylerfilla.android.notes.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import com.example.android.trivialdrivesample.util.IabException;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.util.PublicKeyUtil;

import java.util.Collections;

public class ActivitySettings extends AppCompatPreferenceActivity {
    
    private static final String BILLING_SKU_AD_REMOVAL = "ad_removal";
    private static final String PREF_NAME_UPGRADE = "pref_upgrade";
    
    private Toolbar toolbar;
    
    private IabHelper iabHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        
        // Add preferences
        this.addPreferencesFromResource(R.xml.pref);
        
        // Set content view
        this.setContentView(R.layout.activity_settings);
        
        // Set toolbar
        this.setSupportActionBar((Toolbar) this.findViewById(R.id.activitySettingsToolbar));
        
        // Create IAB helper
        this.iabHelper = new IabHelper(this, PublicKeyUtil.getPublicKey());
        
        // Enable IAB helper debugging
        this.iabHelper.enableDebugLogging(true);
        
        // Start IAB helper setup
        this.iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            
            @Override
            public void onIabSetupFinished(IabResult result) {
                // If successful, handle ad visibility
                if (result.isSuccess()) {
                    ActivitySettings.this.handleUpgradeVisibility();
                }
            }
            
        });
    }
    
    private void handleUpgradeVisibility() {
        // Whether or not upgrade category should be shown
        boolean showUpgrade = true;
        
        // Query user's purchases
        Inventory inventory = null;
        try {
            inventory = this.iabHelper.queryInventory(false, Collections.singletonList(BILLING_SKU_AD_REMOVAL));
        } catch (IabException e) {
            e.printStackTrace();
        }
        
        // Check for presence of ad removal purchase
        if (inventory != null) {
            showUpgrade = !inventory.hasPurchase(BILLING_SKU_AD_REMOVAL);
        }
        
        // Remove upgrade category if it shouldn't be shown
        if (!showUpgrade) {
            this.getPreferenceScreen().removePreference(this.findPreference(PREF_NAME_UPGRADE));
        }
    }
    
}
