package com.gmail.tylerfilla.android.notes.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

public class BillingHelper {
    
    public static final int BILLING_API_VERSION = 3;
    
    public static final class RESPONSE_CODES_HELPER {
        
        public static final int SUCCESS = 0;
        public static final int INTERNAL_EXCEPTION = 1;
        public static final int INVALID_REQUEST_CODE = 2;
        public static final int ERROR_SANITY = 3;
        public static final int ERROR_UNSPECIFIED = 4;
        
        // Flags indicating other response code sets
        public static final int FLAG_VENDING_RESPONSE = 1;
        public static final int FLAG_ACTIVITY_RESULT = 2;
        
    }
    
    public static final class RESPONSE_CODES_VENDING {
        
        public static final int OK = 0;
        public static final int USER_CANCELLED = 1;
        public static final int BILLING_UNAVAILABLE = 2;
        public static final int ITEM_UNAVAILABLE = 3;
        public static final int DEVELOPER_ERROR = 4;
        public static final int ERROR = 5;
        public static final int ITEM_ALREADY_OWNED = 6;
        public static final int ITEM_NOT_OWNED = 7;
        
    }
    
    private Context context;
    
    private IInAppBillingService service;
    private ServiceConnection serviceConnection;
    
    private ServiceConnectionProxy serviceConnectionProxy;
    
    private boolean connected;
    
    private boolean supportInAppBilling;
    private boolean supportSubscriptions;
    
    private OpenPurchaseInfo openPurchaseInfo;
    
