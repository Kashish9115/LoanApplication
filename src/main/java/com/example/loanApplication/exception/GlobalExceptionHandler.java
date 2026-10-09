package com.example.loanApplication.exception;


import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import com.example.loanApplication.apiResponse.ResponseApi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseApi<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ResponseApi<Map<String, String>> response =
                ResponseApi.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed for input fields")
                        .data(errors)
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }

    // Handles validation errors
    // e.g., Investment > 30% of income, invalid input formats
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseApi<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex
    ) {

        ResponseApi<Object> response = ResponseApi.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }

    // Handles state errors
    // e.g., Unverified Email, Revoked Refresh Token
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseApi<Object>> handleIllegalStateException(
            IllegalStateException ex
    ) {

        ResponseApi<Object> response = ResponseApi.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.FORBIDDEN
        );
    }

    // Catch-all for unexpected server exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Object>> handleGenericException(
            Exception ex
    ) {

        ResponseApi<Object> response = ResponseApi.builder()
                .success(false)
                .message(
                        "An internal server error occurred: "
                                + ex.getMessage()
                )
                .data(null)
                .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
