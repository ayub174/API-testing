package com.romkalkylator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Central felhantering som översätter undantag till tydliga JSON-svar.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OgiltigFilException.class)
    public ResponseEntity<Map<String, Object>> handleOgiltigFil(OgiltigFilException ex) {
        return byggSvar(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleForStorFil(MaxUploadSizeExceededException ex) {
        return byggSvar(HttpStatus.BAD_REQUEST, "Filen är för stor för att läsas in.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return byggSvar(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> byggSvar(HttpStatus status, String meddelande) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", meddelande);
        return ResponseEntity.status(status).body(body);
    }
}
