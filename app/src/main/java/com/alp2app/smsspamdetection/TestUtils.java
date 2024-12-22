package com.alp2app.smsspamdetection;

public class TestUtils {
    public static String generateTestMessage(boolean isSpam) {
        if (isSpam) {
            String[] spamTemplates = {
                "Tebrikler! 10000 TL kazandınız!",
                "Özel fırsat! Hemen tıklayın!",
                "Kredi kartı borcunuz ödenmedi!",
                "Hesabınız bloke edildi!"
            };
            return spamTemplates[(int) (Math.random() * spamTemplates.length)];
        } else {
            String[] normalTemplates = {
                "Merhaba, nasılsın?",
                "Toplantı saat 15:00'te",
                "Akşam görüşelim mi?",
                "Dökümanları aldım, teşekkürler"
            };
            return normalTemplates[(int) (Math.random() * normalTemplates.length)];
        }
    }
} 