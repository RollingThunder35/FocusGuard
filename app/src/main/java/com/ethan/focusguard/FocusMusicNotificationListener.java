package com.ethan.focusguard;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class FocusMusicNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        Notification notification = sbn.getNotification();
        if (!looksLikeMusic(notification, sbn.getPackageName())) return;

        Bundle extras = notification.extras;
        if (extras == null) return;
        CharSequence titleCs = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence textCs = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subTextCs = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        String title = titleCs == null ? "" : titleCs.toString().trim();
        String text = textCs == null ? "" : textCs.toString().trim();
        String subText = subTextCs == null ? "" : subTextCs.toString().trim();
        if (title.isEmpty()) return;
        String detail = !text.isEmpty() ? text : subText;
        FocusManager.setNowPlaying(this, appLabel(sbn.getPackageName()), title, detail);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn != null && looksLikeMusic(sbn.getNotification(), sbn.getPackageName())) {
            FocusManager.clearNowPlaying(this);
        }
    }

    private boolean looksLikeMusic(Notification notification, String packageName) {
        if (notification == null) return false;
        if (Notification.CATEGORY_TRANSPORT.equals(notification.category)) return true;
        String pkg = packageName == null ? "" : packageName.toLowerCase();
        return pkg.contains("spotify")
                || pkg.contains("music")
                || pkg.contains("youtube")
                || pkg.contains("soundcloud")
                || pkg.contains("deezer")
                || pkg.contains("tidal")
                || pkg.contains("podcast");
    }

    private String appLabel(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            CharSequence label = pm.getApplicationLabel(info);
            return label == null ? "Music" : label.toString();
        } catch (Exception ignored) {
            return "Music";
        }
    }
}
