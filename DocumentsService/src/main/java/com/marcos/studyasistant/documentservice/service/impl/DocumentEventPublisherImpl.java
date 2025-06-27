package com.marcos.studyasistant.documentservice.service.impl;

import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import com.marcos.studyasistant.documentservice.event.DocumentProcessingCompletedEvent;
import com.marcos.studyasistant.documentservice.service.DocumentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentEventPublisherImpl implements DocumentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String DOCUMENT_PROCESSING_COMPLETED_TOPIC = "document.processing.completed";

    @Override
    public void publishDocumentProcessingCompleted(DocumentEntity document) {
        try {
            DocumentProcessingCompletedEvent event = DocumentProcessingCompletedEvent.builder()
                    .documentId(document.getId())
                    .userId(document.getUserId())
                    .originalFilename(document.getOriginalFilename())
                    .extractedText(document.getExtractedText())
                    .languageDetected(document.getLanguageDetected())
                    .pageCount(document.getPageCount())
                    .processedAt(document.getProcessedAt())
                    .mimeType(document.getMimeType())
                    .fileSize(document.getFileSize())
                    .build();

            kafkaTemplate.send(DOCUMENT_PROCESSING_COMPLETED_TOPIC, event);
            log.info("Published document processing completed event for document: {}", document.getId());
            
        } catch (Exception e) {
            log.error("Error publishing document processing completed event for document {}: {}", 
                document.getId(), e.getMessage(), e);
        }
    }
}