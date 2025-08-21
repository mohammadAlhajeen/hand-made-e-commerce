package com.hand.demo.exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.CredentialException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        
        });
        
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", errors);
        
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.notFound(ex.getMessage(), request.getRequestURI());
    }

   @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

       return ApiErrorBuilder.serverError("Something  wrong", request.getRequestURI());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.unauthorized(ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(
            IOException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.ioError(ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(
            ExpiredJwtException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.unauthorized("JWT token has expired", request.getRequestURI());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.illegalArgumentException(ex.getMessage(), request.getRequestURI());
    }
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointer(
            NullPointerException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.serverError("Null pointer exception", request.getRequestURI());
    }
    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<ErrorResponse> handleCredentialException(
            CredentialException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.unauthorized(ex.getMessage(), request.getRequestURI());
    }
}
