package com.portfolio.banking.controller;

import com.portfolio.banking.dto.response.AccountResponse;
import com.portfolio.banking.dto.response.AdminDashboardResponse;
import com.portfolio.banking.dto.response.TransactionResponse;
import com.portfolio.banking.dto.response.UserResponse;
import com.portfolio.banking.entity.TransactionType;
import com.portfolio.banking.service.AdminService;
import com.portfolio.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Endpoints for bank administrators: dashboard metrics, customer audit, account freeze/unfreeze")
public class AdminController {

    private final AdminService adminService;
    private final TransactionService transactionService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get banking platform metrics", description = "Calculates total deposits, accounts, user counts, and transaction volume")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboardResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    @Operation(summary = "Search customers", description = "Search customers by name or email with pagination")
    public ResponseEntity<Page<UserResponse>> getCustomers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> customers = adminService.searchCustomers(query, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/accounts")
    @Operation(summary = "View all banking accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = adminService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/accounts/{accountNumber}/freeze")
    @Operation(summary = "Freeze an account", description = "Freezes an account to block all debit, credit, and transfer activities")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber) {
        AccountResponse response = adminService.freezeAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/{accountNumber}/unfreeze")
    @Operation(summary = "Unfreeze an account", description = "Re-activates a frozen account for normal operations")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountNumber) {
        AccountResponse response = adminService.unfreezeAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Audit all system transactions", description = "Full audit trail of transactions across all accounts with search & filters")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> response = transactionService.getAllTransactions(type, startDate, endDate, search, pageable);
        return ResponseEntity.ok(response);
    }
}
