package com.ethan.focusguard;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

public class FocusAccessibilityService extends AccessibilityService {
    private long lastBlockLaunch = 0L;
    private String lastBlockedPackage = "";
    private final Handler handler = new Handler();

    private final Runnable idleCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (FocusManager.shouldAutoStartFromIdle(FocusAccessibilityService.this)) {
                try {
                    FocusManager.openStandbyClock(FocusAccessibilityService.this, true, "idle");
                } catch (Exception ignored) {
                    // Some Android versions block background activity launches. Manual standby still works.
                }
            }
            handler.postDelayed(this, 15_000L);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        FocusManager.recordUserActivity(this);
        FocusNotificationHelper.prepare(this);
        FocusNotificationHelper.update(this);
        handler.post(idleCheckRunnable);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        FocusNotificationHelper.update(this);

        if (event == null) return;
        FocusManager.recordUserActivity(this);
        if (event.getPackageName() == null) return;

        String packageName = event.getPackageName().toString();
        if (packageName.equals(getPackageName())) return;
        if (!FocusManager.isFocusActive(this)) return;
        if (!FocusManager.isBlocked(this, packageName)) return;

        long now = System.currentTimeMillis();
        if (packageName.equals(lastBlockedPackage) && now - lastBlockLaunch < 1200) return;
        lastBlockedPackage = packageName;
        lastBlockLaunch = now;

        Intent intent = new Intent(this, BlockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("blockedPackage", packageName);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(idleCheckRunnable);
    }
}
