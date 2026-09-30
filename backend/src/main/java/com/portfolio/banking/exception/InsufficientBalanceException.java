package com.portfolio.banking.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BankingException {
    public InsufficientBalanceException(String accountNumber, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Insufficient funds in account %s. Current balance: %s, Requested amount: %s",
                accountNumber, currentBalance, requestedAmount));
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
