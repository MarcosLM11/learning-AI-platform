package com.marcos.studyasistant.summarizationservice.service;

import com.marcos.studyasistant.summarizationservice.dto.SummarizationRequest;
import com.marcos.studyasistant.summarizationservice.dto.SummarizationResponse;
import com.marcos.studyasistant.summarizationservice.event.SummarizationCompletedEvent;
import com.marcos.studyasistant.summarizationservice.event.SummarizationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummarizationEventListener {

    private final SummarizationService summarizationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String SUMMARIZATION_COMPLETED_TOPIC = "document.summarization.completed";

    @KafkaListener(topics = "document.summarization.requested", groupId = "summarization-service")
    public void handleSummarizationRequested(SummarizationRequestedEvent event) {
        log.info("Received summarization request for document: {}", event.getDocumentId());
        
        try {
            SummarizationRequest request = SummarizationRequest.builder()
                    .documentId(event.getDocumentId())
                    .userId(event.getUserId())
                    .text(event.getExtractedText())
                    .language(event.getLanguageDetected())
                    .originalFilename(event.getOriginalFilename())
                    .build();
            
            summarizationService.summarizeText(request)
                    .thenAccept(response -> publishSummarizationCompleted(event, response))
                    .exceptionally(throwable -> {
                        log.error("Error processing summarization for document {}: {}", 
                            event.getDocumentId(), throwable.getMessage());
                        publishSummarizationFailed(event, throwable.getMessage());
                        return null;
                    });
                    
        } catch (Exception e) {
            log.error("Error handling summarization request for document {}: {}", 
                event.getDocumentId(), e.getMessage(), e);
            publishSummarizationFailed(event, e.getMessage());
        }
    }
    
    private void publishSummarizationCompleted(SummarizationRequestedEvent originalEvent, SummarizationResponse response) {
        SummarizationCompletedEvent completedEvent = SummarizationCompletedEvent.builder()
                .summaryId(originalEvent.getSummaryId())
                .documentId(originalEvent.getDocumentId())
                .userId(originalEvent.getUserId())
                .summaryText(response.getSummaryText())
                .modelUsed(response.getModelUsed())
                .summaryLength(response.getSummaryLength())
                .originalTextLength(response.getOriginalTextLength())
                .compressionRatio(response.getCompressionRatio())
                .processingTimeMs(response.getProcessingTimeMs())
                .completedAt(response.getCreatedAt())
                .success(true)
                .build();
        
        kafkaTemplate.send(SUMMARIZATION_COMPLETED_TOPIC, completedEvent);
        log.info("Published summarization completed event for document: {}", originalEvent.getDocumentId());
    }
    
    private void publishSummarizationFailed(SummarizationRequestedEvent originalEvent, String errorMessage) {
        SummarizationCompletedEvent failedEvent = SummarizationCompletedEvent.builder()
                .summaryId(originalEvent.getSummaryId())
                .documentId(originalEvent.getDocumentId())
                .userId(originalEvent.getUserId())
                .success(false)
                .errorMessage(errorMessage)
                .build();
        
        kafkaTemplate.send(SUMMARIZATION_COMPLETED_TOPIC, failedEvent);
        log.info("Published summarization failed event for document: {}", originalEvent.getDocumentId());
    }
}