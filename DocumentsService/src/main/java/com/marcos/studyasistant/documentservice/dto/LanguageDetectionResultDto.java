package com.marcos.studyasistant.documentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDetectionResultDto {
    private String language;
    private double confidence;
    private String reason;

    public static LanguageDetectionResultDto unknown(String reason) {
        return new LanguageDetectionResultDto("unknown", 0.0, reason);
    }

    public boolean isReliable() {
        return confidence > 0.7 && !"unknown".equals(language);
    }
}
