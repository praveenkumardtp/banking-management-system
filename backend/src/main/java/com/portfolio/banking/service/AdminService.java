package com.portfolio.banking.service;

import com.portfolio.banking.dto.response.AccountResponse;
import com.portfolio.banking.dto.response.AdminDashboardResponse;
import com.portfolio.banking.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    AdminDashboardResponse getDashboardStats();
    Page<UserResponse> searchCustomers(String query, Pageable pageable);
    List<AccountResponse> getAllAccounts();
    AccountResponse freezeAccount(String accountNumber);
    AccountResponse unfreezeAccount(String accountNumber);
}
