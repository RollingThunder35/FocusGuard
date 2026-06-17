package com.ethan.focusguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FocusAlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_REFRESH = "com.ethan.focusguard.ACTION_REFRESH";

    @Override
    public void onReceive(Context context, Intent intent) {
        FocusNotificationHelper.update(context);
    }
}
