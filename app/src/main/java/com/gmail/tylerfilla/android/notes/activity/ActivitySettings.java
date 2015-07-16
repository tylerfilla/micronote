package com.gmail.tylerfilla.android.notes.activity;

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

import com.example.android.trivialdrivesample.util.IabException;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.util.PublicKeyUtil;

import java.util.Collections;

public class ActivitySettings extends PreferenceActivity {
    
    private static final String BILLING_SKU_AD_REMOVAL = "android.test.purchased";
    
    private boolean upgradeCategoryVisible;
    
    private FragmentSettings fragmentSettings;
    private Toolbar toolbar;
    
    private IabHelper iabHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        
        // Default this to true
        this.upgradeCategoryVisible = true;
        
        // Set content view
        this.setContentView(R.layout.activity_settings);
        
        // Get reference to toolbar
        this.toolbar = (Toolbar) this.findViewById(R.id.activitySettingsToolbar);
        
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
            // Set flag for future settings fragment
            this.upgradeCategoryVisible = false;
            
            // If settings fragment already exists
            if (this.fragmentSettings != null) {
                this.fragmentSettings.removeUpgradeCategory();
            }
        }
    }
    
    public static class FragmentSettings extends PreferenceFragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load preferences
            this.addPreferencesFromResource(R.xml.pref);
            
            // Set reference to this fragment in activity
            ((ActivitySettings) this.getActivity()).fragmentSettings = this;
            
            // Handle upgrade category removal
            if (!((ActivitySettings) this.getActivity()).upgradeCategoryVisible) {
                this.removeUpgradeCategory();
            }
        }
        
        private void removeUpgradeCategory() {
            // Remove upgrade category
            this.getPreferenceScreen().removePreference(this.findPreference("pref_upgrade"));
        }
        
    }
    
}
