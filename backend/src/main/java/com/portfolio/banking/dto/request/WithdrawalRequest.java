package com.portfolio.banking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Withdrawal amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be strictly greater than 0.00")
    private BigDecimal amount;

    private String description;
}
