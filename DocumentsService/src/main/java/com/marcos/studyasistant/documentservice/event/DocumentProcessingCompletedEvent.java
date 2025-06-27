package com.marcos.studyasistant.documentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProcessingCompletedEvent {
    private UUID documentId;
    private UUID userId;
    private String originalFilename;
    private String extractedText;
    private String languageDetected;
    private Integer pageCount;
    private LocalDateTime processedAt;
    private String mimeType;
    private Long fileSize;
}