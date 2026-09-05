package com.jobsearch.jobservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class JobAlreadyExistsException extends RuntimeException {

    public JobAlreadyExistsException(String message) {
        super(message);
    }
}
