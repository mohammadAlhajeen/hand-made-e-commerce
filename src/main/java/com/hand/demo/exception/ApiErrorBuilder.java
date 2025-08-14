package com.hand.demo.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiErrorBuilder {

    public static ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            List<String> messages,
            String path,
            String errorLabel
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorLabel,
                messages,
                path
        );
        return ResponseEntity.status(status).body(response);
    }

    public static ResponseEntity<ErrorResponse> badRequest(List<String> messages, String path) {
        return build(HttpStatus.BAD_REQUEST, messages, path, "Bad Request");
    }

    public static ResponseEntity<ErrorResponse> notFound(String message, String path) {
        return build(HttpStatus.NOT_FOUND, List.of(message), path, "Not Found");
    }
    public static ResponseEntity<ErrorResponse> illegalArgumentException(String message, String path) {
        return build(HttpStatus.BAD_REQUEST, List.of(message), path, "invalid inputs");
    }


    public static ResponseEntity<ErrorResponse> serverError(String message, String path) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, List.of(message), path, "Server Error");
    }

    public static ResponseEntity<ErrorResponse> unauthorized(String message, String path) {
        return build(HttpStatus.UNAUTHORIZED, List.of(message), path, "Unauthorized");
    }

    public static ResponseEntity<ErrorResponse> ioError(String message, String path) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, List.of(message), path, "IO Error");
    }
}
