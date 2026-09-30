package com.portfolio.banking.service;

import com.portfolio.banking.dto.request.DepositRequest;
import com.portfolio.banking.dto.request.TransferRequest;
import com.portfolio.banking.dto.request.WithdrawalRequest;
import com.portfolio.banking.dto.response.TransactionResponse;
import com.portfolio.banking.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TransactionService {
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawalRequest request);
    TransactionResponse transfer(TransferRequest request);
    TransactionResponse getTransactionByReference(String referenceNumber);
    Page<TransactionResponse> getAccountTransactions(
            String accountNumber,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable
    );
    Page<TransactionResponse> getAllTransactions(
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable
    );
}
