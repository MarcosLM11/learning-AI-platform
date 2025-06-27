package com.marcos.studyasistant.summarizationservice.controller;

import com.marcos.studyasistant.summarizationservice.dto.SummarizationRequest;
import com.marcos.studyasistant.summarizationservice.dto.SummarizationResponse;
import com.marcos.studyasistant.summarizationservice.service.SummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/summarization")
@RequiredArgsConstructor
@Slf4j
public class SummarizationController {

    private final SummarizationService summarizationService;

    @PostMapping("/summarize")
    public CompletableFuture<ResponseEntity<SummarizationResponse>> summarizeText(
            @Valid @RequestBody SummarizationRequest request) {
        log.info("Received summarization request for document: {}", request.getDocumentId());
        
        return summarizationService.summarizeText(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error in summarization endpoint: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/models/default")
    public ResponseEntity<String> getDefaultModel() {
        return ResponseEntity.ok(summarizationService.getDefaultModel());
    }

    @GetMapping("/languages/{language}/supported")
    public ResponseEntity<Boolean> isLanguageSupported(@PathVariable String language) {
        boolean supported = summarizationService.isLanguageSupported(language);
        return ResponseEntity.ok(supported);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Summarization Service is running");
    }
}