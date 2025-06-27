package com.marcos.studyasistant.qagenerationservice.event;

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
public class QAGenerationCompletedEvent {
    private UUID qaId;
    private UUID documentId;
    private UUID userId;
    private String modelUsed;
    private Integer questionsGenerated;
    private List<GeneratedQuestionAnswer> questionAnswers;
    private Long processingTimeMs;
    private LocalDateTime completedAt;
    private boolean success;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedQuestionAnswer {
        private String question;
        private String answer;
        private String difficultyLevel;
        private String questionType;
        private Double confidenceScore;
    }
}