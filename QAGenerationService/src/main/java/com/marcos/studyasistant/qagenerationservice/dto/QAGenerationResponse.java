package com.marcos.studyasistant.qagenerationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QAGenerationResponse {
    
    private UUID qaId;
    private UUID documentId;
    private String modelUsed;
    private Integer questionsGenerated;
    private List<QuestionAnswerDto> questionAnswers;
    private Long processingTimeMs;
    private LocalDateTime createdAt;
    private String status;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswerDto {
        private String question;
        private String answer;
        private String difficultyLevel;
        private String questionType;
        private Double confidenceScore;
    }
}