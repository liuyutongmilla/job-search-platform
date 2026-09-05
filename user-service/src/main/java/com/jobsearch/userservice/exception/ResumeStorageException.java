package com.jobsearch.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResumeStorageException extends RuntimeException {

    public ResumeStorageException(String message) {
        super(message);
    }
}
