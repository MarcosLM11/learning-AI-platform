package com.marcos.studyasistant.qagenerationservice.service;

import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationRequest;
import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationResponse;

import java.util.concurrent.CompletableFuture;

public interface QAGenerationService {
    
    CompletableFuture<QAGenerationResponse> generateQuestions(QAGenerationRequest request);
    
    CompletableFuture<Void> processQAGenerationRequest(String text, String language, int questionCount);
    
    boolean isLanguageSupported(String language);
    
    String getDefaultModel();
    
    int getMaxQuestions();
}