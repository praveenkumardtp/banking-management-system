package com.portfolio.banking.service.impl;

import com.portfolio.banking.dto.response.AccountResponse;
import com.portfolio.banking.dto.response.AdminDashboardResponse;
import com.portfolio.banking.dto.response.UserResponse;
import com.portfolio.banking.entity.Account;
import com.portfolio.banking.entity.AccountStatus;
import com.portfolio.banking.exception.ResourceNotFoundException;
import com.portfolio.banking.mapper.AccountMapper;
import com.portfolio.banking.mapper.UserMapper;
import com.portfolio.banking.repository.AccountRepository;
import com.portfolio.banking.repository.TransactionRepository;
import com.portfolio.banking.repository.UserRepository;
import com.portfolio.banking.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByStatus(AccountStatus.ACTIVE);
        long frozenAccounts = accountRepository.countByStatus(AccountStatus.FROZEN);
        BigDecimal totalDeposited = accountRepository.sumTotalActiveBalance();
        BigDecimal totalVolume = transactionRepository.sumTotalTransactionVolume();

        return AdminDashboardResponse.builder()
                .totalCustomers(totalUsers)
                .totalAccounts(totalAccounts)
                .activeAccounts(activeAccounts)
                .frozenAccounts(frozenAccounts)
                .totalDepositedFunds(totalDeposited != null ? totalDeposited : BigDecimal.ZERO)
                .totalTransactionVolume(totalVolume != null ? totalVolume : BigDecimal.ZERO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchCustomers(String query, Pageable pageable) {
        String searchQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : "";
        return userRepository.searchUsers(searchQuery, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse freezeAccount(String accountNumber) {
        log.warn("ADMIN ACTION: Freezing account {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        account.setStatus(AccountStatus.FROZEN);
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse unfreezeAccount(String accountNumber) {
        log.info("ADMIN ACTION: Unfreezing account {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        account.setStatus(AccountStatus.ACTIVE);
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }
}
