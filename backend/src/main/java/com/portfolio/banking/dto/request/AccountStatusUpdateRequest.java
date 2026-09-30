package com.portfolio.banking.dto.request;

import com.portfolio.banking.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusUpdateRequest {

    @NotNull(message = "Account status is required (ACTIVE, FROZEN, CLOSED)")
    private AccountStatus status;
}
