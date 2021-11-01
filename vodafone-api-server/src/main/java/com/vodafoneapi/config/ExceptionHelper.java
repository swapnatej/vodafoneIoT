package com.vodafoneapi.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@Log4j2
@ControllerAdvice(basePackages = "com.vodafoneapi.controller")
public class ExceptionHelper {

    @ExceptionHandler(value = { NoSuchElementException.class })
    public ResponseEntity handleNoSuchElementException(NoSuchElementException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error("Exception: ",ex.getMessage());
        return new ResponseEntity<Object>(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
