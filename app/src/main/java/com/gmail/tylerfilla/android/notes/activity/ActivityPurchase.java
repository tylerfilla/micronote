package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;

import com.gmail.tylerfilla.android.notes.util.BillingHelper;

import java.util.Random;

public class ActivityPurchase extends Activity {
    
    private static final int PURCHASE_REQUEST_CODE = -5897;
    
    private static final int DEVELOPER_PAYLOAD_GENERATOR_MAX_ITERATIONS = 20;
    private static final int DEVELOPER_PAYLOAD_GENERATOR_MIN_ITERATIONS = 5;
    private static final String DEVELOPER_PAYLOAD_GENERATOR_SEED = "++I am a seed! YAY!++3.141592653589793238462643383";
    
    private String productId;
    
    private BillingHelper billingHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get product ID from intent data
        this.productId = this.getIntent().getDataString();
        
        // If a product ID wasn't given
        if (this.productId == null || this.productId.isEmpty()) {
            this.finish();
        }
        
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
        System.out.println("result");
        
        // If billing helper cannot handle this result
        if (!this.billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Call through to super
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void process() {
        // Set up billing helper
        this.billingHelper.setup();
        
        // Generate a random developer payload
        final String developerPayload = this.generateDeveloperPayload();
        
        // Attempt to make purchase
        try {
            this.billingHelper.purchase(this, PURCHASE_REQUEST_CODE, this.productId, BillingHelper.ProductType.IN_APP, developerPayload, new BillingHelper.PurchaseCallback() {
                
                @Override
                public void onPurchaseCompleted(BillingHelper.Response response, BillingHelper.PurchaseInfo purchaseInfo) {
                    // If response code is for billing helper
                    if (response.getFlags() == 0) {
                        // If successfully purchased
                        if (response.getResponseCode() == BillingHelper.RESPONSE_CODES_HELPER.SUCCESS) {
                            System.out.println("Success!");
                        }
                    }
                    
                    // Finish activity
                    ActivityPurchase.this.finish();
                }
                
            });
        } catch (BillingHelper.BillingHelperException e) {
            e.printStackTrace();
        }
    }
    
    private String generateDeveloperPayload() {
        // Create pRNG
        Random random = new Random(SystemClock.uptimeMillis());
        
        // Calculate number of iterations
        int numIterations = DEVELOPER_PAYLOAD_GENERATOR_MIN_ITERATIONS + random.nextInt(DEVELOPER_PAYLOAD_GENERATOR_MAX_ITERATIONS - DEVELOPER_PAYLOAD_GENERATOR_MIN_ITERATIONS);
        
        // Start with seed
        String payloadString = DEVELOPER_PAYLOAD_GENERATOR_SEED;
        
        // Generate payload
        for (int i = 0; i < numIterations; i++) {
            // Generate a mutation
            char mutation = (char) (random.nextInt() & 0xFFFF);
            
            // Mutate all characters
            for (int ci = 0; ci < payloadString.length(); ci++) {
                if (ci == 0) {
                    payloadString = String.valueOf((char) (payloadString.charAt(0) ^ mutation)) + payloadString.substring(1, payloadString.length());
                } else if (ci == payloadString.length() - 1) {
                    payloadString = payloadString.substring(0, payloadString.length() - 1) + String.valueOf((char) (payloadString.charAt(payloadString.length() - 1) ^ mutation));
                } else {
                    payloadString = payloadString.substring(0, ci) + String.valueOf((char) (payloadString.charAt(ci) ^ mutation)) + payloadString.substring(ci + 1, payloadString.length());
                }
            }
        }
        
        return payloadString;
    }
    
}
