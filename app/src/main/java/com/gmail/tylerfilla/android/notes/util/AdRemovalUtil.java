package com.gmail.tylerfilla.android.notes.util;

import android.content.Context;
import android.provider.Settings;

import com.example.android.trivialdrivesample.util.IabException;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AdRemovalUtil {
    
    private static final HashSet<String> TEST_DEVICE_IDS = new HashSet<>();
    
    public static final String BILLING_SKU_AD_REMOVAL = "ad_removal";
    public static final String BILLING_TYPE_AD_REMOVAL = "inapp";
    
    private static final boolean DEBUG_MODE = true;
    
    private static IabHelper currentIabHelper;
    
    private static String deviceId;
    
    public static void create(Context context, final Callback callback) {
        // IabHelper must be disposed of first
        if (currentIabHelper != null) {
            destroy();
        }
        
        // Obtain device ID
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        
        // Instantiate Android sample in-app billing helper
        final IabHelper iabHelper = new IabHelper(context, PublicKeyUtil.getPublicKey());
        
        // Enable debug logging
        iabHelper.enableDebugLogging(DEBUG_MODE);
        
        // Start setup
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    // Set current IabHelper
                    currentIabHelper = iabHelper;
                    
                    // Call back
                    callback.callback();
                }
            }
            
        });
    }
    
    public static void destroy() {
        if (currentIabHelper != null) {
            // Dispose of IabHelper
            currentIabHelper.dispose();
            currentIabHelper = null;
        }
    }
    
    public static boolean checkAdRemovalStatus() {
        // Sanity check
        if (currentIabHelper == null) {
            return false;
        }
        
        // If this is a test device, remove ads
        if (TEST_DEVICE_IDS.contains(deviceId)) {
            return true;
        }
        
        // Query user's purchases
        Inventory inventory = null;
        try {
            inventory = currentIabHelper.queryInventory(false, Collections.singletonList(BILLING_SKU_AD_REMOVAL));
        } catch (IabException e) {
            e.printStackTrace();
        }
        
        // Check for presence of ad removal purchase
        if (inventory != null) {
            return inventory.hasPurchase(BILLING_SKU_AD_REMOVAL);
        } else {
            return false;
        }
    }
    
    public interface Callback {
        
        void callback();
        
    }
    
    static {
        TEST_DEVICE_IDS.add("b3240e21135584c2");
    }
    
}
