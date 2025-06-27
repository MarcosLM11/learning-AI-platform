package com.marcos.studyasistant.qagenerationservice.controller;

import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationRequest;
import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationResponse;
import com.marcos.studyasistant.qagenerationservice.service.QAGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/qa-generation")
@RequiredArgsConstructor
@Slf4j
public class QAGenerationController {

    private final QAGenerationService qaGenerationService;

    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<QAGenerationResponse>> generateQuestions(
            @Valid @RequestBody QAGenerationRequest request) {
        log.info("Received Q&A generation request for document: {}", request.getDocumentId());
        
        return qaGenerationService.generateQuestions(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error in Q&A generation endpoint: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/models/default")
    public ResponseEntity<String> getDefaultModel() {
        return ResponseEntity.ok(qaGenerationService.getDefaultModel());
    }

    @GetMapping("/languages/{language}/supported")
    public ResponseEntity<Boolean> isLanguageSupported(@PathVariable String language) {
        boolean supported = qaGenerationService.isLanguageSupported(language);
        return ResponseEntity.ok(supported);
    }

    @GetMapping("/questions/max")
    public ResponseEntity<Integer> getMaxQuestions() {
        return ResponseEntity.ok(qaGenerationService.getMaxQuestions());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Q&A Generation Service is running");
    }
}