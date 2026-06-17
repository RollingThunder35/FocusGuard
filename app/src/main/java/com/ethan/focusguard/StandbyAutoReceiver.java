package com.ethan.focusguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class StandbyAutoReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_POWER_CONNECTED.equals(action) || Intent.ACTION_DOCK_EVENT.equals(action)) {
            if (!FocusManager.isAutoStandbyOnCharge(context)) return;
            try {
                FocusManager.openStandbyClock(context, true, "charging");
            } catch (Exception e) {
                Toast.makeText(context, "FocusGuard standby is ready. Open the app if Android blocks auto launch.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
