package com.example.loanApplication.exception;

import com.example.loanApplication.apiResponse.ResponseApi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseApi<Object>> handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {

        ResponseApi<Object> response =
                ResponseApi.builder()
                        .success(false)
                        .message(exception.getMessage())
                        .data(null)
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }


    @ExceptionHandler(InvalidLoanDealException.class)
    public ResponseEntity<ResponseApi<Object>> handleInvalidLoanDealException(
            InvalidLoanDealException exception
    ) {

        ResponseApi<Object> response =
                ResponseApi.builder()
                        .success(false)
                        .message(exception.getMessage())
                        .data(null)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseApi<Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            validationErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ResponseApi<Object> response =
                ResponseApi.builder()
                        .success(false)
                        .message("Validation failed")
                        .data(validationErrors)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Object>> handleGeneralException(
            Exception exception
    ) {


        exception.printStackTrace();

        ResponseApi<Object> response =
                ResponseApi.builder()
                        .success(false)
                        .message(exception.getMessage())
                        .data(null)
                        .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}