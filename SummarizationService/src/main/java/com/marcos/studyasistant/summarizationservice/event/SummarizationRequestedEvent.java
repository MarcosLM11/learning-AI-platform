package com.marcos.studyasistant.summarizationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationRequestedEvent {
    private UUID documentId;
    private UUID userId;
    private String extractedText;
    private String languageDetected;
    private String originalFilename;
    private UUID summaryId;
}