package com.example.movie_weaviate_llm.controller;

import com.example.movie_weaviate_llm.service.VectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QueryController {

    private final VectorService vectorService;

    // POST endpoint
    @PostMapping("/summarize")
    public ResponseEntity<String> summarizePost(@RequestBody Map<String, String> payload) {
        String query = payload.get("query");
        String result = vectorService.retrieveRelevantContent(query);
        return ResponseEntity.ok(result);
    }

    // GET endpoint
    @GetMapping("/summarize")
    public ResponseEntity<String> summarizeGet(@RequestParam("query") String query) {
        String result = vectorService.retrieveRelevantContent(query);
        return ResponseEntity.ok(result);
    }
}
