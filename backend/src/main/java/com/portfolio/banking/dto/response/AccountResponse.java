package com.portfolio.banking.dto.response;

import com.portfolio.banking.entity.AccountStatus;
import com.portfolio.banking.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;
    private AccountStatus status;
    private Long userId;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
}
