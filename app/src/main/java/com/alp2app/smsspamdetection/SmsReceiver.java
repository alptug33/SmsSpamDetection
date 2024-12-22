package com.alp2app.smsspamdetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            String message = extractMessage(intent);
            if (message != null) {
                forwardToDetection(context, message);
            }
        }
    }

    private String extractMessage(Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages != null && messages.length > 0) {
            StringBuilder fullMessage = new StringBuilder();
            for (SmsMessage sms : messages) {
                fullMessage.append(sms.getMessageBody());
            }
            return fullMessage.toString();
        }
        return null;
    }

    private void forwardToDetection(Context context, String message) {
        Intent serviceIntent = new Intent(context, SpamDetectionService.class);
        serviceIntent.putExtra("message", message);
        context.startService(serviceIntent);
    }
}