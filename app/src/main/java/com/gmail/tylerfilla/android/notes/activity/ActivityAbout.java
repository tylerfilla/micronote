package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
        
        // Handle overriding of programmatic resources
        dialogView.setWebViewClient(new WebViewClient() {
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getPath().endsWith("google_play_services.html")) {
                    view.loadData("<!DOCTYPE html><html><body>" + GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(ActivityAbout.this).replaceAll("\n", "<br />") + "</body></html>", "text/html", "UTF-8");
                    return true;
                }
                
                return false;
            }
            
        });
        
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
