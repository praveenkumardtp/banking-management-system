package com.portfolio.banking.service.impl;

import com.portfolio.banking.dto.request.AccountStatusUpdateRequest;
import com.portfolio.banking.dto.request.CreateAccountRequest;
import com.portfolio.banking.dto.response.AccountResponse;
import com.portfolio.banking.dto.response.UserResponse;
import com.portfolio.banking.entity.Account;
import com.portfolio.banking.entity.AccountStatus;
import com.portfolio.banking.entity.User;
import com.portfolio.banking.exception.BadRequestException;
import com.portfolio.banking.exception.ResourceNotFoundException;
import com.portfolio.banking.mapper.AccountMapper;
import com.portfolio.banking.mapper.UserMapper;
import com.portfolio.banking.repository.AccountRepository;
import com.portfolio.banking.repository.UserRepository;
import com.portfolio.banking.service.AccountService;
import com.portfolio.banking.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        log.info("Creating {} account for user id {}", request.getAccountType(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Initial deposit cannot be negative");
        }

        String accNum;
        do {
            accNum = AccountNumberGenerator.generate();
        } while (accountRepository.existsByAccountNumber(accNum));

        Account account = Account.builder()
                .accountNumber(accNum)
                .balance(initialDeposit)
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Successfully created account {}", saved.getAccountNumber());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return account.getBalance();
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(String accountNumber, AccountStatusUpdateRequest request) {
        log.info("Updating status for account {} to {}", accountNumber, request.getStatus());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        account.setStatus(request.getStatus());
        Account updated = accountRepository.save(account);
        return accountMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }
}
