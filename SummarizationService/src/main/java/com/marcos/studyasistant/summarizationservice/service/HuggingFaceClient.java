package com.marcos.studyasistant.summarizationservice.client;

import lombok.Data;
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
public class HuggingFaceClient {

    private final RestTemplate restTemplate;

    @Value("${huggingface.api.token}")
    private String apiToken;

    @Value("${huggingface.api.url:https://api-inference.huggingface.co/models}")
    private String baseUrl;

    public String summarizeText(String text, String model) {
        String url = baseUrl + "/" + model;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        Map<String, Object> requestBody = Map.of(
                "inputs", text,
                "parameters", Map.of(
                        "max_length", 150,
                        "min_length", 30,
                        "do_sample", false
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
                    return (String) results.get(0).get("summary_text");
                }
            }

            throw new RuntimeException("Failed to get summary from Hugging Face API");

        } catch (Exception e) {
            log.error("Error calling Hugging Face API: {}", e.getMessage(), e);
            throw new RuntimeException("Hugging Face API call failed", e);
        }
    }
}