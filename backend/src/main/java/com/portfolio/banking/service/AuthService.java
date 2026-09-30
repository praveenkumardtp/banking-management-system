package com.portfolio.banking.service;

import com.portfolio.banking.dto.request.LoginRequest;
import com.portfolio.banking.dto.request.RegisterRequest;
import com.portfolio.banking.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
