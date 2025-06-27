package com.marcos.studyasistant.summarizationservice.service.impl;

import com.marcos.studyasistant.summarizationservice.dto.SummarizationRequest;
import com.marcos.studyasistant.summarizationservice.dto.SummarizationResponse;
import com.marcos.studyasistant.summarizationservice.service.SummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummarizationServiceImpl implements SummarizationService {

    @Override
    @Async("summarizationTaskExecutor")
    public CompletableFuture<SummarizationResponse> summarizeText(SummarizationRequest request) {
        log.info("Starting summarization for document: {}", request.getDocumentId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // TODO: Implement Hugging Face integration here
            // This is where you'll add the actual AI model call
            
            // Placeholder implementation
            String summaryText = generatePlaceholderSummary(request.getText());
            long processingTime = System.currentTimeMillis() - startTime;
            
            SummarizationResponse response = SummarizationResponse.builder()
                    .summaryId(UUID.randomUUID())
                    .documentId(request.getDocumentId())
                    .summaryText(summaryText)
                    .modelUsed(getDefaultModel())
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
        
        // TODO: Add your Hugging Face processing logic here
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isLanguageSupported(String language) {
        // TODO: Implement language support check based on your models
        return language != null && (language.startsWith("en") || language.startsWith("es") || language.startsWith("fr"));
    }

    @Override
    public String getDefaultModel() {
        // TODO: Return your preferred Hugging Face model name
        return "facebook/bart-large-cnn";
    }
    
    private String generatePlaceholderSummary(String text) {
        // Placeholder implementation - replace with actual Hugging Face call
        if (text.length() <= 200) {
            return text;
        }
        
        String[] sentences = text.split("\\.");
        if (sentences.length <= 2) {
            return text;
        }
        
        return sentences[0] + "." + (sentences.length > 1 ? sentences[1] + "." : "");
    }
}