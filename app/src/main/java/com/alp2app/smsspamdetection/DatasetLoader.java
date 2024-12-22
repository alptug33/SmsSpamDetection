package com.alp2app.smsspamdetection;

import android.content.Context;
import android.content.res.Resources;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatasetLoader {
    private Context context;
    private Set<String> spamKeywords;
    private List<MessageData> trainingData;

    public static class MessageData {
        public final String message;
        public final boolean isSpam;

        public MessageData(String message, boolean isSpam) {
            this.message = message;
            this.isSpam = isSpam;
        }
    }

    public DatasetLoader(Context context) {
        this.context = context;
        this.spamKeywords = new HashSet<>();
        this.trainingData = new ArrayList<>();
        loadDataset();
        extractKeywords();
    }

    private void loadDataset() {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.trspam);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            
            // Header'ı atla
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    boolean isSpam = parts[0].trim().equals("1");
                    String message = parts[1].trim();
                    trainingData.add(new MessageData(message, isSpam));
                }
            }
            reader.close();
        } catch (IOException | Resources.NotFoundException e) {
            ErrorHandler.handleException(context, e, "Veriseti yüklenirken hata oluştu");
        }
    }

    private void extractKeywords() {
        for (MessageData data : trainingData) {
            if (data.isSpam) {
                String[] words = data.message.toLowerCase()
                    .replaceAll("[^a-zçğıöşü0-9 ]", " ")
                    .split("\\s+");
                
                for (String word : words) {
                    if (word.length() > 3) { // Kısa kelimeleri atla
                        spamKeywords.add(word);
                    }
                }
            }
        }
    }

    public boolean isLikelySpam(String message) {
        String[] words = message.toLowerCase()
            .replaceAll("[^a-zçğıöşü0-9 ]", " ")
            .split("\\s+");
        
        int spamWordCount = 0;
        for (String word : words) {
            if (spamKeywords.contains(word)) {
                spamWordCount++;
            }
        }
        
        // Mesajdaki spam kelime oranı
        float spamRatio = (float) spamWordCount / words.length;
        return spamRatio > 0.15; // %15'den fazla spam kelime içeriyorsa
    }

    public List<MessageData> getTrainingData() {
        return trainingData;
    }

    public Set<String> getSpamKeywords() {
        return spamKeywords;
    }
} 