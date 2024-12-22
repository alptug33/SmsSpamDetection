package com.alp2app.smsspamdetection;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NaiveBayesClassifier {
    private Map<String, Double> spamProbabilities;
    private Map<String, Double> hamProbabilities;
    private double spamPrior;
    private double hamPrior;
    private Set<String> vocabulary;

    public NaiveBayesClassifier(Context context) {
        spamProbabilities = new HashMap<>();
        hamProbabilities = new HashMap<>();
        vocabulary = new HashSet<>();
        loadModel(context);
    }

    private void loadModel(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.trspam);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            int spamCount = 0;
            int totalCount = 0;
            String line;
            
            // Header'ı atla
            reader.readLine();
            
            // Verisetini oku ve kelimelerin olasılıklarını hesapla
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    boolean isSpam = parts[0].trim().equals("1");
                    String message = parts[1].trim().toLowerCase();
                    
                    // Mesajı kelimelere ayır
                    String[] words = message.replaceAll("[^a-zçğıöşü0-9 ]", " ")
                                         .split("\\s+");
                    
                    for (String word : words) {
                        if (word.length() > 3) {
                            vocabulary.add(word);
                            if (isSpam) {
                                spamProbabilities.merge(word, 1.0, Double::sum);
                            } else {
                                hamProbabilities.merge(word, 1.0, Double::sum);
                            }
                        }
                    }
                    
                    if (isSpam) spamCount++;
                    totalCount++;
                }
            }
            
            // Prior olasılıkları hesapla
            spamPrior = (double) spamCount / totalCount;
            hamPrior = 1.0 - spamPrior;
            
            // Olasılıkları normalize et (Laplace smoothing)
            double vocabSize = vocabulary.size();
            for (String word : vocabulary) {
                spamProbabilities.put(word, 
                    (spamProbabilities.getOrDefault(word, 0.0) + 1) / (spamCount + vocabSize));
                hamProbabilities.put(word, 
                    (hamProbabilities.getOrDefault(word, 0.0) + 1) / ((totalCount - spamCount) + vocabSize));
            }
            
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double predictSpam(String message) {
        String[] words = message.toLowerCase()
            .replaceAll("[^a-zçğıöşü0-9 ]", " ")
            .split("\\s+");
        
        double spamScore = Math.log(spamPrior);
        double hamScore = Math.log(hamPrior);
        
        for (String word : words) {
            if (word.length() > 3 && vocabulary.contains(word)) {
                spamScore += Math.log(spamProbabilities.getOrDefault(word, 1.0 / vocabulary.size()));
                hamScore += Math.log(hamProbabilities.getOrDefault(word, 1.0 / vocabulary.size()));
            }
        }
        
        // Olasılıkları normalize et
        double expSpam = Math.exp(spamScore);
        double expHam = Math.exp(hamScore);
        return expSpam / (expSpam + expHam);
    }
} 