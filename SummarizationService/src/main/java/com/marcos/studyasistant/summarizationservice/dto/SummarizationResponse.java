package com.marcos.studyasistant.summarizationservice.dto;

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
public class SummarizationResponse {
    
    private UUID summaryId;
    private UUID documentId;
    private String summaryText;
    private String modelUsed;
    private Integer summaryLength;
    private Integer originalTextLength;
    private Double compressionRatio;
    private Long processingTimeMs;
    private LocalDateTime createdAt;
    private String status;
}