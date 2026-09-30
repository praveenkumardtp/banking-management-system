package com.portfolio.banking.controller;

import com.portfolio.banking.dto.request.CreateAccountRequest;
import com.portfolio.banking.dto.request.DepositRequest;
import com.portfolio.banking.dto.request.TransferRequest;
import com.portfolio.banking.dto.request.WithdrawalRequest;
import com.portfolio.banking.dto.response.AccountResponse;
import com.portfolio.banking.dto.response.TransactionResponse;
import com.portfolio.banking.dto.response.UserResponse;
import com.portfolio.banking.entity.TransactionType;
import com.portfolio.banking.security.CustomUserDetails;
import com.portfolio.banking.service.AccountService;
import com.portfolio.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Tag(name = "Customer Operations", description = "Endpoints for bank customers: balance, deposits, withdrawals, transfers, history")
public class CustomerController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/profile")
    @Operation(summary = "Get current customer profile", description = "Returns customer details and linked bank accounts")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = accountService.getCurrentUserProfile(userDetails.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts")
    @Operation(summary = "List customer accounts", description = "Retrieves all bank accounts owned by the authenticated customer")
    public ResponseEntity<List<AccountResponse>> getAccounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userDetails.getId());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{accountNumber}")
    @Operation(summary = "Get account details by account number")
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    @Operation(summary = "Check live balance of an account")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "balance", balance
        ));
    }

    @PostMapping("/accounts")
    @Operation(summary = "Open an additional bank account", description = "Allows customer to open another savings or checking account")
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse account = accountService.createAccount(userDetails.getId(), request);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @PostMapping("/transactions/deposit")
    @Operation(summary = "Deposit funds into account", description = "Validates amount and adds funds with database transaction")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/withdraw")
    @Operation(summary = "Withdraw funds from account", description = "Checks sufficient balance, prevents negative balances, records transaction")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        TransactionResponse response = transactionService.withdraw(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/transfer")
    @Operation(summary = "Transfer funds between accounts", description = "ACID transaction transferring money with balance & frozen account checks")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountNumber}/transactions")
    @Operation(summary = "Get and filter transaction history", description = "Filter transactions by date range, transaction type, and keyword search")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = transactionService.getAccountTransactions(
                accountNumber, type, startDate, endDate, search, pageable
        );
        return ResponseEntity.ok(transactions);
    }
}
