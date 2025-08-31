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

    private static final String SUMMARIZATION_TOPIC = "sum-requests";
    private static final String QA_GENERATION_TOPIC = "qa-requests";

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
            requestSummarization(event, AIProcessingType.BOTH).get();
            requestQAGeneration(event, AIProcessingType.BOTH).get();

            log.info("AI processing requests sent for document: {}", event.getDocumentId());

        } catch (Exception e) {
            log.error("Error processing document {} for AI: {}", event.getDocumentId(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("aiProcessingTaskExecutor")
    public CompletableFuture<Void> requestSummarization(DocumentProcessingCompletedEvent event, AIProcessingType processingType) {
        try {
            // Check if summary already exists
            if (documentSummaryRepository.existsByDocumentId(event.getDocumentId())) {
                log.info("Summary already exists for document: {}", event.getDocumentId());
                return CompletableFuture.completedFuture(null);
            }

            // Create DocumentSummary entity
            DocumentSummary summary = DocumentSummary.builder()
                    .userId(event.getUserId())
                    .documentId(event.getDocumentId())
                    .summaryText("")
                    .status(AIProcessingStatus.REQUESTED)
                    .build();

            summary = documentSummaryRepository.save(summary);
            log.info("Created summary entity with ID: {} for document: {}", summary.getId(), event.getDocumentId());

            // Send event to summarization service
            SummarizationRequestedEvent eventRequest = SummarizationRequestedEvent.builder()
                    .userId(event.getUserId())
                    .extractedText(event.getExtractedText())
                    .languageDetected(event.getLanguageDetected())
                    .originalFilename(event.getOriginalFilename())
                    .extractedText(event.getExtractedText())
                    .documentId(event.getDocumentId())
                    .summaryId(summary.getId())
                    .build();

            kafkaTemplate.send(SUMMARIZATION_TOPIC, eventRequest);
            log.info("Sent summarization request for document: {}", event.getDocumentId());

        } catch (Exception e) {
            log.error("Error requesting summarization for document {}: {}", event.getDocumentId(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("aiProcessingTaskExecutor")
    public CompletableFuture<Void> requestQAGeneration(DocumentProcessingCompletedEvent event, AIProcessingType processingType) {
        try {
            // Check if QA already exists
            if (documentQARepository.existsByDocumentId(event.getDocumentId())) {
                log.info("QA already exists for document: {}", event.getDocumentId());
                return CompletableFuture.completedFuture(null);
            }

            // Create DocumentQA entity
            DocumentQA qa = DocumentQA.builder()
                    .userId(event.getUserId())
                    .documentId(event.getDocumentId())
                    .status(AIProcessingStatus.REQUESTED)
                    .build();

            qa = documentQARepository.save(qa);
            log.info("Created QA entity with ID: {} for document: {}", qa.getId(), event.getDocumentId());

            // Send event to QA generation service
            QAGenerationRequestedEvent eventRequest = QAGenerationRequestedEvent.builder()
                    .userId(event.getUserId())
                    .extractedText(event.getExtractedText())
                    .languageDetected(event.getLanguageDetected())
                    .originalFilename(event.getOriginalFilename())
                    .extractedText(event.getExtractedText())
                    .documentId(event.getDocumentId())
                    .build();

            kafkaTemplate.send(QA_GENERATION_TOPIC, eventRequest);
            log.info("Sent QA generation request for document: {}", event.getDocumentId());

        } catch (Exception e) {
            log.error("Error requesting QA generation for document {}: {}", event.getDocumentId(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }
}