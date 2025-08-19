package com.marcos.studyasistant.summarizationservice.service.impl;


import com.marcos.studyasistant.summarizationservice.dto.SummarizationRequest;
import com.marcos.studyasistant.summarizationservice.dto.SummarizationResponse;
import com.marcos.studyasistant.summarizationservice.service.SummarizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SummarizationServiceImpl implements SummarizationService {

    private final ChatClient chatClient;

    private static final String PROMPT_TEMPLATE = "Eres un experto en resúmenes de documentos." +
            " Tu tarea es resumir el siguiente texto de manera concisa y clara." +
            "Por favor, proporciona un resumen que contenga toda la información importante" +
            " para que una persona pueda usarlo para estudiar sin necesidad de tener que leer el documento entero." +
            "El resumen debe de ser completo y la salida tiene que ser directamente el resumen del texto.";

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es", "fr");

    public SummarizationServiceImpl(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

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

            String summaryText = chatClient.prompt(PROMPT_TEMPLATE).user(request.getText()).call().content();

            long processingTime = System.currentTimeMillis() - startTime;

            SummarizationResponse response = SummarizationResponse.builder()
                    .summaryId(UUID.randomUUID())
                    .documentId(request.getDocumentId())
                    .summaryText(summaryText)
                    .modelUsed("qwen3")
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

            String summaryText = chatClient.prompt(PROMPT_TEMPLATE).user(text).call().content();

            log.info("Summarization completed. Original: {} chars, Summary: {} chars",
                    text.length(), summaryText.length());

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

    private String extractLanguageCode(String language) {
        if (language == null) return "en";
        return language.toLowerCase().substring(0, Math.min(2, language.length()));
    }
}