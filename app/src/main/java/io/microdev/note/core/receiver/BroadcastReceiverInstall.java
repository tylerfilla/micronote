package io.microdev.note.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tappx.TrackInstall;

public class BroadcastReceiverInstall extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
                new TrackInstall().onReceive(context, intent);
            }
        }
    }
    
}
