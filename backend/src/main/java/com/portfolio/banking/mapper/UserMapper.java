package com.portfolio.banking.mapper;

import com.portfolio.banking.dto.response.UserResponse;
import com.portfolio.banking.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final AccountMapper accountMapper;

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName().name() : null)
                .accounts(user.getAccounts() != null
                        ? user.getAccounts().stream().map(accountMapper::toResponse).collect(Collectors.toList())
                        : Collections.emptyList())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
