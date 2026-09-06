package com.jobsearch.matchingservice.service;

import com.jobsearch.matchingservice.dto.MatchResponseDto;

public interface IMatchingService {

    
    MatchResponseDto computeMatches(Long userId);

    
    MatchResponseDto fetchMatches(Long userId);
}
