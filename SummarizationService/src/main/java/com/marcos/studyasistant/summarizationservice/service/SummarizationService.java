package com.marcos.studyasistant.summarizationservice.service;

import com.marcos.studyasistant.summarizationservice.dto.SummarizationRequest;
import com.marcos.studyasistant.summarizationservice.dto.SummarizationResponse;

import java.util.concurrent.CompletableFuture;

public interface SummarizationService {
    
    CompletableFuture<SummarizationResponse> summarizeText(SummarizationRequest request);
    
    CompletableFuture<Void> processSummarizationRequest(String text, String language, int maxLength);
    
    boolean isLanguageSupported(String language);
    
    String getDefaultModel();
}