package com.example.loanApplication.exception;

public class SignatureVerificationException extends RuntimeException {
    public SignatureVerificationException(String message) {
        super(message);
    }
}
