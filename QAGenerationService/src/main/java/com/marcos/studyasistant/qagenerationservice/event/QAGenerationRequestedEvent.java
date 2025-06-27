package com.marcos.studyasistant.qagenerationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QAGenerationRequestedEvent {
    private UUID documentId;
    private UUID userId;
    private String extractedText;
    private String languageDetected;
    private String originalFilename;
    private UUID qaId;
    private Integer desiredQuestionCount;
}