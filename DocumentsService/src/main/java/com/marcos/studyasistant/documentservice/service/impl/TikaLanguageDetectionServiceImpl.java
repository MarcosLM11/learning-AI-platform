package com.marcos.studyasistant.documentservice.service.impl;

import com.marcos.studyasistant.documentservice.dto.LanguageDetectionResultDto;
import com.marcos.studyasistant.documentservice.service.LanguageDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@Slf4j
public class TikaLanguageDetectionServiceImpl implements LanguageDetectionService {

    private final LanguageDetector languageDetector;

    @Value("${language.detection.min-text-length:50}")
    private int minTextLength;

    @Value("${language.detection.confidence-threshold:0.5}")
    private double confidenceThreshold;

    public TikaLanguageDetectionServiceImpl() {
        LanguageDetector tempDetector = null;
        try {
            tempDetector = LanguageDetector.getDefaultLanguageDetector();
            tempDetector.loadModels();
            log.info("LanguageDetectionService initialized with Apache Tika");
        } catch (Exception e) {
            log.warn("Language detector not available, falling back to default behavior: {}", e.getMessage());
            tempDetector = null;
        }
        this.languageDetector = tempDetector;
    }

    @Override
    public LanguageDetectionResultDto detectLanguage(String text) {

        if (isTextEmpty(text)) {
            return LanguageDetectionResultDto.unknown("Text is empty or null");
        }

        // Si no hay detector disponible, retornamos unknown
        if (languageDetector == null) {
            return LanguageDetectionResultDto.unknown("Language detector not available");
        }

        String cleanText = cleanText(text);

        if (isTextTooShort(cleanText)) {
            return LanguageDetectionResultDto.unknown(
                    String.format("Text too short (%d chars), minimum required: %d",
                            cleanText.length(), minTextLength));
        }

        try {
            LanguageResult result = languageDetector.detect(cleanText);
            String detectedLanguage = result.getLanguage();
            double confidence = result.getRawScore();

            log.debug("Language detected: {} with confidence: {}", detectedLanguage, confidence);

            if (confidence < confidenceThreshold) {
                return LanguageDetectionResultDto.unknown(
                        String.format("Low confidence: %.2f (threshold: %.2f)",
                                confidence, confidenceThreshold));
            }

            return new LanguageDetectionResultDto(detectedLanguage, confidence, "apache-tika");

        } catch (Exception e) {
            log.error("Error detecting language with Apache Tika: {}", e.getMessage(), e);
            return LanguageDetectionResultDto.unknown("Detection error: " + e.getMessage());
        }
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replaceAll("https?://[\\w\\.-]+(?:/[\\w\\.-]*)*(?:\\?[\\w&=%\\.-]*)?", " ")
                .replaceAll("[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}", " ")
                .replaceAll("\\b\\d{4,}\\b", " ")
                .replaceAll("[^\\p{L}\\p{N}\\s\\p{Punct}]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isTextEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isTextTooShort(String text) {
        return text.length() < minTextLength;
    }

    public String getLanguageName(String languageCode) {
        if (languageCode == null || languageCode.equals("unknown")) {
            return "Unknown";
        }

        Locale locale = new Locale(languageCode);
        return locale.getDisplayLanguage(Locale.ENGLISH);
    }
}