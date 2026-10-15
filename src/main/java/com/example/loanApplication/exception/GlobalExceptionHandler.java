package com.example.loanApplication.exception;

import com.example.loanApplication.apiResponse.ResponseApi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import com.example.loanApplication.apiResponse.ResponseApi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

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

    /*@ExceptionHandler(MethodArgumentNotValidException.class)
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
    }*/

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




    @ExceptionHandler(LoanAccountNotFoundException.class)
    public ResponseEntity<ResponseApi<Object>> handleLoanAccountNotFoundException(LoanAccountNotFoundException ex){
        ResponseApi<Object> responseApi = ResponseApi.builder().success(false).message(ex.getMessage()).data(null).build();

        return new ResponseEntity<>(
                responseApi, HttpStatus.NOT_FOUND
        );

    }



    @ExceptionHandler(EmiScheduleNotFound.class)

    public  ResponseEntity<ResponseApi<Object>> handleEmiScheduleNotFoundException(EmiScheduleNotFound es){

        ResponseApi<Object> responseApi = ResponseApi.builder().success(false).message(es.getMessage()).data(null).build();
        return  new ResponseEntity<>(
                responseApi, HttpStatus.NOT_FOUND
        );


    }


}
