package com.example.tadhkirati;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppLaunchAccessibilityService extends AccessibilityService {
    private static final String PREFS = "tadhkirati_settings";
    private static final String CHANNEL_ID = "tadhkirati_reminders";
    private long lastShownAt = 0L;
    private boolean wasInTargetApp = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString();
        if ("com.whatsapp".equals(packageName)) {
            if (!wasInTargetApp) {
                wasInTargetApp = true;
                showReminderIfAllowed();
            }
        } else {
            wasInTargetApp = false;
        }
    }

    private void showReminderIfAllowed() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String reminder = prefs.getString("reminder", "لا تنسَ تنفيذ مهمتك!");
        boolean everyTime = prefs.getBoolean("every_time", true);
        long now = System.currentTimeMillis();
        String today = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());

        // Avoid duplicate events and enforce the once-per-day option.
        if (now - lastShownAt < 2500) return;
        if (!everyTime && today.equals(prefs.getString("shown_date", ""))) return;

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "تذكيرات التطبيقات", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("تذكير يظهر عند فتح واتساب");
            manager.createNotificationChannel(channel);
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("تذكيراتي")
                .setContentText(reminder)
                .setStyle(new Notification.BigTextStyle().bigText(reminder))
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH);

        try {
            manager.notify(101, builder.build());
            lastShownAt = now;
            if (!everyTime) prefs.edit().putString("shown_date", today).apply();
        } catch (SecurityException ignored) {
            // On Android 13+, notification permission must be granted by the user.
        }
    }

    @Override
    public void onInterrupt() { }
}
