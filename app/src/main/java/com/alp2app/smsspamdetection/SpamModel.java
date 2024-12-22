package com.alp2app.smsspamdetection;

import android.content.Context;
import androidx.annotation.NonNull;

public class SpamModel {
    private Context context;
    private DatasetLoader datasetLoader;
    private NaiveBayesClassifier naiveBayes;

    public SpamModel(Context context) {
        this.context = context;
        loadModel();
    }

    private void loadModel() {
        datasetLoader = new DatasetLoader(context);
        naiveBayes = new NaiveBayesClassifier(context);
    }

    public interface SpamPredictionCallback {
        void onPredictionComplete(boolean isSpam, float confidence);
    }

    public void predictSpam(@NonNull String message, SpamPredictionCallback callback) {
        // Naive Bayes tahmin sonucu
        double spamProbability = naiveBayes.predictSpam(message);
        
        // Kural tabanlı kontroller
        String lowerMessage = message.toLowerCase();
        
        // Spam anahtar kelimeleri genişletelim
        boolean hasSpamKeywords = 
            // Para ile ilgili
            lowerMessage.contains("kazandınız") || 
            lowerMessage.contains("tl") ||
            lowerMessage.contains("bonus") ||
            lowerMessage.contains("hediye") ||
            lowerMessage.contains("ödül") ||
            lowerMessage.contains("çekiliş") ||
            lowerMessage.contains("fırsat") ||
            lowerMessage.contains("jackpot") ||
            
            // Aciliyet/Baskı kelimeleri
            lowerMessage.contains("hemen") ||
            lowerMessage.contains("acil") ||
            lowerMessage.contains("son gün") ||
            lowerMessage.contains("kaçırma") ||
            
            // Eylem çağrıları
            lowerMessage.contains("tıkla") ||
            lowerMessage.contains("tıklayın") ||
            lowerMessage.contains("katıl") ||
            lowerMessage.contains("üye ol") ||
            
            // Bağlantılar
            lowerMessage.contains("http") ||
            lowerMessage.contains("www") ||
            lowerMessage.contains(".com") ||
            lowerMessage.contains(".net") ||  // .net uzantısı eklendi
            lowerMessage.contains("link") ||
            
            // Şüpheli ifadeler
            lowerMessage.contains("sms iptal") ||
            lowerMessage.contains("ücretsiz") ||
            lowerMessage.contains("bedava");

        // Metin2 PVP sunucu spam tespiti
        boolean isMetin2Spam = 
            // Metin2 ile ilgili terimler
            (lowerMessage.contains("metin2") || lowerMessage.contains("mt2")) &&
            (
                // Sunucu özellikleri
                lowerMessage.contains("pvp") ||
                lowerMessage.contains("server") ||
                lowerMessage.contains("aciliyor") ||
                lowerMessage.contains("açılış") ||
                lowerMessage.contains("kayıt") ||
                lowerMessage.contains("kayit") ||
                lowerMessage.contains("beta") ||
                
                // Oyun terimleri
                lowerMessage.contains("kostum") ||
                lowerMessage.contains("kostüm") ||
                lowerMessage.contains("pet") ||
                lowerMessage.contains("yuzuk") ||
                lowerMessage.contains("yüzük") ||
                lowerMessage.contains("ejder") ||
                lowerMessage.contains("lonca") ||
                
                // Sunucu tipleri
                lowerMessage.contains("old school") ||
                lowerMessage.contains("oldschool") ||
                lowerMessage.contains("hard") ||
                lowerMessage.contains("emek") ||
                
                // SMS iptal kalıpları
                lowerMessage.matches(".*sms.*iptal.*[0-9]{4}.*") ||
                lowerMessage.matches(".*(yaz|gonder).*[0-9]{4}.*")
            );

        // Spam kombinasyonları
        boolean hasSpamCombination = 
            (lowerMessage.contains("kazandınız") && lowerMessage.contains("tıkla")) ||
            (lowerMessage.contains("çekiliş") && lowerMessage.contains("kazandınız")) ||
            (lowerMessage.contains("hediye") && lowerMessage.contains("tıkla")) ||
            (lowerMessage.contains("fırsat") && lowerMessage.contains("kaçırma")) ||
            (lowerMessage.contains("bonus") && lowerMessage.contains("üye")) ||
            
            // Metin2 spam kombinasyonları
            (lowerMessage.contains("metin2") && lowerMessage.contains("aciliyor")) ||
            (lowerMessage.contains("mt2") && lowerMessage.contains("kayit")) ||
            (lowerMessage.contains("server") && lowerMessage.contains("sms iptal"));
                                
        // Nihai karar
        boolean isSpam = 
            spamProbability > 0.7 || // Yüksek Naive Bayes olasılığı
            (spamProbability > 0.5 && hasSpamKeywords) || // Orta olasılık + anahtar kelimeler
            hasSpamCombination || // Spam kelime kombinasyonları
            (hasSpamKeywords && lowerMessage.matches(".*[A-Z]{3,}.*")) || // Büyük harfli yazım + spam kelimeler
            isMetin2Spam || // Metin2 PVP sunucu spam kontrolü
            datasetLoader.isLikelySpam(message); // Veri seti kontrolü
        
        float confidence = (float) Math.max(
            spamProbability,
            isMetin2Spam ? 0.98 : // Metin2 spam için yüksek güven
            hasSpamCombination ? 0.95 : 
            hasSpamKeywords ? 0.85 : 0.0
        );
        
        callback.onPredictionComplete(isSpam, confidence);
    }
}