package com.hand.demo.exception;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.security.auth.login.CredentialException;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .collect(Collectors.toList());

        return ApiErrorBuilder.badRequest(errors, request.getRequestURI());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        return ApiErrorBuilder.notFound(ex.getMessage(), request.getRequestURI());
    }

  /*  @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {
TODO 
       return ApiErrorBuilder.serverError("Something  wrong", request.getRequestURI());
    }*/

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
