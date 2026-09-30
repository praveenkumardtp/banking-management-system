package com.portfolio.banking.service.impl;

import com.portfolio.banking.dto.request.DepositRequest;
import com.portfolio.banking.dto.request.TransferRequest;
import com.portfolio.banking.dto.request.WithdrawalRequest;
import com.portfolio.banking.dto.response.TransactionResponse;
import com.portfolio.banking.entity.*;
import com.portfolio.banking.exception.AccountFrozenException;
import com.portfolio.banking.exception.InsufficientBalanceException;
import com.portfolio.banking.exception.InvalidTransactionException;
import com.portfolio.banking.exception.ResourceNotFoundException;
import com.portfolio.banking.mapper.TransactionMapper;
import com.portfolio.banking.repository.AccountRepository;
import com.portfolio.banking.repository.TransactionRepository;
import com.portfolio.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse deposit(DepositRequest request) {
        log.info("Processing DEPOSIT: {} to account {}", request.getAmount(), request.getAccountNumber());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be strictly greater than zero");
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", request.getAccountNumber()));

        validateAccountActive(account);

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String ref = generateReferenceNumber("DEP");
        Transaction transaction = Transaction.builder()
                .referenceNumber(ref)
                .toAccount(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null && !request.getDescription().isBlank()
                        ? request.getDescription() : "Standard Cash/Online Deposit")
                .timestamp(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        log.info("DEPOSIT SUCCESS: Ref {}, Account {}, New Balance {}", ref, account.getAccountNumber(), newBalance);
        return transactionMapper.toResponse(savedTx);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse withdraw(WithdrawalRequest request) {
        log.info("Processing WITHDRAWAL: {} from account {}", request.getAmount(), request.getAccountNumber());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be strictly greater than zero");
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", request.getAccountNumber()));

        validateAccountActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Withdrawal denied: Account {} has insufficient funds. Balance: {}, Requested: {}",
                    account.getAccountNumber(), account.getBalance(), request.getAmount());
            throw new InsufficientBalanceException(account.getAccountNumber(), account.getBalance(), request.getAmount());
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String ref = generateReferenceNumber("WTH");
        Transaction transaction = Transaction.builder()
                .referenceNumber(ref)
                .fromAccount(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null && !request.getDescription().isBlank()
                        ? request.getDescription() : "ATM/Branch Cash Withdrawal")
                .timestamp(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        log.info("WITHDRAWAL SUCCESS: Ref {}, Account {}, New Balance {}", ref, account.getAccountNumber(), newBalance);
        return transactionMapper.toResponse(savedTx);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Processing TRANSFER: {} from {} to {}",
                request.getAmount(), request.getFromAccountNumber(), request.getToAccountNumber());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be strictly greater than zero");
        }

        if (request.getFromAccountNumber().equalsIgnoreCase(request.getToAccountNumber())) {
            throw new InvalidTransactionException("Cannot transfer funds to the same account.");
        }

        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Source Account", "accountNumber", request.getFromAccountNumber()));

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination Account", "accountNumber", request.getToAccountNumber()));

        validateAccountActive(fromAccount);
        validateAccountActive(toAccount);

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Transfer denied: Account {} balance {} is less than transfer amount {}",
                    fromAccount.getAccountNumber(), fromAccount.getBalance(), request.getAmount());
            throw new InsufficientBalanceException(fromAccount.getAccountNumber(), fromAccount.getBalance(), request.getAmount());
        }

        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(request.getAmount());
        BigDecimal toNewBalance = toAccount.getBalance().add(request.getAmount());

        fromAccount.setBalance(fromNewBalance);
        toAccount.setBalance(toNewBalance);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        String ref = generateReferenceNumber("TRF");
        Transaction transaction = Transaction.builder()
                .referenceNumber(ref)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .transactionType(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .balanceAfter(fromNewBalance)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null && !request.getDescription().isBlank()
                        ? request.getDescription() : String.format("Transfer to %s", toAccount.getAccountNumber()))
                .timestamp(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        log.info("TRANSFER SUCCESS: Ref {}, From {} to {}, Amount {}",
                ref, fromAccount.getAccountNumber(), toAccount.getAccountNumber(), request.getAmount());
        return transactionMapper.toResponse(savedTx);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String referenceNumber) {
        Transaction tx = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "referenceNumber", referenceNumber));
        return transactionMapper.toResponse(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactions(
            String accountNumber,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        String sanitizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        return transactionRepository.filterAccountTransactions(
                account.getId(),
                type,
                startDate,
                endDate,
                sanitizedSearch,
                pageable
        ).map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search,
            Pageable pageable) {

        String sanitizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        return transactionRepository.filterAllTransactions(
                type,
                startDate,
                endDate,
                sanitizedSearch,
                pageable
        ).map(transactionMapper::toResponse);
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException(account.getAccountNumber());
        }
    }

    private String generateReferenceNumber(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
