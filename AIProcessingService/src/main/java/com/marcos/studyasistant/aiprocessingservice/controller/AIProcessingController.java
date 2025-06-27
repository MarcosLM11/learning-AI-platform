package com.marcos.studyasistant.aiprocessingservice.controller;

import com.marcos.studyasistant.aiprocessingservice.entity.DocumentQA;
import com.marcos.studyasistant.aiprocessingservice.entity.DocumentSummary;
import com.marcos.studyasistant.aiprocessingservice.repository.DocumentQARepository;
import com.marcos.studyasistant.aiprocessingservice.repository.DocumentSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai-processing")
@RequiredArgsConstructor
@Slf4j
public class AIProcessingController {

    private final DocumentSummaryRepository documentSummaryRepository;
    private final DocumentQARepository documentQARepository;

    @GetMapping("/summaries/{documentId}")
    public ResponseEntity<DocumentSummary> getSummaryByDocumentId(@PathVariable UUID documentId) {
        log.info("Getting summary for document: {}", documentId);
        
        return documentSummaryRepository.findByDocumentId(documentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/qa/{documentId}")
    public ResponseEntity<DocumentQA> getQAByDocumentId(@PathVariable UUID documentId) {
        log.info("Getting QA for document: {}", documentId);
        
        return documentQARepository.findByDocumentId(documentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summaries/user/{userId}")
    public ResponseEntity<List<DocumentSummary>> getSummariesByUserId(@PathVariable UUID userId) {
        log.info("Getting summaries for user: {}", userId);
        
        List<DocumentSummary> summaries = documentSummaryRepository.findByUserId(userId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/qa/user/{userId}")
    public ResponseEntity<List<DocumentQA>> getQAByUserId(@PathVariable UUID userId) {
        log.info("Getting QA for user: {}", userId);
        
        List<DocumentQA> qaList = documentQARepository.findByUserId(userId);
        return ResponseEntity.ok(qaList);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Processing Service is running");
    }
}