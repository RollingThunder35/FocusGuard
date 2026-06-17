package com.ethan.focusguard;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class UiKit {
    public static final int BG = Color.parseColor("#F5F7FB");
    public static final int SURFACE = Color.WHITE;
    public static final int SURFACE_SOFT = Color.parseColor("#EEF3FF");
    public static final int TEXT = Color.parseColor("#101828");
    public static final int MUTED = Color.parseColor("#667085");
    public static final int PRIMARY = Color.parseColor("#2563EB");
    public static final int PRIMARY_DARK = Color.parseColor("#174EA6");
    public static final int GREEN = Color.parseColor("#16A34A");
    public static final int RED = Color.parseColor("#DC2626");
    public static final int BORDER = Color.parseColor("#D0D5DD");

    private UiKit() {}

    public static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void page(Activity activity, View root) {
        root.setBackgroundColor(BG);
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(BG);
            window.setNavigationBarColor(BG);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static GradientDrawable rounded(int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radiusDp);
        return drawable;
    }

    public static GradientDrawable roundedStroke(Activity activity, int color, int strokeColor, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(activity, radiusDp));
        drawable.setStroke(dp(activity, strokeDp), strokeColor);
        return drawable;
    }

    public static TextView text(Activity activity, String value, int sp, int color, boolean bold) {
        TextView tv = new TextView(activity);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setLineSpacing(dp(activity, 2), 1.0f);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    public static LinearLayout card(Activity activity) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 18), dp(activity, 18), dp(activity, 18), dp(activity, 18));
        card.setBackground(roundedStroke(activity, SURFACE, Color.parseColor("#E4E7EC"), 1, 24));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, dp(activity, 12), 0, dp(activity, 12));
        card.setLayoutParams(lp);
        return card;
    }

    public static LinearLayout pill(Activity activity, int color) {
        LinearLayout pill = new LinearLayout(activity);
        pill.setOrientation(LinearLayout.VERTICAL);
        pill.setPadding(dp(activity, 14), dp(activity, 10), dp(activity, 14), dp(activity, 10));
        pill.setBackground(rounded(color, dp(activity, 18)));
        return pill;
    }

    public static Button button(Activity activity, String label) {
        Button button = new Button(activity);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setAllCaps(false);
        button.setBackground(rounded(PRIMARY, dp(activity, 18)));
        button.setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, dp(activity, 8), 0, dp(activity, 8));
        button.setLayoutParams(lp);
        return button;
    }

    public static Button secondaryButton(Activity activity, String label) {
        Button button = button(activity, label);
        button.setTextColor(TEXT);
        button.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        button.setBackground(roundedStroke(activity, SURFACE, BORDER, 1, 18));
        return button;
    }

    public static Button dangerButton(Activity activity, String label) {
        Button button = button(activity, label);
        button.setBackground(rounded(RED, dp(activity, 18)));
        return button;
    }

    public static TextView sectionHeader(Activity activity, String title, String subtitle) {
        TextView tv = text(activity, title + (subtitle == null || subtitle.isEmpty() ? "" : "\n" + subtitle), 18, TEXT, true);
        tv.setPadding(0, 0, 0, dp(activity, 12));
        return tv;
    }

    public static LinearLayout row(Activity activity) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(activity, 4), 0, dp(activity, 4));
        return row;
    }

    public static void topMargin(Activity activity, View view, int dp) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (lp == null) {
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        lp.setMargins(lp.leftMargin, dp(activity, dp), lp.rightMargin, lp.bottomMargin);
        view.setLayoutParams(lp);
    }
}
