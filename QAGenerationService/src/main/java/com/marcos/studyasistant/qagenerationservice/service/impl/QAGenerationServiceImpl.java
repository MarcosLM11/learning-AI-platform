package com.marcos.studyasistant.qagenerationservice.service.impl;

import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationRequest;
import com.marcos.studyasistant.qagenerationservice.dto.QAGenerationResponse;
import com.marcos.studyasistant.qagenerationservice.service.QAGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class QAGenerationServiceImpl implements QAGenerationService {

    private final ChatClient chatClient;

    private static final String PROMPT_TEMPLATE = "You are an expert in question generation." +
            " Your task is to generate questions based on the provided text." +
            " Please ensure that the questions are clear, concise, and relevant to the content." +
            " The output should be a list of questions." +
            " The questions should be suitable for a study context, allowing users to test their understanding of" +
            " the material. It should contain a variety of questions types like HARD, EXTREME HARD , MEDIUM, EASY and VERY EASY." +
            " The output should be directly the questions and the answer to each question." +
            " Should be 20 questions in total." +
            " And must have this format: Question: <question text> Answer: <answer text>, separating each question-answer pair with a @.";

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es", "fr");

    public QAGenerationServiceImpl(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

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

            String QuestionsAndAnswerText = chatClient.prompt(PROMPT_TEMPLATE).user(request.getText()).call().content();

            long processingTime = System.currentTimeMillis() - startTime;

            QAGenerationResponse response = QAGenerationResponse.builder()
                    .qaId(UUID.randomUUID())
                    .documentId(request.getDocumentId())
                    .modelUsed("qwen3")
                    .questionsGenerated(20)
                    .questionAnswers(processModelAnswer(QuestionsAndAnswerText))
                    .processingTimeMs(processingTime)
                    .createdAt(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();

            log.info("Q&A generation completed for document: {} with {} questions in {}ms",
                    request.getDocumentId(), 20, processingTime);

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

    private List<QAGenerationResponse.QuestionAnswerDto> processModelAnswer(String questionsAndAnswerText) {
        List<QAGenerationResponse.QuestionAnswerDto> response = new ArrayList<>();

        List<String> qaPairs = Arrays.asList(questionsAndAnswerText.split("@"));
        for (String pair : qaPairs) {
            String[] parts = pair.split("Answer:", 2);
            if (parts.length == 2) {
                String questionPart = parts[0].trim();
                String answerPart = parts[1].trim();
                if (questionPart.startsWith("Question:")) {
                    String question = questionPart.substring("Question:".length()).trim();
                    String answer = answerPart.trim();
                    QAGenerationResponse.QuestionAnswerDto questionAnswerDto = QAGenerationResponse.QuestionAnswerDto.builder()
                            .question(question)
                            .answer(answer)
                            .difficultyLevel("MEDIUM") // Default difficulty, can be enhanced later
                            .questionType("GENERAL") // Default type, can be enhanced later
                            .confidenceScore(1.0) // Default confidence score, can be enhanced later
                            .build();
                    response.add(questionAnswerDto);
                }
            }
        }
        return response;
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

            String QuestionsAndAnswerText = chatClient.prompt(PROMPT_TEMPLATE).user(text).call().content();

            log.info("Generated {} questions for text of {} characters", 20, text.length());

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

    private String extractLanguageCode(String language) {
        if (language == null) return "en";
        return language.toLowerCase().substring(0, Math.min(2, language.length()));
    }
}