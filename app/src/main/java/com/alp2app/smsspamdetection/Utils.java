package com.alp2app.smsspamdetection;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class Utils {
    public static String cleanText(String text) {
        return text.trim().toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", " ");
    }

    public static String formatForModel(String text) {
        // Model için metin formatlaması
        return cleanText(text);
    }

    public static void showNotification(Context context, boolean isSpam, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "spam_detection",
                    "Spam Tespiti",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "spam_detection")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(isSpam ? "Spam Mesaj Tespit Edildi!" : "Yeni Mesaj Alındı")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}