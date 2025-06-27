package com.marcos.studyasistant.aiprocessingservice.service;

import com.marcos.studyasistant.aiprocessingservice.event.DocumentProcessingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingEventListener {

    private final AIProcessingCoordinatorService aiProcessingCoordinatorService;

    @KafkaListener(topics = "document.processing.completed", groupId = "ai-processing-service")
    public void handleDocumentProcessingCompleted(DocumentProcessingCompletedEvent event) {
        log.info("Received document processing completed event for document: {}", event.getDocumentId());
        
        try {
            aiProcessingCoordinatorService.processDocument(event);
            log.info("Successfully initiated AI processing for document: {}", event.getDocumentId());
        } catch (Exception e) {
            log.error("Error handling document processing completed event for document {}: {}", 
                event.getDocumentId(), e.getMessage(), e);
        }
    }
}