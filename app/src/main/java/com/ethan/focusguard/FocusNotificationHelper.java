package com.ethan.focusguard;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import java.util.Calendar;

public final class FocusNotificationHelper {
    public static final String CHANNEL_ID = "focus_status";
    public static final int NOTIFICATION_ID = 101;
    private static final int REFRESH_REQUEST_CODE = 201;
    private static final String EXTRA_REQUEST_PROMOTED_ONGOING = "android.app.extra.REQUEST_PROMOTED_ONGOING";
    private static final String ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS = "android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS";

    private FocusNotificationHelper() {}

    public static void prepare(Context context) {
        createChannel(context);
        scheduleRefresh(context);
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT < 33) return true;
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public static Intent appNotificationSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        return intent;
    }

    public static Intent liveNotificationSettingsIntent(Context context) {
        Intent promoted = new Intent(ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS);
        promoted.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        promoted.setPackage("com.android.settings");
        if (promoted.resolveActivity(context.getPackageManager()) != null) {
            return promoted;
        }
        return appNotificationSettingsIntent(context);
    }

    public static String liveCapabilityText(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return "Live notification support: unknown";
        try {
            Object result = manager.getClass().getMethod("canPostPromotedNotifications").invoke(manager);
            if (result instanceof Boolean) {
                return (Boolean) result
                        ? "Live Updates: allowed by Android settings"
                        : "Live Updates: off or blocked in Android settings";
            }
        } catch (Exception ignored) {
            // This phone/Android version does not expose the Android 16 promoted notification API.
        }
        return "Live Updates: depends on Samsung One UI/Now Bar support for this app";
    }

    public static void update(Context context) {
        createChannel(context);
        if (FocusManager.isFocusActive(context)) {
            showActiveNotification(context);
        } else {
            cancel(context);
        }
        scheduleRefresh(context);
    }

    public static void cancel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(NOTIFICATION_ID);
    }

    public static void scheduleRefresh(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = refreshPendingIntent(context);
        alarmManager.cancel(pendingIntent);

        long next = nextRefreshMillis(context);
        if (next <= 0L) return;

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, next, pendingIntent);
        }
    }

    private static long nextRefreshMillis(Context context) {
        long now = System.currentTimeMillis();
        long best = Long.MAX_VALUE;

        long timerEnd = FocusManager.getTimerEnd(context);
        if (timerEnd > now) {
            best = Math.min(best, timerEnd + 1000L);
        }

        if (FocusManager.isScheduleEnabled(context)) {
            boolean scheduleActive = FocusManager.isScheduleActiveNow(context);
            int targetMinute = scheduleActive ? FocusManager.getScheduleEnd(context) : FocusManager.getScheduleStart(context);
            long scheduleBoundary = nextOccurrenceMillis(targetMinute, now) + 1000L;
            best = Math.min(best, scheduleBoundary);
        }

        return best == Long.MAX_VALUE ? 0L : best;
    }

    private static long nextOccurrenceMillis(int minuteOfDay, long now) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60);
        c.set(Calendar.MINUTE, minuteOfDay % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= now) c.add(Calendar.DATE, 1);
        return c.getTimeInMillis();
    }

    private static PendingIntent refreshPendingIntent(Context context) {
        Intent intent = new Intent(context, FocusAlarmReceiver.class);
        intent.setAction(FocusAlarmReceiver.ACTION_REFRESH);
        return PendingIntent.getBroadcast(
                context,
                REFRESH_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Focus status",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Shows when FocusGuard is actively blocking distracting apps.");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(channel);
    }

    private static void showActiveNotification(Context context) {
        if (!hasNotificationPermission(context)) return;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(context, FocusActionReceiver.class);
        stopIntent.setAction(FocusActionReceiver.ACTION_STOP_TIMER);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);

        String status = FocusManager.statusText(context);
        boolean timerActive = FocusManager.isTimerActive(context);
        long timerEnd = FocusManager.getTimerEnd(context);

        Bundle extras = new Bundle();
        extras.putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true);
        extras.putInt("android.ongoingActivityNoti.style", 1);
        extras.putString("android.ongoingActivityNoti.primaryInfo", "FocusGuard active");
        extras.putString("android.ongoingActivityNoti.secondaryInfo", status);

        builder.setSmallIcon(R.drawable.ic_focus_guard)
                .setContentTitle("FocusGuard active")
                .setContentText(status)
                .setStyle(new Notification.BigTextStyle().bigText(status + "\nBlocked apps will stay locked until focus mode ends."))
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_STATUS)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(true)
                .addExtras(extras);

        requestPromotedOngoing(builder);

        if (timerActive) {
            builder.setWhen(timerEnd)
                    .setUsesChronometer(true);
            if (Build.VERSION.SDK_INT >= 24) {
                builder.setChronometerCountDown(true);
            }
            requestShortCriticalText(builder, "Focus");
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop timer", stopPendingIntent);
        } else {
            builder.setSubText("Scheduled focus");
            requestShortCriticalText(builder, "Focus");
        }

        Notification notification = builder.build();
        notification.extras.putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true);
        manager.notify(NOTIFICATION_ID, notification);
    }

    private static void requestPromotedOngoing(Notification.Builder builder) {
        try {
            Notification.Builder.class
                    .getMethod("setRequestPromotedOngoing", boolean.class)
                    .invoke(builder, true);
        } catch (Exception ignored) {
            // Android versions before Android 16 do not expose this method.
        }
    }

    private static void requestShortCriticalText(Notification.Builder builder, String text) {
        try {
            Notification.Builder.class
                    .getMethod("setShortCriticalText", CharSequence.class)
                    .invoke(builder, text);
        } catch (Exception ignored) {
            // Android versions before Android 16 do not expose this method.
        }
    }
}
