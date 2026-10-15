package com.example.loanApplication.exception;

public class LoanAccountNotFoundException extends RuntimeException {
    public LoanAccountNotFoundException(String message) {
        super(message);
    }
}
