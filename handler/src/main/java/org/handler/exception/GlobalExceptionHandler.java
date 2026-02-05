package org.handler.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = buildErrorResponseWithDetails(HttpStatus.BAD_REQUEST, "Validation Failed", request).getBody();
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponseWithDetails(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponseWithDetails(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCaseNotFoundException(CaseNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponseWithDetails(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ProcessingActionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProcessingActionNotFoundException(ProcessingActionNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponseWithDetails(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponseWithDetails(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponseWithDetails(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCommentNotFoundException(CommentNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponseWithDetails(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(PromptNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePromptNotFoundException(PromptNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponseWithDetails(HttpStatus.CONFLICT, ex.getMessage(), request);
    }
}
