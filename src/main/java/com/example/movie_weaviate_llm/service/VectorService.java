package com.example.movie_weaviate_llm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VectorService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weaviate.graphql-endpoint:http://localhost:8080/v1/graphql}")
    private String weaviateUrl;

    public String retrieveRelevantContent(String query) {
        // TODO: Replace this with actual embedding logic (call Python microservice or use JNI)
        List<Double> fakeEmbedding = getMockEmbedding(query); // Replace this later

        String vectorString = fakeEmbedding.toString();

        String gqlQuery = """
        {
          Get {
            Movie (
              nearVector: {
                vector: %s
              },
              limit: 1
            ) {
              title
              text
            }
          }
        }
        """.formatted(vectorString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = Map.of("query", gqlQuery);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                weaviateUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Parse response
        try {
            var data = (Map<?, ?>) response.getBody().get("data");
            var get = (List<?>) ((Map<?, ?>) data.get("Get")).get("Movie");
            if (get.isEmpty()) return "No relevant content found.";

            var movie = (Map<?, ?>) get.get(0);
            return movie.get("text").toString();
        } catch (Exception e) {
            return "Error parsing Weaviate response: " + e.getMessage();
        }
    }

    // Temporary dummy embedding generator (replace this)
    private List<Double> getMockEmbedding(String query) {
        Random rand = new Random(query.hashCode());
        List<Double> vec = new ArrayList<>();
        for (int i = 0; i < 384; i++) vec.add(rand.nextDouble() - 0.5); // 384-dim example
        return vec;
    }
}