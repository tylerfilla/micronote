package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.util.DimenUtil;
import com.google.android.gms.common.GoogleApiAvailability;

public class ActivityAbout extends Activity {
    
    private static final String ASSET_PATH_ABOUT_HTML_INDEX = "file:///android_asset/about_html/about.html";
    
    private AlertDialog dialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create dialog web view
        WebView dialogView = new WebView(this);
        
        // Dialog web view layout parameters
        FrameLayout.LayoutParams dialogViewLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogViewLayoutParams.setMargins(DimenUtil.dpToPxInt(this, 24), DimenUtil.dpToPxInt(this, 20), DimenUtil.dpToPxInt(this, 24), DimenUtil.dpToPxInt(this, 24));
        dialogView.setLayoutParams(dialogViewLayoutParams);
        
        // Set web Chrome client for handling alert messages
        dialogView.setWebChromeClient(new WebChromeClient() {
            
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                // Dismiss alert "dialog"
                result.confirm();
                
                System.out.println(message);
                
                if ("get_license_google_play_services".equals(message)) {
                    // Write Google Play Services license
                    view.loadUrl("javascript:document.write('" + GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(ActivityAbout.this).replaceAll("\\'", "&#39;") + "');");
                }
                
                return true;
            }
            
        });
        
        // Enable JavaScript
        dialogView.getSettings().setJavaScriptEnabled(true);
        
        // Load about document
        dialogView.loadUrl(ASSET_PATH_ABOUT_HTML_INDEX);
        
        // Set dialog web view background transparent
        dialogView.setBackgroundColor(0);
        
        // Build dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setIcon(R.drawable.ic_launcher);
        dialogBuilder.setTitle(R.string.dialog_activity_about_title);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(android.R.string.ok, null);
        
        // Create dialog
        this.dialog = dialogBuilder.create();
        
        // Set dismiss listener
        this.dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Finish about activity
                ActivityAbout.this.finish();
            }
            
        });
        
        // Show dialog
        this.dialog.show();
    }
    
}
