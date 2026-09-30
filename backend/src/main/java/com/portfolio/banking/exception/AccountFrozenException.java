package com.portfolio.banking.exception;

public class AccountFrozenException extends BankingException {
    public AccountFrozenException(String accountNumber) {
        super(String.format("Account %s is FROZEN or INACTIVE. Transactions are prohibited.", accountNumber));
    }
}
