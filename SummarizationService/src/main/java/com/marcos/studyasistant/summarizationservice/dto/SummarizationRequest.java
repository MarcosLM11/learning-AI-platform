package com.marcos.studyasistant.summarizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationRequest {
    
    @NotNull
    private UUID documentId;
    
    @NotNull
    private UUID userId;
    
    @NotBlank
    private String text;
    
    private String language;
    
    private String originalFilename;
    
    private Integer maxSummaryLength;
    
    private String modelPreference;
}