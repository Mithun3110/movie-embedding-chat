package com.example.movie_weaviate_llm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryResult {
    private String context;
    private String answer;
}
