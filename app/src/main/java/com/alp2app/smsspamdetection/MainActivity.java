package com.alp2app.smsspamdetection;

import com.alp2app.smsspamdetection.R;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView statusTextView;
    private ListView spamListView;
    private Button blockButton;
    private ArrayList<String> spamMessages;
    private ArrayAdapter<String> spamAdapter;
    private BroadcastReceiver spamReceiver;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        spamListView = findViewById(R.id.spamListView);
        blockButton = findViewById(R.id.blockButton);

        spamMessages = new ArrayList<>();
        spamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spamMessages);
        spamListView.setAdapter(spamAdapter);

        blockButton.setOnClickListener(v -> toggleAutoBlock());

        // Test butonu ekleyelim
        Button testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(v -> testSpamDetection());

        checkPermissions();
        registerSpamReceiver();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    public void showSpamStatus(boolean isSpam, String message) {
        runOnUiThread(() -> {
            String status = isSpam ? "SPAM Tespit Edildi!" : "Normal Mesaj";
            String confidenceText = isSpam ? "Yüksek Güvenilirlik" : "Düşük Risk";
            
            statusTextView.setText(String.format(
                "Son Mesaj Durumu:\n%s\n\nGüvenilirlik: %s\n\nMesaj:\n%s",
                status, confidenceText, message
            ));

            if (isSpam) {
                // Spam mesajı listeye ekle
                spamMessages.add(0, message); // En başa ekle
                spamAdapter.notifyDataSetChanged();
                
                // Veritabanına kaydet
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                dbHelper.addSpamMessage("Bilinmeyen", message, true);
            }
        });
    }

    private void registerSpamReceiver() {
        spamReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isSpam = intent.getBooleanExtra("isSpam", false);
                String message = intent.getStringExtra("message");
                showSpamStatus(isSpam, message);

                if (isSpam) {
                    spamMessages.add(message);
                    spamAdapter.notifyDataSetChanged();
                }
            }
        };

        registerReceiver(spamReceiver, new IntentFilter("SPAM_DETECTION_RESULT"), 
            Context.RECEIVER_EXPORTED);
    }

    private void toggleAutoBlock() {
        SharedPreferences prefs = getSharedPreferences("SpamDetectionPrefs", MODE_PRIVATE);
        boolean isBlocking = !prefs.getBoolean("auto_block", false);
        
        prefs.edit()
            .putBoolean("auto_block", isBlocking)
            .apply();

        blockButton.setText(isBlocking ? "Otomatik Engelleme: Açık" : "Otomatik Engelleme: Kapalı");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (spamReceiver != null) {
            unregisterReceiver(spamReceiver);
        }
    }

    // Test için yeni metod
    private void testSpamDetection() {
        String[] testMessages = {
            "Tebrikler! 10000 TL kazandınız hemen tıklayın",
            "Merhaba, nasılsın? Akşam görüşelim",
            "BÜYÜK FIRSAT! Kredi kartı borcunuzu sildiriyoruz",
            "Toplantı yarın saat 14:00'te",
            "ÇEKİLİŞ KAZANDINIZ! Hediyenizi almak için tıklayın",
            "M2 OLD HARD BU AKSAM 21:00'DA ACILIYOR HERSEY DEGERLİ YUZUK PET KOSTUM YOK GERCEK METIN2 VAR www.m2old.net SMS iptal için VEGO yaz 4607 ye gönder B013",
            "METIN2 PVP SERVER ACILIYOR! OLD SCHOOL 1-99 EMEK SERVER! HEMEN KAYIT OL! www.mt2pvp.net",
            "MT2 BETA KAYITLARI BASLAMISTIR! KOSTUM PET EJDER HERSEY VAR! www.mt2game.com"
        };

        SpamModel spamModel = new SpamModel(this);
        for (String message : testMessages) {
            spamModel.predictSpam(message, (isSpam, confidence) -> {
                showSpamStatus(isSpam, message);
            });
        }
    }
}