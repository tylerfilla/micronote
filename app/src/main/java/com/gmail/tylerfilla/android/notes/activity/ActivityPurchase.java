package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.gmail.tylerfilla.android.notes.util.BillingHelper;

public class ActivityPurchase extends Activity {
    
    private BillingHelper billingHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create new billing helper
        this.billingHelper = new BillingHelper(this);
        
        // Establish a service connection proxy
        this.billingHelper.setServiceConnectionProxy(new BillingHelper.ServiceConnectionProxy() {
            
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // Activity process
                ActivityPurchase.this.process();
            }
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
            
        });
        
        // Attempt to bind billing helper
        try {
            this.billingHelper.bind();
        } catch (BillingHelper.BillingHelperException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unbind billing helper
        this.billingHelper.unbind();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If billing helper cannot handle this result
        if (!this.billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Call through to super
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void process() {
        // Set up billing helper
        this.billingHelper.setup();
        
        // TODO: Purchase flow
        
        // Finish activity
        this.finish();
    }
    
}
