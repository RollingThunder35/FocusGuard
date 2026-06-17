package com.ethan.focusguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FocusActionReceiver extends BroadcastReceiver {
    public static final String ACTION_STOP_TIMER = "com.ethan.focusguard.ACTION_STOP_TIMER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_STOP_TIMER.equals(intent.getAction())) {
            FocusManager.stopTimer(context);
            FocusNotificationHelper.update(context);
        }
    }
}
