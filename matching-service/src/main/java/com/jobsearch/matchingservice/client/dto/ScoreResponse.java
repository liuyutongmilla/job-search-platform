package com.jobsearch.matchingservice.client.dto;

import java.util.List;


public record ScoreResponse(
        
        String engine,
        List<ScoreResult> results
) {
    public static ScoreResponse ruleBased(List<ScoreResult> results) {
        return new ScoreResponse("RULE_BASED", results);
    }
}
