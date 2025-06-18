package com.marcos.studyasistant.documentservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDetectionResult {
    private String language;
    private double confidence;
    private String reason;

    public static LanguageDetectionResult unknown(String reason) {
        return new LanguageDetectionResult("unknown", 0.0, reason);
    }

    public boolean isReliable() {
        return confidence > 0.7 && !"unknown".equals(language);
    }
}
