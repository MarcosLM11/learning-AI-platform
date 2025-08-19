package com.marcos.studyasistant.aiprocessingservice.service;

import com.marcos.studyasistant.aiprocessingservice.enums.AIProcessingType;
import com.marcos.studyasistant.aiprocessingservice.event.DocumentProcessingCompletedEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AIProcessingCoordinatorService {
    
    CompletableFuture<Void> processDocument(DocumentProcessingCompletedEvent event);
    
    CompletableFuture<Void> requestSummarization(DocumentProcessingCompletedEvent event, AIProcessingType processingType);
    
    CompletableFuture<Void> requestQAGeneration(DocumentProcessingCompletedEvent event, AIProcessingType processingType);
}