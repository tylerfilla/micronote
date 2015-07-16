package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Purchase;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.util.PublicKeyUtil;

public class ActivityPurchase extends Activity {
    
    private static final String EXTRA_TYPE_NAME = "TYPE";
    private static final String EXTRA_TYPE_DEFAULT = "inapp";
    
    private static final int PURCHASE_REQUEST_CODE = -5897;
    
    private String productId;
    private String productType;
    
    private IabHelper iabHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get product ID from intent data
        this.productId = this.getIntent().getDataString();
        
        // Get product type from intent extras
        this.productType = this.getIntent().hasExtra(EXTRA_TYPE_NAME) ? this.getIntent().getStringExtra(EXTRA_TYPE_NAME) : EXTRA_TYPE_DEFAULT;
        
        // If a product ID wasn't given
        if (this.productId == null || this.productId.isEmpty()) {
            this.finish();
        }
        
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
        
        // Create a proxy view for handling touches
        final View touchProxy = new View(this);
        touchProxy.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        touchProxy.setFocusable(true);
        touchProxy.requestFocusFromTouch();
        
        // Set touch proxy as content view
        this.setContentView(touchProxy);
        
        // Set listener for touch proxy
        touchProxy.setOnTouchListener(new View.OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Finish purchase activity
                ActivityPurchase.this.finish();
                
                // Nullify touch listener
                touchProxy.setOnTouchListener(null);
                
                return true;
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
                
                // Finish
                ActivityPurchase.this.finish();
            }
            
        }, null);
    }
    
}
