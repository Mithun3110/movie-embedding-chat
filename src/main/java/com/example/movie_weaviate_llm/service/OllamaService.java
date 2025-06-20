package com.example.movie_weaviate_llm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OllamaService {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends a POST request to the Mistral model running via Ollama
     * with context and question, and returns the combined final answer.
     */
    public String askMistral(String context, String question) {
        String prompt = String.format("Using the following context:\n%s\n\nAnswer this question:\n%s", context, question);

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("stream", true);
        request.put("messages", List.of(
                Map.of("role", "system", "content", "Use the following context to answer the question."),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(ollamaUrl, entity, String.class);
        return extractFinalTextFromNdjson(response.getBody());
    }

    /**
     * Parses the raw NDJSON (streamed response) into a single readable text block.
     */
    private String extractFinalTextFromNdjson(String ndjson) {
        StringBuilder result = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();

        for (String line : ndjson.split("\n")) {
            if (line.trim().isEmpty()) continue;
            try {
                JsonNode node = mapper.readTree(line);
                JsonNode message = node.get("message");
                if (message != null && message.get("content") != null) {
                    result.append(message.get("content").asText());
                }
            } catch (Exception e) {
                // You can log the error here if needed
                continue;
            }
        }
        return result.toString().trim();
    }
}
