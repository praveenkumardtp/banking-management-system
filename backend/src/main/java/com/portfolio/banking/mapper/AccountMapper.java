package com.portfolio.banking.mapper;

import com.portfolio.banking.dto.response.AccountResponse;
import com.portfolio.banking.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        if (account == null) return null;

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .userId(account.getUser() != null ? account.getUser().getId() : null)
                .ownerName(account.getUser() != null ? account.getUser().getFullName() : null)
                .ownerEmail(account.getUser() != null ? account.getUser().getEmail() : null)
                .createdAt(account.getCreatedAt())
                .build();
    }
}
