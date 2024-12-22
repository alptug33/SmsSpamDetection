package com.alp2app.smsspamdetection;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class SpamDetectionService extends Service {
    private SpamModel spamModel;
    private NotificationHelper notificationHelper;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        spamModel = new SpamModel(this);
        notificationHelper = new NotificationHelper(this);
        prefs = getSharedPreferences("SpamDetectionPrefs", MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("message")) {
            String message = intent.getStringExtra("message");
            String sender = intent.getStringExtra("sender");
            processMessage(message, sender);
        }
        return START_NOT_STICKY;
    }

    private void processMessage(String message, String sender) {
        spamModel.predictSpam(message, (isSpam, confidence) -> {
            // Sonucu bildir
            Intent resultIntent = new Intent("SPAM_DETECTION_RESULT");
            resultIntent.putExtra("isSpam", isSpam);
            resultIntent.putExtra("message", message);
            resultIntent.putExtra("confidence", confidence);
            sendBroadcast(resultIntent);

            // Bildirim göster
            if (prefs.getBoolean("show_notifications", true)) {
                notificationHelper.showSpamNotification(sender, message, isSpam);
            }

            // Spam mesajı kaydet
            if (isSpam) {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                dbHelper.addSpamMessage(sender, message, true);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}