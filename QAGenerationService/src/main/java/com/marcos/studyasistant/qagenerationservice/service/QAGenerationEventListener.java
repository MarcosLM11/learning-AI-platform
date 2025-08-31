package com.marcos.studyasistant.qagenerationservice.service;

import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationRequest;
import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationResponse;
import com.marcos.studyasistant.qagenerationservice.event.QAGenerationCompletedEvent;
import com.marcos.studyasistant.qagenerationservice.event.QAGenerationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QAGenerationEventListener {

    private final QAGenerationService qaGenerationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String QA_GENERATION_COMPLETED_TOPIC = "document.qa.completed";

    @KafkaListener(topics = "qa-requests")
    public void handleQAGenerationRequested(QAGenerationRequestedEvent event) {
        log.info("Received Q&A generation request for document: {}", event.getDocumentId());
        
        try {
            QAGenerationRequest request = QAGenerationRequest.builder()
                    .documentId(event.getDocumentId())
                    .userId(event.getUserId())
                    .text(event.getExtractedText())
                    .language(event.getLanguageDetected())
                    .originalFilename(event.getOriginalFilename())
                    .questionCount(event.getDesiredQuestionCount())
                    .build();
            
            qaGenerationService.generateQuestions(request)
                    .thenAccept(response -> publishQAGenerationCompleted(event, response))
                    .exceptionally(throwable -> {
                        log.error("Error processing Q&A generation for document {}: {}", 
                            event.getDocumentId(), throwable.getMessage());
                        publishQAGenerationFailed(event, throwable.getMessage());
                        return null;
                    });
                    
        } catch (Exception e) {
            log.error("Error handling Q&A generation request for document {}: {}", 
                event.getDocumentId(), e.getMessage(), e);
            publishQAGenerationFailed(event, e.getMessage());
        }
    }
    
    private void publishQAGenerationCompleted(QAGenerationRequestedEvent originalEvent, QAGenerationResponse response) {
        List<QAGenerationCompletedEvent.GeneratedQuestionAnswer> questionAnswers = response.getQuestionAnswers()
                .stream()
                .map(qa -> QAGenerationCompletedEvent.GeneratedQuestionAnswer.builder()
                        .question(qa.getQuestion())
                        .answer(qa.getAnswer())
                        .difficultyLevel(qa.getDifficultyLevel())
                        .questionType(qa.getQuestionType())
                        .confidenceScore(qa.getConfidenceScore())
                        .build())
                .collect(Collectors.toList());
        
        QAGenerationCompletedEvent completedEvent = QAGenerationCompletedEvent.builder()
                .qaId(originalEvent.getQaId())
                .documentId(originalEvent.getDocumentId())
                .userId(originalEvent.getUserId())
                .modelUsed(response.getModelUsed())
                .questionsGenerated(response.getQuestionsGenerated())
                .questionAnswers(questionAnswers)
                .processingTimeMs(response.getProcessingTimeMs())
                .completedAt(response.getCreatedAt())
                .success(true)
                .build();
        
        kafkaTemplate.send(QA_GENERATION_COMPLETED_TOPIC, completedEvent);
        log.info("Published Q&A generation completed event for document: {}", originalEvent.getDocumentId());
    }
    
    private void publishQAGenerationFailed(QAGenerationRequestedEvent originalEvent, String errorMessage) {
        QAGenerationCompletedEvent failedEvent = QAGenerationCompletedEvent.builder()
                .qaId(originalEvent.getQaId())
                .documentId(originalEvent.getDocumentId())
                .userId(originalEvent.getUserId())
                .success(false)
                .errorMessage(errorMessage)
                .build();
        
        kafkaTemplate.send(QA_GENERATION_COMPLETED_TOPIC, failedEvent);
        log.info("Published Q&A generation failed event for document: {}", originalEvent.getDocumentId());
    }
}