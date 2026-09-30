package com.portfolio.banking.service;

import com.portfolio.banking.dto.request.AccountStatusUpdateRequest;
import com.portfolio.banking.dto.request.CreateAccountRequest;
import com.portfolio.banking.dto.response.AccountResponse;
import com.portfolio.banking.dto.response.UserResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(Long userId, CreateAccountRequest request);
    AccountResponse getAccountByNumber(String accountNumber);
    List<AccountResponse> getAccountsByUserId(Long userId);
    BigDecimal getBalance(String accountNumber);
    AccountResponse updateAccountStatus(String accountNumber, AccountStatusUpdateRequest request);
    UserResponse getCurrentUserProfile(String email);
}
