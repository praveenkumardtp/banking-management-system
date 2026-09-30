package com.portfolio.banking.exception;

public class BadRequestException extends BankingException {
    public BadRequestException(String message) {
        super(message);
    }
}
