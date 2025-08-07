package com.marcos.studyasistant.qagenerationservice.service.impl;

import com.marcos.studyasistant.qagenerationservice.client.HuggingFaceQAClient;
import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationRequest;
import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationResponse;
import com.marcos.studyasistant.qagenerationservice.service.QAGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class QAGenerationServiceImpl implements QAGenerationService {

    private final HuggingFaceQAClient huggingFaceQAClient;

    private static final Map<String, String> LANGUAGE_MODELS = Map.of(
            "en", "google/flan-t5-base",
            "es", "google/flan-t5-base",
            "fr", "google/flan-t5-base"
    );

    private static final Map<String, String> QA_MODELS = Map.of(
            "en", "deepset/roberta-base-squad2",
            "es", "deepset/roberta-base-squad2-distilled",
            "fr", "etalab-ia/camembert-base-squadFR-fquad-piaf"
    );

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es", "fr");

    private static final List<String> QUESTION_TYPES = List.of(
            "FACTUAL", "CONCEPTUAL", "ANALYTICAL", "PROCEDURAL", "METACOGNITIVE"
    );

    private static final List<String> DIFFICULTY_LEVELS = List.of(
            "EASY", "MEDIUM", "HARD"
    );

    @Override
    @Async("qaGenerationTaskExecutor")
    public CompletableFuture<QAGenerationResponse> generateQuestions(QAGenerationRequest request) {
        log.info("Starting Q&A generation for document: {}", request.getDocumentId());

        long startTime = System.currentTimeMillis();

        try {
            String language = extractLanguageCode(request.getLanguage());

            if (!isLanguageSupported(language)) {
                throw new IllegalArgumentException("Language not supported: " + language);
            }

            String model = getModelForLanguage(language);
            List<String> generatedQuestions = huggingFaceQAClient.generateQuestions(
                    request.getText(), model, request.getQuestionCount()
            );

            List<QAGenerationResponse.QuestionAnswerDto> questionAnswers =
                    generateQuestionAnswerPairs(generatedQuestions, request.getText(), language);

            long processingTime = System.currentTimeMillis() - startTime;

            QAGenerationResponse response = QAGenerationResponse.builder()
                    .qaId(UUID.randomUUID())
                    .documentId(request.getDocumentId())
                    .modelUsed(model)
                    .questionsGenerated(questionAnswers.size())
                    .questionAnswers(questionAnswers)
                    .processingTimeMs(processingTime)
                    .createdAt(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();

            log.info("Q&A generation completed for document: {} with {} questions in {}ms",
                    request.getDocumentId(), questionAnswers.size(), processingTime);

            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("Error generating Q&A for document {}: {}", request.getDocumentId(), e.getMessage(), e);

            QAGenerationResponse errorResponse = QAGenerationResponse.builder()
                    .qaId(UUID.randomUUID())
                    .documentId(request.getDocumentId())
                    .status("FAILED")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .createdAt(LocalDateTime.now())
                    .build();

            return CompletableFuture.completedFuture(errorResponse);
        }
    }

    @Override
    @Async("qaGenerationTaskExecutor")
    public CompletableFuture<Void> processQAGenerationRequest(String text, String language, int questionCount) {
        log.info("Processing Q&A generation request for text length: {}, language: {}, questions: {}",
                text.length(), language, questionCount);

        try {
            String languageCode = extractLanguageCode(language);

            if (!isLanguageSupported(languageCode)) {
                log.warn("Unsupported language: {}", languageCode);
                return CompletableFuture.completedFuture(null);
            }

            String model = getModelForLanguage(languageCode);
            List<String> questions = huggingFaceQAClient.generateQuestions(text, model, questionCount);

            log.info("Generated {} questions for text of {} characters", questions.size(), text.length());

        } catch (Exception e) {
            log.error("Error processing Q&A generation request: {}", e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isLanguageSupported(String language) {
        if (language == null) return false;
        String languageCode = extractLanguageCode(language);
        return SUPPORTED_LANGUAGES.contains(languageCode);
    }

    @Override
    public String getDefaultModel() {
        return "google/flan-t5-base";
    }

    @Override
    public int getMaxQuestions() {
        return 20;
    }

    private List<QAGenerationResponse.QuestionAnswerDto> generateQuestionAnswerPairs(
            List<String> questions, String context, String language) {

        List<QAGenerationResponse.QuestionAnswerDto> questionAnswers = new ArrayList<>();
        String qaModel = getQAModelForLanguage(language);
        Random random = new Random();

        for (String question : questions) {
            try {
                String answer = huggingFaceQAClient.generateAnswer(context, question, qaModel);

                QAGenerationResponse.QuestionAnswerDto qa = QAGenerationResponse.QuestionAnswerDto.builder()
                        .question(question)
                        .answer(answer)
                        .difficultyLevel(getRandomDifficultyLevel(random))
                        .questionType(getRandomQuestionType(random))
                        .confidenceScore(0.75 + random.nextDouble() * 0.25) // 0.75-1.0
                        .build();

                questionAnswers.add(qa);
            } catch (Exception e) {
                log.warn("Failed to generate answer for question: {}", question, e);
            }
        }

        return questionAnswers;
    }

    private String getModelForLanguage(String language) {
        return LANGUAGE_MODELS.getOrDefault(language, getDefaultModel());
    }

    private String getQAModelForLanguage(String language) {
        return QA_MODELS.getOrDefault(language, "deepset/roberta-base-squad2");
    }

    private String extractLanguageCode(String language) {
        if (language == null) return "en";
        return language.toLowerCase().substring(0, Math.min(2, language.length()));
    }

    private String getRandomQuestionType(Random random) {
        return QUESTION_TYPES.get(random.nextInt(QUESTION_TYPES.size()));
    }

    private String getRandomDifficultyLevel(Random random) {
        return DIFFICULTY_LEVELS.get(random.nextInt(DIFFICULTY_LEVELS.size()));
    }
}