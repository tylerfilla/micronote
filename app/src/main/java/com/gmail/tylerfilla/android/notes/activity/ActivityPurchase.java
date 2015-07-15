package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.os.Bundle;

public class ActivityPurchase extends Activity {
    
    private static final int PURCHASE_REQUEST_CODE = -5897;
    
    private String productId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get product ID from intent data
        this.productId = this.getIntent().getDataString();
        
        // If a product ID wasn't given
        if (this.productId == null || this.productId.isEmpty()) {
            this.finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
}
