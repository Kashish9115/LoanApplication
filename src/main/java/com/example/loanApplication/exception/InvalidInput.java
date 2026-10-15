package com.example.loanApplication.exception;

public class InvalidInput extends RuntimeException {
    public InvalidInput(String message) {
        super(message);
    }
}
