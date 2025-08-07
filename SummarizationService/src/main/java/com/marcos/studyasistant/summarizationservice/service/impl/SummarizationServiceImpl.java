package com.marcos.studyasistant.summarizationservice.service.impl;

import com.marcos.studyasistant.summarizationservice.client.HuggingFaceClient;
import com.marcos.studyasistant.summarizationservice.dto.SummarizationRequest;
import com.marcos.studyasistant.summarizationservice.dto.SummarizationResponse;
import com.marcos.studyasistant.summarizationservice.service.SummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummarizationServiceImpl implements SummarizationService {

    private final HuggingFaceClient huggingFaceClient;

    private static final Map<String, String> LANGUAGE_MODELS = Map.of(
            "en", "facebook/bart-large-cnn",
            "es", "facebook/bart-large-cnn", // También funciona para español
            "fr", "facebook/bart-large-cnn"
    );

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es", "fr");

    @Override
    @Async("summarizationTaskExecutor")
    public CompletableFuture<SummarizationResponse> summarizeText(SummarizationRequest request) {
        log.info("Starting summarization for document: {}", request.getDocumentId());

        long startTime = System.currentTimeMillis();

        try {
            String language = extractLanguageCode(request.getLanguage());

            if (!isLanguageSupported(language)) {
                throw new IllegalArgumentException("Language not supported: " + language);
            }

            String model = getModelForLanguage(language);
            String summaryText = huggingFaceClient.summarizeText(request.getText(), model);

            long processingTime = System.currentTimeMillis() - startTime;

            SummarizationResponse response = SummarizationResponse.builder()
                    .summaryId(UUID.randomUUID())
                    .documentId(request.getDocumentId())
                    .summaryText(summaryText)
                    .modelUsed(model)
                    .summaryLength(summaryText.length())
                    .originalTextLength(request.getText().length())
                    .compressionRatio((double) summaryText.length() / request.getText().length())
                    .processingTimeMs(processingTime)
                    .createdAt(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();

            log.info("Summarization completed for document: {} in {}ms",
                    request.getDocumentId(), processingTime);

            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("Error summarizing document {}: {}", request.getDocumentId(), e.getMessage(), e);

            SummarizationResponse errorResponse = SummarizationResponse.builder()
                    .summaryId(UUID.randomUUID())
                    .documentId(request.getDocumentId())
                    .status("FAILED")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .createdAt(LocalDateTime.now())
                    .build();

            return CompletableFuture.completedFuture(errorResponse);
        }
    }

    @Override
    @Async("summarizationTaskExecutor")
    public CompletableFuture<Void> processSummarizationRequest(String text, String language, int maxLength) {
        log.info("Processing summarization request for text length: {}, language: {}", text.length(), language);

        try {
            String languageCode = extractLanguageCode(language);

            if (!isLanguageSupported(languageCode)) {
                log.warn("Unsupported language: {}", languageCode);
                return CompletableFuture.completedFuture(null);
            }

            String model = getModelForLanguage(languageCode);
            String summary = huggingFaceClient.summarizeText(text, model);

            log.info("Summarization completed. Original: {} chars, Summary: {} chars",
                    text.length(), summary.length());

        } catch (Exception e) {
            log.error("Error processing summarization request: {}", e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isLanguageSupported(String language) {
        if (language == null) return false;
        String languageCode = extractLanguageCode(language);
        return SUPPORTED_LANGUAGES.contains(languageCode);
    }

    @Override
    public String getDefaultModel() {
        return "facebook/bart-large-cnn";
    }

    private String getModelForLanguage(String language) {
        return LANGUAGE_MODELS.getOrDefault(language, getDefaultModel());
    }

    private String extractLanguageCode(String language) {
        if (language == null) return "en";
        return language.toLowerCase().substring(0, Math.min(2, language.length()));
    }
}