package com.portfolio.banking.service.impl;

import com.portfolio.banking.dto.request.LoginRequest;
import com.portfolio.banking.dto.request.RegisterRequest;
import com.portfolio.banking.dto.response.AuthResponse;
import com.portfolio.banking.entity.*;
import com.portfolio.banking.exception.BadRequestException;
import com.portfolio.banking.exception.ResourceNotFoundException;
import com.portfolio.banking.repository.AccountRepository;
import com.portfolio.banking.repository.RoleRepository;
import com.portfolio.banking.repository.UserRepository;
import com.portfolio.banking.security.CustomUserDetails;
import com.portfolio.banking.security.JwtTokenProvider;
import com.portfolio.banking.service.AuthService;
import com.portfolio.banking.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Error: Email is already registered in the banking system.");
        }

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ROLE_CUSTOMER).build()));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(customerRole)
                .build();

        User savedUser = userRepository.save(user);

        // Automatically create an initial Savings Account for the new customer with 0 balance
        Account defaultAccount = Account.builder()
                .accountNumber(AccountNumberGenerator.generate())
                .balance(BigDecimal.ZERO)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .user(savedUser)
                .build();
        accountRepository.save(defaultAccount);

        log.info("Registered user id {} with account {}", savedUser.getId(), defaultAccount.getAccountNumber());

        String jwt = tokenProvider.generateTokenFromUsername(savedUser.getEmail());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().getName().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .build();
    }
}
