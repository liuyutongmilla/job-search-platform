package com.jobsearch.matchingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
public class ResumeNotReadyException extends RuntimeException {

    public ResumeNotReadyException(String message) {
        super(message);
    }
}
