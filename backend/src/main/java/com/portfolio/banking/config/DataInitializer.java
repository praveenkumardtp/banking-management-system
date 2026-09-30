package com.portfolio.banking.config;

import com.portfolio.banking.entity.*;
import com.portfolio.banking.repository.AccountRepository;
import com.portfolio.banking.repository.RoleRepository;
import com.portfolio.banking.repository.TransactionRepository;
import com.portfolio.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Initializing banking roles...");
            Role adminRole = roleRepository.save(Role.builder().name(RoleType.ROLE_ADMIN).build());
            Role customerRole = roleRepository.save(Role.builder().name(RoleType.ROLE_CUSTOMER).build());

            log.info("Seeding default Administrator...");
            User admin = User.builder()
                    .fullName("Banking System Admin")
                    .email("admin@bank.com")
                    .password(passwordEncoder.encode("AdminPassword@123"))
                    .phone("+1-555-0100")
                    .role(adminRole)
                    .build();
            userRepository.save(admin);

            log.info("Seeding demo customer: customer@bank.com ...");
            User customer1 = User.builder()
                    .fullName("Praveen Kumar")
                    .email("customer@bank.com")
                    .password(passwordEncoder.encode("CustomerPassword@123"))
                    .phone("+1-555-0101")
                    .role(customerRole)
                    .build();
            userRepository.save(customer1);

            Account account1 = Account.builder()
                    .accountNumber("101000000001")
                    .balance(new BigDecimal("15000.00"))
                    .accountType(AccountType.CHECKING)
                    .status(AccountStatus.ACTIVE)
                    .user(customer1)
                    .build();
            accountRepository.save(account1);

            transactionRepository.save(Transaction.builder()
                    .referenceNumber("DEP-INIT-101")
                    .toAccount(account1)
                    .transactionType(TransactionType.DEPOSIT)
                    .amount(new BigDecimal("15000.00"))
                    .balanceAfter(new BigDecimal("15000.00"))
                    .status(TransactionStatus.SUCCESS)
                    .description("Initial Opening Balance Deposit")
                    .timestamp(LocalDateTime.now().minusDays(3))
                    .build());

            log.info("Seeding demo customer: alice@bank.com ...");
            User customer2 = User.builder()
                    .fullName("Alice Morgan")
                    .email("alice@bank.com")
                    .password(passwordEncoder.encode("CustomerPassword@123"))
                    .phone("+1-555-0102")
                    .role(customerRole)
                    .build();
            userRepository.save(customer2);

            Account account2 = Account.builder()
                    .accountNumber("101000000002")
                    .balance(new BigDecimal("8500.00"))
                    .accountType(AccountType.SAVINGS)
                    .status(AccountStatus.ACTIVE)
                    .user(customer2)
                    .build();
            accountRepository.save(account2);

            transactionRepository.save(Transaction.builder()
                    .referenceNumber("DEP-INIT-102")
                    .toAccount(account2)
                    .transactionType(TransactionType.DEPOSIT)
                    .amount(new BigDecimal("8500.00"))
                    .balanceAfter(new BigDecimal("8500.00"))
                    .status(TransactionStatus.SUCCESS)
                    .description("Opening Balance Deposit")
                    .timestamp(LocalDateTime.now().minusDays(2))
                    .build());

            log.info("Banking system demo seed data successfully populated.");
        }
    }
}
