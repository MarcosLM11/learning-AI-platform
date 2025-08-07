package com.marcos.studyasistant.qagenerationservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HuggingFaceQAClient {

    private final RestTemplate restTemplate;

    @Value("${huggingface.api.token}")
    private String apiToken;

    @Value("${huggingface.api.url:https://api-inference.huggingface.co/models}")
    private String baseUrl;

    public List<String> generateQuestions(String text, String model, int questionCount) {
        String url = baseUrl + "/" + model;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        String prompt = buildQuestionGenerationPrompt(text, questionCount);

        Map<String, Object> requestBody = Map.of(
                "inputs", prompt,
                "parameters", Map.of(
                        "max_new_tokens", 500,
                        "temperature", 0.7,
                        "do_sample", true,
                        "return_full_text", false
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> results = response.getBody();
                if (!results.isEmpty()) {
                    String generatedText = (String) results.get(0).get("generated_text");
                    return parseQuestionsFromResponse(generatedText);
                }
            }

            throw new RuntimeException("Failed to generate questions from Hugging Face API");

        } catch (Exception e) {
            log.error("Error calling Hugging Face API for question generation: {}", e.getMessage(), e);
            throw new RuntimeException("Hugging Face API call failed", e);
        }
    }

    public String generateAnswer(String context, String question, String model) {
        String url = baseUrl + "/" + model.replace("generation", "question-answering");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        Map<String, Object> requestBody = Map.of(
                "inputs", Map.of(
                        "question", question,
                        "context", context
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("answer");
            }

            return "Unable to generate answer";

        } catch (Exception e) {
            log.error("Error generating answer: {}", e.getMessage(), e);
            return "Error generating answer";
        }
    }

    private String buildQuestionGenerationPrompt(String text, int questionCount) {
        return String.format(
                "Generate %d questions based on the following text. Each question should be on a new line starting with 'Q:':\n\nText: %s\n\nQuestions:",
                questionCount, text.length() > 1000 ? text.substring(0, 1000) + "..." : text
        );
    }

    private List<String> parseQuestionsFromResponse(String response) {
        return List.of(response.split("Q:"))
                .stream()
                .filter(q -> !q.trim().isEmpty())
                .map(q -> q.trim().replaceAll("^\\d+\\.\\s*", ""))
                .filter(q -> q.length() > 10)
                .limit(20)
                .toList();
    }
}