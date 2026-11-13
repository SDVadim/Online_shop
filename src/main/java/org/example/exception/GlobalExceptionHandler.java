package org.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException e,
        HttpServletRequest request
    ) {
        return errorMapper(HttpStatus.NOT_FOUND, e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CategoryNotEmptyException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotEmptyException(
        CategoryNotEmptyException e,
        HttpServletRequest request
    ) {
        return errorMapper(HttpStatus.BAD_REQUEST, e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e,
        HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            fieldErrors.put(field, message);
        });
        return errorMapper(HttpStatus.BAD_REQUEST, e.getMessage(), request, fieldErrors);
    }


    private ResponseEntity<ErrorResponse> errorMapper(
        HttpStatus status,
        String message,
        HttpServletRequest request,
        Map<String, String> fieldErrors
    ) {
        ErrorResponse error = new ErrorResponse(Instant.now(), status.value(), message, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(error);
    }

    @Builder
    private record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String path,
        Map<String, String> fieldErrors
    ) {
    }
}