    public BillingHelper(Context context) {
        this.context = context;
        
        // Create service connection
        this.serviceConnection = new ServiceConnection() {
            
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // Set service reference 
                BillingHelper.this.service = IInAppBillingService.Stub.asInterface(service);
                
                // Set connected flag
                BillingHelper.this.connected = true;
                
                // Call proxy method
                if (BillingHelper.this.serviceConnectionProxy != null) {
                    BillingHelper.this.serviceConnectionProxy.onServiceConnected(name, service);
                }
            }
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
                // Clear service reference
                BillingHelper.this.service = null;
                
                // Clear connected flag
                BillingHelper.this.connected = false;
                
                // Call proxy method
                if (BillingHelper.this.serviceConnectionProxy != null) {
                    BillingHelper.this.serviceConnectionProxy.onServiceDisconnected(name);
                }
            }
            
        };
    }
    
    public void setServiceConnectionProxy(ServiceConnectionProxy serviceConnectionProxy) {
        this.serviceConnectionProxy = serviceConnectionProxy;
    }
    
    public boolean getConnected() {
        return this.connected;
    }
    
    public boolean getSupportInAppBilling() {
        return this.supportInAppBilling;
    }
    
    public boolean getSupportSubscriptions() {
        return this.supportSubscriptions;
    }
    
    public OpenPurchaseInfo getOpenPurchaseInfo() {
        return this.openPurchaseInfo;
    }
    
    public void bind() throws BillingHelperException {
        // Create bind intent
        Intent bindIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        bindIntent.setPackage("com.android.vending");
        
        // If billing service doesn't exist
        if (this.context.getPackageManager().queryIntentServices(bindIntent, 0).isEmpty()) {
            throw new BillingHelperException("Billing service is unavailable for binding");
        }
        
        // Bind to service
        this.context.bindService(bindIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    public void unbind() {
        if (this.connected) {
            this.context.unbindService(this.serviceConnection);
        } else {
            throw new IllegalStateException("Service has not been bound");
        }
    }
    
    public void setup() {
        if (this.connected) {
            // Check if in-app billing is supported
            int responseInAppBillingSupported = -1;
            try {
                responseInAppBillingSupported = this.service.isBillingSupported(BILLING_API_VERSION, this.context.getPackageName(), ProductType.IN_APP.getInternalName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            
            // If a response was received
            if (responseInAppBillingSupported > -1) {
                // Anything but OK is bad
                this.supportInAppBilling = responseInAppBillingSupported == RESPONSE_CODES_VENDING.OK;
                
                // Lack of in-app billing support implies lack of subscription support
                if (!this.supportInAppBilling) {
                    this.supportSubscriptions = false;
                }
            }
            
            // If in-app billing is supported (subscriptions might be supported)
            if (this.supportInAppBilling) {
                // Check if subscriptions are supported
                int responseSubscriptionsSupported = -1;
                try {
                    responseSubscriptionsSupported = this.service.isBillingSupported(BILLING_API_VERSION, this.context.getPackageName(), ProductType.SUBSCRIPTION.getInternalName());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                
                // If a response was received
                if (responseSubscriptionsSupported > -1) {
                    // Anything but OK is bad
                    this.supportSubscriptions = responseSubscriptionsSupported == RESPONSE_CODES_VENDING.OK;
                }
            }
        }
    }
    
    public void purchase(Activity activity, int requestCode, String productId, ProductType productType, String developerPayload, PurchaseCallback callback) throws BillingHelperException {
        // Sanity check for in-app billing support
        if (productType == ProductType.IN_APP && !this.supportInAppBilling) {
            throw new BillingMethodUnsupportedException("In-app billing is not supported");
        }
        
        // Sanity check for subscription support
        if (productType == ProductType.SUBSCRIPTION && !this.supportSubscriptions) {
            throw new BillingMethodUnsupportedException("Subscriptions are not supported");
        }
        
        try {
            // Get bundle for buy intent
            Bundle intentBundle = this.service.getBuyIntent(BILLING_API_VERSION, this.context.getPackageName(), productId, productType.getInternalName(), developerPayload);
            
            // Extract response code from bundle
            int responseCode = -1;
            Object responseCodeObject = intentBundle.get("RESPONSE_CODE");
            if (responseCodeObject == null) {
                responseCode = RESPONSE_CODES_VENDING.OK;
            } else if (responseCodeObject instanceof Integer) {
                responseCode = (Integer) responseCodeObject;
            } else if (responseCodeObject instanceof Long) {
                responseCode = (int) ((Long) responseCodeObject).longValue();
            }
            
            // If response code extracted
            if (responseCode > -1) {
                if (responseCode == RESPONSE_CODES_VENDING.OK) {
                    // Get intent from bundle
                    PendingIntent intent = intentBundle.getParcelable("BUY_INTENT");
                    
                    // Start intent sender
                    activity.startIntentSenderForResult(intent.getIntentSender(), requestCode, new Intent(), 0, 0, 0);
                    
                    // Set open purchase info
                    this.openPurchaseInfo = new OpenPurchaseInfo();
                    this.openPurchaseInfo.callback = callback;
                    this.openPurchaseInfo.productType = productType;
                    this.openPurchaseInfo.requestCode = requestCode;
                } else {
                    callback.onPurchaseCompleted(new Response(responseCode, null, RESPONSE_CODES_HELPER.FLAG_VENDING_RESPONSE), null);
                }
            } else {
                throw new BillingHelperException("No response code extracted from buy intent");
            }
        } catch (RemoteException e) {
            callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.INTERNAL_EXCEPTION, e, 0), null);
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.INTERNAL_EXCEPTION, e, 0), null);
            e.printStackTrace();
        }
    }
    
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        // Check for open purchase
        if (this.openPurchaseInfo == null) {
            return false;
        }
        
        // Sanity check for intent data
        if (data == null) {
            this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.ERROR_SANITY, new Throwable("Intent data null"), 0), null);
            return true;
        }
        
        // Check for request code
        if (requestCode != this.openPurchaseInfo.requestCode) {
            this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.INVALID_REQUEST_CODE, null, 0), null);
            return false;
        }
        
        // Check for activity result code
        if (resultCode != Activity.RESULT_OK) {
            this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(resultCode, null, RESPONSE_CODES_HELPER.FLAG_ACTIVITY_RESULT), null);
            return true;
        }
        
        // Extract response code from intent extras
        int responseCode = -1;
        Object responseCodeObject = data.getExtras().get("RESPONSE_CODE");
        if (responseCodeObject == null) {
            responseCode = RESPONSE_CODES_VENDING.OK;
        } else if (responseCodeObject instanceof Integer) {
            responseCode = (Integer) responseCodeObject;
        } else if (responseCodeObject instanceof Long) {
            responseCode = (int) ((Long) responseCodeObject).longValue();
        }
        
        // If response code extracted
        if (responseCode > -1) {
            if (responseCode == RESPONSE_CODES_VENDING.OK) {
                // Extract purchase data
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                
                // Sanity check for extracted purchase data
                if (purchaseData == null || dataSignature == null) {
                    this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.ERROR_SANITY, new Throwable("Extracted purchase data null"), 0), null);
                }
                
                // Interpret purchase data as JSON
                JSONObject purchaseDataJson = null;
                try {
                    purchaseDataJson = new JSONObject(purchaseData);
                } catch (JSONException e) {
                    this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.INTERNAL_EXCEPTION, e, 0), null);
                    e.printStackTrace();
                }
                
                // If interpretation successful
                if (purchaseDataJson != null) {
                    // Store purchase info
                    PurchaseInfo purchaseInfo = new PurchaseInfo();
                    purchaseInfo.productType = this.openPurchaseInfo.productType;
                    purchaseInfo.autoRenewing = purchaseDataJson.optBoolean("autoRenewing");
                    purchaseInfo.orderId = purchaseDataJson.optString("orderId");
                    purchaseInfo.packageName = purchaseDataJson.optString("packageName");
                    purchaseInfo.productId = purchaseDataJson.optString("productId");
                    purchaseInfo.purchaseTime = purchaseDataJson.optLong("purchaseTime");
                    purchaseInfo.purchaseState = PurchaseInfo.State.fromInternalId(purchaseDataJson.optInt("purchaseState"));
                    purchaseInfo.developerPayload = purchaseDataJson.optString("developerPayload");
                    purchaseInfo.purchaseToken = purchaseDataJson.optString("purchaseToken");
                    
                    // Callback
                    this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.SUCCESS, null, 0), purchaseInfo);
                } else {
                    this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.ERROR_UNSPECIFIED, new Throwable("Purchase data JSONObject null"), 0), null);
                }
            } else {
                this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(responseCode, null, RESPONSE_CODES_HELPER.FLAG_VENDING_RESPONSE), null);
            }
        } else {
            this.openPurchaseInfo.callback.onPurchaseCompleted(new Response(RESPONSE_CODES_HELPER.ERROR_UNSPECIFIED, new Throwable("No response code extracted from intent extras"), 0), null);
        }
        
        // Clear open purchase info
        this.openPurchaseInfo = null;
        
        return true;
    }
    
    public interface ServiceConnectionProxy {
        
        void onServiceConnected(ComponentName name, IBinder service);
        
        void onServiceDisconnected(ComponentName name);
        
    }
    
    public interface PurchaseCallback {
        
        void onPurchaseCompleted(Response response, PurchaseInfo purchaseInfo);
        
    }
    
    public static class Response {
        
        private int responseCode;
        private Throwable errorCause;
        private int flags;
        
        private Response(int responseCode, Throwable errorCause, int flags) {
            this.responseCode = responseCode;
            this.errorCause = errorCause;
            this.flags = flags;
        }
        
        public int getResponseCode() {
            return this.responseCode;
        }
        
        public Throwable getErrorCause() {
            return this.errorCause;
        }
        
        public int getFlags() {
            return this.flags;
        }
        
    }
    
    public static class PurchaseInfo {
        
        private ProductType productType;
        private boolean autoRenewing;
        private String orderId;
        private String packageName;
        private String productId;
        private long purchaseTime;
        private State purchaseState;
        private String developerPayload;
        private String purchaseToken;
        
        private PurchaseInfo() {
        }
        
        public ProductType getProductType() {
            return this.productType;
        }
        
        public boolean getAutoRenewing() {
            return this.autoRenewing;
        }
        
        public String getOrderId() {
            return this.orderId;
        }
        
        public String getPackageName() {
            return this.packageName;
        }
        
        public String getProductId() {
            return this.productId;
        }
        
        public long getPurchaseTime() {
            return this.purchaseTime;
        }
        
        public State getPurchaseState() {
            return this.purchaseState;
        }
        
        public String getDeveloperPayload() {
            return this.developerPayload;
        }
        
        public String getPurchaseToken() {
            return this.purchaseToken;
        }
        
        public enum State {
            
            PURCHASED(0),
            CANCELLED(1),
            REFUNDED(2);
            
            private int internalId;
            
            State(int internalId) {
                this.internalId = internalId;
            }
            
            public int getInternalId() {
                return this.internalId;
            }
            
            private static State fromInternalId(int internalId) {
                for (State state : values()) {
                    if (state.internalId == internalId) {
                        return state;
                    }
                }
                
                return null;
            }
            
        }
        
    }
    
    public static class OpenPurchaseInfo {
        
        private PurchaseCallback callback;
        private ProductType productType;
        private int requestCode;
        
        private OpenPurchaseInfo() {
        }
        
        public PurchaseCallback getCallback() {
            return this.callback;
        }
        
        public ProductType getProductType() {
            return this.productType;
        }
        
        public int getRequestCode() {
            return this.requestCode;
        }
        
    }
    
    public static class BillingHelperException extends Exception {
        
        public BillingHelperException(String message) {
            super(message);
        }
        
        public BillingHelperException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
    public static class BillingMethodUnsupportedException extends BillingHelperException {
        
        public BillingMethodUnsupportedException(String message) {
            super(message);
        }
        
        public BillingMethodUnsupportedException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
    public enum ProductType {
        
        IN_APP("inapp"),
        SUBSCRIPTION("subs");
        
        private String internalName;
        
        ProductType(String internalName) {
            this.internalName = internalName;
        }
        
        public String getInternalName() {
            return this.internalName;
        }
        
        private static ProductType fromInternalName(String internalName) {
            for (ProductType productType : values()) {
                if (productType.internalName.equals(internalName)) {
                    return productType;
                }
            }
            
            return null;
        }
        
    }
    
}
