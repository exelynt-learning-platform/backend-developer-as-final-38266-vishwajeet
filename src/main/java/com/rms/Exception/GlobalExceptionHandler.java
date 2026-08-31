
package com.rms.Exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.rms.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    
    // RESOURCE NOT FOUND


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            WebRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    
    // RESERVATION NOT FOUND
    

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFound(
            ReservationNotFoundException exception,
            WebRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    
    // USER NOT FOUND
    

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException exception,
            WebRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    // ILLEGAL ARGUMENT
    

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            WebRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    // GENERAL EXCEPTION
    

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception,
            WebRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred",
                        request);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    private ErrorResponse createErrorResponse(
            HttpStatus status,
            String message,
            WebRequest request) {

        String path = "";

        if (request instanceof ServletWebRequest servletWebRequest) {
            path = servletWebRequest
                    .getRequest()
                    .getRequestURI();
        }

        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                message,
                path);
    }
}

