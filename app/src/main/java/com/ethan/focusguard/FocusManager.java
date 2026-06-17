package com.ethan.focusguard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class FocusManager {
    private static final String PREFS = "focus_prefs";
    private static final String KEY_TIMER_END = "timer_end";
    private static final String KEY_SCHEDULE_ENABLED = "schedule_enabled";
    private static final String KEY_SCHEDULE_START = "schedule_start_minute";
    private static final String KEY_SCHEDULE_END = "schedule_end_minute";
    private static final String KEY_BLOCKED = "blocked_packages";
    private static final String KEY_CLOCK_STYLE = "clock_style";
    private static final String KEY_CLOCK_COLOR = "clock_color";
    private static final String KEY_CLOCK_CUSTOM_COLOR = "clock_custom_color";
    private static final String KEY_CLOCK_DIM = "clock_dim";
    private static final String KEY_CLOCK_SECONDS = "clock_seconds";
    private static final String KEY_AUTO_STANDBY_CHARGE = "auto_standby_charge";
    private static final String KEY_AUTO_STANDBY_IDLE = "auto_standby_idle";
    private static final String KEY_IDLE_MINUTES = "idle_minutes";
    private static final String KEY_LAST_USER_ACTIVITY = "last_user_activity";
    private static final String KEY_LAST_AUTO_STANDBY = "last_auto_standby";
    private static final String KEY_LAST_STANDBY_DISMISS = "last_standby_dismiss";
    private static final String KEY_WEATHER_CITY = "weather_city";
    private static final String KEY_WEATHER_SUMMARY = "weather_summary";
    private static final String KEY_WEATHER_UPDATED = "weather_updated";
    private static final String KEY_NOW_PLAYING_APP = "now_playing_app";
    private static final String KEY_NOW_PLAYING_TITLE = "now_playing_title";
    private static final String KEY_NOW_PLAYING_TEXT = "now_playing_text";
    private static final String KEY_NOW_PLAYING_UPDATED = "now_playing_updated";
    private static final int CLOCK_STYLE_COUNT = 10;
    private static final int CLOCK_COLOR_COUNT = 10;

    private FocusManager() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void startTimer(Context context, int minutes) {
        long endAt = System.currentTimeMillis() + minutes * 60_000L;
        prefs(context).edit().putLong(KEY_TIMER_END, endAt).apply();
        FocusNotificationHelper.update(context);
    }

    public static void stopTimer(Context context) {
        prefs(context).edit().putLong(KEY_TIMER_END, 0L).apply();
        FocusNotificationHelper.update(context);
    }

    public static long getTimerEnd(Context context) {
        return prefs(context).getLong(KEY_TIMER_END, 0L);
    }

    public static boolean isTimerActive(Context context) {
        return getTimerEnd(context) > System.currentTimeMillis();
    }

    public static void setDailySchedule(Context context, boolean enabled, int startMinute, int endMinute) {
        prefs(context).edit()
                .putBoolean(KEY_SCHEDULE_ENABLED, enabled)
                .putInt(KEY_SCHEDULE_START, startMinute)
                .putInt(KEY_SCHEDULE_END, endMinute)
                .apply();
        FocusNotificationHelper.update(context);
    }

    public static boolean isScheduleEnabled(Context context) {
        return prefs(context).getBoolean(KEY_SCHEDULE_ENABLED, false);
    }

    public static int getScheduleStart(Context context) {
        return prefs(context).getInt(KEY_SCHEDULE_START, 18 * 60);
    }

    public static int getScheduleEnd(Context context) {
        return prefs(context).getInt(KEY_SCHEDULE_END, 20 * 60);
    }

    public static boolean isFocusActive(Context context) {
        if (isTimerActive(context)) return true;
        return isScheduleActiveNow(context);
    }

    public static boolean isScheduleActiveNow(Context context) {
        if (!isScheduleEnabled(context)) return false;

        int start = getScheduleStart(context);
        int end = getScheduleEnd(context);
        if (start == end) return false;

        Calendar c = Calendar.getInstance();
        int current = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        if (start < end) {
            return current >= start && current < end;
        }
        return current >= start || current < end;
    }

    public static String statusText(Context context) {
        long now = System.currentTimeMillis();
        long timerEnd = getTimerEnd(context);
        if (timerEnd > now) {
            long remainingMs = timerEnd - now;
            long minutes = remainingMs / 60_000L;
            long seconds = (remainingMs / 1_000L) % 60L;
            return "Focus timer active: " + minutes + "m " + seconds + "s left";
        }
        if (isScheduleEnabled(context)) {
            boolean active = isScheduleActiveNow(context);
            return active
                    ? "Scheduled focus is active until " + formatMinute(getScheduleEnd(context))
                    : "Scheduled focus is on: " + formatMinute(getScheduleStart(context)) + " to " + formatMinute(getScheduleEnd(context));
        }
        return "Focus is off";
    }

    public static String formatMinute(int minuteOfDay) {
        int hour = minuteOfDay / 60;
        int minute = minuteOfDay % 60;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        return new SimpleDateFormat("HH:mm", Locale.UK).format(c.getTime());
    }

    public static Set<String> getBlockedPackages(Context context) {
        return new HashSet<>(prefs(context).getStringSet(KEY_BLOCKED, new HashSet<String>()));
    }

    public static boolean isBlocked(Context context, String packageName) {
        return getBlockedPackages(context).contains(packageName);
    }

    public static void setBlocked(Context context, String packageName, boolean blocked) {
        Set<String> blockedPackages = getBlockedPackages(context);
        if (blocked) {
            blockedPackages.add(packageName);
        } else {
            blockedPackages.remove(packageName);
        }
        prefs(context).edit().putStringSet(KEY_BLOCKED, blockedPackages).apply();
    }

    public static int getClockStyle(Context context) {
        return prefs(context).getInt(KEY_CLOCK_STYLE, 0);
    }

    public static void setClockStyle(Context context, int styleIndex) {
        int safe = Math.max(0, Math.min(CLOCK_STYLE_COUNT - 1, styleIndex));
        prefs(context).edit().putInt(KEY_CLOCK_STYLE, safe).apply();
    }

    public static int nextClockStyle(Context context) {
        int next = (getClockStyle(context) + 1) % CLOCK_STYLE_COUNT;
        prefs(context).edit().putInt(KEY_CLOCK_STYLE, next).apply();
        return next;
    }

    public static int getClockColor(Context context) {
        return prefs(context).getInt(KEY_CLOCK_COLOR, 0);
    }

    public static void setClockColor(Context context, int colourIndex) {
        int safe = Math.max(0, Math.min(CLOCK_COLOR_COUNT - 1, colourIndex));
        prefs(context).edit().putInt(KEY_CLOCK_COLOR, safe).apply();
    }

    public static int nextClockColor(Context context) {
        int next = (getClockColor(context) + 1) % CLOCK_COLOR_COUNT;
        prefs(context).edit().putInt(KEY_CLOCK_COLOR, next).apply();
        return next;
    }

    public static int getClockCustomColor(Context context) {
        return prefs(context).getInt(KEY_CLOCK_CUSTOM_COLOR, Color.parseColor("#67E8F9"));
    }

    public static void setClockCustomColor(Context context, int color) {
        prefs(context).edit().putInt(KEY_CLOCK_CUSTOM_COLOR, color).apply();
    }

    public static boolean isClockDim(Context context) {
        return prefs(context).getBoolean(KEY_CLOCK_DIM, false);
    }

    public static boolean toggleClockDim(Context context) {
        boolean next = !isClockDim(context);
        prefs(context).edit().putBoolean(KEY_CLOCK_DIM, next).apply();
        return next;
    }

    public static boolean isClockSecondsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_CLOCK_SECONDS, false);
    }

    public static boolean toggleClockSeconds(Context context) {
        boolean next = !isClockSecondsEnabled(context);
        prefs(context).edit().putBoolean(KEY_CLOCK_SECONDS, next).apply();
        return next;
    }


    public static boolean isAutoStandbyOnCharge(Context context) {
        return prefs(context).getBoolean(KEY_AUTO_STANDBY_CHARGE, true);
    }

    public static void setAutoStandbyOnCharge(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_AUTO_STANDBY_CHARGE, enabled).apply();
    }

    public static boolean isAutoStandbyOnIdle(Context context) {
        return prefs(context).getBoolean(KEY_AUTO_STANDBY_IDLE, false);
    }

    public static void setAutoStandbyOnIdle(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_AUTO_STANDBY_IDLE, enabled).apply();
        recordUserActivity(context);
    }

    public static int getIdleMinutes(Context context) {
        return prefs(context).getInt(KEY_IDLE_MINUTES, 3);
    }

    public static void setIdleMinutes(Context context, int minutes) {
        int safe = Math.max(1, Math.min(60, minutes));
        prefs(context).edit().putInt(KEY_IDLE_MINUTES, safe).apply();
    }

    public static void recordUserActivity(Context context) {
        prefs(context).edit().putLong(KEY_LAST_USER_ACTIVITY, System.currentTimeMillis()).apply();
    }

    public static boolean shouldAutoStartFromIdle(Context context) {
        if (!isAutoStandbyOnIdle(context)) return false;
        long now = System.currentTimeMillis();
        long lastActivity = prefs(context).getLong(KEY_LAST_USER_ACTIVITY, now);
        long lastAuto = prefs(context).getLong(KEY_LAST_AUTO_STANDBY, 0L);
        long lastDismiss = prefs(context).getLong(KEY_LAST_STANDBY_DISMISS, 0L);
        long requiredIdle = getIdleMinutes(context) * 60_000L;
        if (now - lastActivity < requiredIdle) return false;
        if (now - lastAuto < 60_000L) return false;
        return now - lastDismiss >= 60_000L;
    }

    public static void openStandbyClock(Context context, boolean automatic, String reason) {
        prefs(context).edit().putLong(KEY_LAST_AUTO_STANDBY, System.currentTimeMillis()).apply();
        Intent intent = new Intent(context, StandbyClockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("autoStandby", automatic);
        intent.putExtra("reason", reason == null ? "standby" : reason);
        context.startActivity(intent);
    }


    public static String getWeatherCity(Context context) {
        return prefs(context).getString(KEY_WEATHER_CITY, "London");
    }

    public static void setWeatherCity(Context context, String city) {
        String safe = city == null ? "" : city.trim();
        if (safe.isEmpty()) safe = "London";
        prefs(context).edit().putString(KEY_WEATHER_CITY, safe).apply();
    }

    public static void setWeatherSummary(Context context, String summary) {
        prefs(context).edit()
                .putString(KEY_WEATHER_SUMMARY, summary == null ? "Weather unavailable" : summary)
                .putLong(KEY_WEATHER_UPDATED, System.currentTimeMillis())
                .apply();
    }

    public static long getWeatherUpdated(Context context) {
        return prefs(context).getLong(KEY_WEATHER_UPDATED, 0L);
    }

    public static String getWeatherSummary(Context context) {
        String city = getWeatherCity(context);
        String summary = prefs(context).getString(KEY_WEATHER_SUMMARY, "");
        long updated = getWeatherUpdated(context);
        if (summary == null || summary.trim().isEmpty()) {
            return "Weather  •  set city: " + city;
        }
        long age = System.currentTimeMillis() - updated;
        if (updated > 0 && age > 2L * 60L * 60L * 1000L) {
            return summary + "  •  old";
        }
        return summary;
    }

    public static void setNowPlaying(Context context, String app, String title, String text) {
        prefs(context).edit()
                .putString(KEY_NOW_PLAYING_APP, app == null ? "Music" : app)
                .putString(KEY_NOW_PLAYING_TITLE, title == null ? "" : title)
                .putString(KEY_NOW_PLAYING_TEXT, text == null ? "" : text)
                .putLong(KEY_NOW_PLAYING_UPDATED, System.currentTimeMillis())
                .apply();
    }

    public static void clearNowPlaying(Context context) {
        prefs(context).edit()
                .remove(KEY_NOW_PLAYING_APP)
                .remove(KEY_NOW_PLAYING_TITLE)
                .remove(KEY_NOW_PLAYING_TEXT)
                .remove(KEY_NOW_PLAYING_UPDATED)
                .apply();
    }

    public static String getNowPlayingSummary(Context context) {
        String title = prefs(context).getString(KEY_NOW_PLAYING_TITLE, "");
        String text = prefs(context).getString(KEY_NOW_PLAYING_TEXT, "");
        String app = prefs(context).getString(KEY_NOW_PLAYING_APP, "Music");
        long updated = prefs(context).getLong(KEY_NOW_PLAYING_UPDATED, 0L);
        if (title == null || title.trim().isEmpty()) {
            return "Music  •  enable notification access";
        }
        if (System.currentTimeMillis() - updated > 30L * 60L * 1000L) {
            return "Music  •  no recent playback";
        }
        String detail = text == null || text.trim().isEmpty() ? app : text;
        return app + "  •  " + title + "\n" + detail;
    }

    public static void noteStandbyDismissed(Context context) {
        long now = System.currentTimeMillis();
        prefs(context).edit()
                .putLong(KEY_LAST_STANDBY_DISMISS, now)
                .putLong(KEY_LAST_USER_ACTIVITY, now)
                .apply();
    }
}
