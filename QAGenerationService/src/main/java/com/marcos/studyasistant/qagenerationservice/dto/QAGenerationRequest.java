package com.marcos.studyasistant.qagenerationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QAGenerationRequest {
    
    @NotNull
    private UUID documentId;
    
    @NotNull
    private UUID userId;
    
    @NotBlank
    private String text;
    
    private String language;
    
    private String originalFilename;
    
    @Min(1)
    @Max(20)
    private Integer questionCount = 5;
    
    private String difficultyLevel; // EASY, MEDIUM, HARD
    
    private String questionType; // FACTUAL, CONCEPTUAL, ANALYTICAL
    
    private String modelPreference;
}