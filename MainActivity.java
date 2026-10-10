package com.example.tadhkirati;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "tadhkirati_settings";
    private static final String CHANNEL_ID = "tadhkirati_reminders";
    private EditText reminderInput;
    private CheckBox everyTimeCheck;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 36, 32, 24);
        root.setLayoutDirection(ViewGroup.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("تذكيراتي");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView intro = new TextView(this);
        intro.setText("اكتب التذكير الذي تريد رؤيته عند فتح واتساب. يعمل الإصدار الأول مع واتساب فقط، ويعرض إشعارًا دون قراءة رسائلك.");
        intro.setTextSize(16);
        LinearLayout.LayoutParams introParams = new LinearLayout.LayoutParams(-1, -2);
        introParams.setMargins(0, 24, 0, 12);
        root.addView(intro, introParams);

        reminderInput = new EditText(this);
        reminderInput.setHint("مثال: لا تنسَ إرسال الملف");
        reminderInput.setTextSize(16);
        root.addView(reminderInput, new LinearLayout.LayoutParams(-1, -2));

        everyTimeCheck = new CheckBox(this);
        everyTimeCheck.setText("إظهار التذكير في كل مرة أفتح فيها واتساب");
        everyTimeCheck.setTextSize(15);
        root.addView(everyTimeCheck, new LinearLayout.LayoutParams(-1, -2));

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        reminderInput.setText(prefs.getString("reminder", "لا تنسَ تنفيذ مهمتك!"));
        everyTimeCheck.setChecked(prefs.getBoolean("every_time", true));

        Button save = new Button(this);
        save.setText("حفظ التذكير");
        root.addView(save, buttonParams());
        save.setOnClickListener(v -> {
            String message = reminderInput.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "اكتب نص التذكير أولًا", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString("reminder", message)
                    .putBoolean("every_time", everyTimeCheck.isChecked()).apply();
            Toast.makeText(this, "تم حفظ التذكير", Toast.LENGTH_SHORT).show();
        });

        Button enable = new Button(this);
        enable.setText("تفعيل اكتشاف فتح واتساب");
        root.addView(enable, buttonParams());
        enable.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "ابحث عن «تذكيراتي» داخل خدمات إمكانية الوصول وفعّلها بنفسك.", Toast.LENGTH_LONG).show();
        });

        Button notificationPermission = new Button(this);
        notificationPermission.setText("السماح بالإشعارات");
        root.addView(notificationPermission, buttonParams());
        notificationPermission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                } else {
                    Toast.makeText(this, "الإشعارات مسموح بها بالفعل", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "لا يحتاج هذا الإصدار إلى إذن إشعارات منفصل", Toast.LENGTH_SHORT).show();
            }
        });

        TextView note = new TextView(this);
        note.setText("ملاحظة: قد تختلف أسماء القوائم حسب نوع الهاتف. إذا أوقف النظام التطبيق في الخلفية، اسمح له بالعمل دون قيود من إعدادات البطارية.");
        note.setTextSize(13);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
        noteParams.setMargins(0, 18, 0, 0);
        root.addView(note, noteParams);

        setContentView(root);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 10, 0, 0);
        return p;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "تذكيرات التطبيقات", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("تذكير يظهر عند فتح واتساب");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
