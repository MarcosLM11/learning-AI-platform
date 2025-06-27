package com.marcos.studyasistant.aiprocessingservice.service.impl;

import com.marcos.studyasistant.aiprocessingservice.entity.DocumentQA;
import com.marcos.studyasistant.aiprocessingservice.entity.DocumentSummary;
import com.marcos.studyasistant.aiprocessingservice.enums.AIProcessingStatus;
import com.marcos.studyasistant.aiprocessingservice.enums.AIProcessingType;
import com.marcos.studyasistant.aiprocessingservice.event.DocumentProcessingCompletedEvent;
import com.marcos.studyasistant.aiprocessingservice.event.QAGenerationRequestedEvent;
import com.marcos.studyasistant.aiprocessingservice.event.SummarizationRequestedEvent;
import com.marcos.studyasistant.aiprocessingservice.repository.DocumentQARepository;
import com.marcos.studyasistant.aiprocessingservice.repository.DocumentSummaryRepository;
import com.marcos.studyasistant.aiprocessingservice.service.AIProcessingCoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIProcessingCoordinatorServiceImpl implements AIProcessingCoordinatorService {

    private final DocumentSummaryRepository documentSummaryRepository;
    private final DocumentQARepository documentQARepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String SUMMARIZATION_TOPIC = "document.summarization.requested";
    private static final String QA_GENERATION_TOPIC = "document.qa.requested";

    @Override
    @Async("aiProcessingTaskExecutor")
    public CompletableFuture<Void> processDocument(DocumentProcessingCompletedEvent event) {
        log.info("Starting AI processing for document: {}", event.getDocumentId());

        try {
            // Check if text is suitable for AI processing
            if (event.getExtractedText() == null || event.getExtractedText().trim().length() < 100) {
                log.warn("Document {} has insufficient text for AI processing (length: {})", 
                    event.getDocumentId(), 
                    event.getExtractedText() != null ? event.getExtractedText().length() : 0);
                return CompletableFuture.completedFuture(null);
            }

            // Process both summarization and QA generation by default
            requestSummarization(event.getDocumentId(), AIProcessingType.BOTH).get();
            requestQAGeneration(event.getDocumentId(), AIProcessingType.BOTH).get();

            log.info("AI processing requests sent for document: {}", event.getDocumentId());

        } catch (Exception e) {
            log.error("Error processing document {} for AI: {}", event.getDocumentId(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("aiProcessingTaskExecutor")
    public CompletableFuture<Void> requestSummarization(UUID documentId, AIProcessingType processingType) {
        try {
            // Check if summary already exists
            if (documentSummaryRepository.existsByDocumentId(documentId)) {
                log.info("Summary already exists for document: {}", documentId);
                return CompletableFuture.completedFuture(null);
            }

            // Create DocumentSummary entity
            DocumentSummary summary = DocumentSummary.builder()
                    .documentId(documentId)
                    .status(AIProcessingStatus.REQUESTED)
                    .build();

            summary = documentSummaryRepository.save(summary);
            log.info("Created summary entity with ID: {} for document: {}", summary.getId(), documentId);

            // Send event to summarization service
            SummarizationRequestedEvent event = SummarizationRequestedEvent.builder()
                    .documentId(documentId)
                    .summaryId(summary.getId())
                    .build();

            kafkaTemplate.send(SUMMARIZATION_TOPIC, event);
            log.info("Sent summarization request for document: {}", documentId);

        } catch (Exception e) {
            log.error("Error requesting summarization for document {}: {}", documentId, e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("aiProcessingTaskExecutor")
    public CompletableFuture<Void> requestQAGeneration(UUID documentId, AIProcessingType processingType) {
        try {
            // Check if QA already exists
            if (documentQARepository.existsByDocumentId(documentId)) {
                log.info("QA already exists for document: {}", documentId);
                return CompletableFuture.completedFuture(null);
            }

            // Create DocumentQA entity
            DocumentQA qa = DocumentQA.builder()
                    .documentId(documentId)
                    .status(AIProcessingStatus.REQUESTED)
                    .build();

            qa = documentQARepository.save(qa);
            log.info("Created QA entity with ID: {} for document: {}", qa.getId(), documentId);

            // Send event to QA generation service
            QAGenerationRequestedEvent event = QAGenerationRequestedEvent.builder()
                    .documentId(documentId)
                    .qaId(qa.getId())
                    .desiredQuestionCount(5) // Default question count
                    .build();

            kafkaTemplate.send(QA_GENERATION_TOPIC, event);
            log.info("Sent QA generation request for document: {}", documentId);

        } catch (Exception e) {
            log.error("Error requesting QA generation for document {}: {}", documentId, e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }
}