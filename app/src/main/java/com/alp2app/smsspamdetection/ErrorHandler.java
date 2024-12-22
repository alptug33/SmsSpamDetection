package com.alp2app.smsspamdetection;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ErrorHandler {
    private static final String TAG = "SpamDetection";

    public static void handleException(Context context, Exception e, String message) {
        Log.e(TAG, message, e);
        Toast.makeText(context, "Hata: " + message, Toast.LENGTH_SHORT).show();
    }

    public static void logError(String message) {
        Log.e(TAG, message);
    }

    public static void showError(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
} 