package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Purchase;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.util.PublicKeyUtil;

public class ActivityPurchase extends Activity {
    
    public static final String EXTRA_SKU_NAME = "SKU";
    public static final String EXTRA_TYPE_NAME = "TYPE";
    
    private static final String EXTRA_SKU_DEFAULT = "android.test.purchased";
    private static final String EXTRA_TYPE_DEFAULT = "inapp";
    
    private static final int PURCHASE_REQUEST_CODE = -1337;
    
    private String productId;
    private String productType;
    
    private IabHelper iabHelper;
    
    private boolean enteredPurchaseFlow;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get product ID from intent extras
        this.productId = this.getIntent().hasExtra(EXTRA_SKU_NAME) ? this.getIntent().getStringExtra(EXTRA_SKU_NAME) : EXTRA_SKU_DEFAULT;
        
        // Get product type from intent extras
        this.productType = this.getIntent().hasExtra(EXTRA_TYPE_NAME) ? this.getIntent().getStringExtra(EXTRA_TYPE_NAME) : EXTRA_TYPE_DEFAULT;
        
        // Create IAB helper
        this.iabHelper = new IabHelper(this, PublicKeyUtil.getPublicKey());
        
        // Enable IAB helper debugging
        this.iabHelper.enableDebugLogging(true);
        
        // Start IAB helper setup
        this.iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            
            @Override
            public void onIabSetupFinished(IabResult result) {
                // If successful, continue with process
                if (result.isSuccess()) {
                    ActivityPurchase.this.process();
                }
            }
            
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Dispose IAB helper
        this.iabHelper.dispose();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // If resuming from purchase flow
        if (this.enteredPurchaseFlow) {
            // We're done here
            this.finish();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Pausing without finishing indicates purchase flow
        this.enteredPurchaseFlow = !this.isFinishing();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If IAB helper can't handle the result
        if (!this.iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Call through to super
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void process() {
        // Launch purchase flow
        this.iabHelper.launchPurchaseFlow(this, this.productId, this.productType, PURCHASE_REQUEST_CODE, new IabHelper.OnIabPurchaseFinishedListener() {
            
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase info) {
                // Show a toast
                if (result.isSuccess()) {
                    Toast.makeText(ActivityPurchase.this, ActivityPurchase.this.getString(R.string.toast_activity_purchase_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityPurchase.this, ActivityPurchase.this.getString(R.string.toast_activity_purchase_fail), Toast.LENGTH_SHORT).show();
                }
            }
            
        }, null);
    }
    
}
