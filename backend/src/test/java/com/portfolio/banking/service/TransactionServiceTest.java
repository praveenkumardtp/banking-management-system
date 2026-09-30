package com.portfolio.banking.service;

import com.portfolio.banking.dto.request.DepositRequest;
import com.portfolio.banking.dto.request.TransferRequest;
import com.portfolio.banking.dto.request.WithdrawalRequest;
import com.portfolio.banking.dto.response.TransactionResponse;
import com.portfolio.banking.entity.*;
import com.portfolio.banking.exception.AccountFrozenException;
import com.portfolio.banking.exception.InsufficientBalanceException;
import com.portfolio.banking.exception.InvalidTransactionException;
import com.portfolio.banking.mapper.TransactionMapper;
import com.portfolio.banking.repository.AccountRepository;
import com.portfolio.banking.repository.TransactionRepository;
import com.portfolio.banking.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account activeAccount;
    private Account destinationAccount;
    private Account frozenAccount;

    @BeforeEach
    void setUp() {
        activeAccount = Account.builder()
                .id(1L)
                .accountNumber("101000000001")
                .balance(new BigDecimal("1000.00"))
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .build();

        destinationAccount = Account.builder()
                .id(2L)
                .accountNumber("101000000002")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .build();

        frozenAccount = Account.builder()
                .id(3L)
                .accountNumber("101000000003")
                .balance(new BigDecimal("2000.00"))
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.FROZEN)
                .build();
    }

    @Test
    @DisplayName("Deposit: Successfully increases balance and creates transaction record")
    void testDeposit_Success() {
        when(accountRepository.findByAccountNumber("101000000001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(100L);
            return tx;
        });

        DepositRequest request = DepositRequest.builder()
                .accountNumber("101000000001")
                .amount(new BigDecimal("250.00"))
                .description("ATM Deposit")
                .build();

        TransactionResponse response = transactionService.deposit(request);

        assertNotNull(response);
        assertEquals(TransactionType.DEPOSIT, response.getTransactionType());
        assertEquals(new BigDecimal("250.00"), response.getAmount());
        assertEquals(new BigDecimal("1250.00"), response.getBalanceAfter());
        assertEquals(new BigDecimal("1250.00"), activeAccount.getBalance());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());

        verify(accountRepository, times(1)).save(activeAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deposit: Invalid amount <= 0 throws InvalidTransactionException")
    void testDeposit_InvalidAmount() {
        DepositRequest request = DepositRequest.builder()
                .accountNumber("101000000001")
                .amount(new BigDecimal("0.00"))
                .build();

        assertThrows(InvalidTransactionException.class, () -> transactionService.deposit(request));
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Withdrawal: Successfully decreases balance and records transaction")
    void testWithdrawal_Success() {
        when(accountRepository.findByAccountNumber("101000000001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(101L);
            return tx;
        });

        WithdrawalRequest request = WithdrawalRequest.builder()
                .accountNumber("101000000001")
                .amount(new BigDecimal("300.00"))
                .description("Counter Withdrawal")
                .build();

        TransactionResponse response = transactionService.withdraw(request);

        assertNotNull(response);
        assertEquals(TransactionType.WITHDRAWAL, response.getTransactionType());
        assertEquals(new BigDecimal("300.00"), response.getAmount());
        assertEquals(new BigDecimal("700.00"), response.getBalanceAfter());
        assertEquals(new BigDecimal("700.00"), activeAccount.getBalance());

        verify(accountRepository, times(1)).save(activeAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Withdrawal: Insufficient balance throws InsufficientBalanceException and prevents overdraft")
    void testWithdrawal_InsufficientBalance() {
        when(accountRepository.findByAccountNumber("101000000001")).thenReturn(Optional.of(activeAccount));

        WithdrawalRequest request = WithdrawalRequest.builder()
                .accountNumber("101000000001")
                .amount(new BigDecimal("1500.00")) // Exceeds balance of 1000.00
                .build();

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.withdraw(request)
        );

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        assertEquals(new BigDecimal("1000.00"), activeAccount.getBalance()); // untouched
        verify(accountRepository, never()).save(activeAccount);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Withdrawal: Frozen account throws AccountFrozenException")
    void testWithdrawal_FrozenAccount() {
        when(accountRepository.findByAccountNumber("101000000003")).thenReturn(Optional.of(frozenAccount));

        WithdrawalRequest request = WithdrawalRequest.builder()
                .accountNumber("101000000003")
                .amount(new BigDecimal("100.00"))
                .build();

        assertThrows(AccountFrozenException.class, () -> transactionService.withdraw(request));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Successfully debits sender, credits receiver, and records transaction")
    void testTransfer_Success() {
        when(accountRepository.findByAccountNumber("101000000001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("101000000002")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(102L);
            return tx;
        });

        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("101000000001")
                .toAccountNumber("101000000002")
                .amount(new BigDecimal("400.00"))
                .description("Rent Payment")
                .build();

        TransactionResponse response = transactionService.transfer(request);

        assertNotNull(response);
        assertEquals(TransactionType.TRANSFER, response.getTransactionType());
        assertEquals(new BigDecimal("400.00"), response.getAmount());
        assertEquals(new BigDecimal("600.00"), response.getBalanceAfter()); // fromAccount balance
        assertEquals(new BigDecimal("600.00"), activeAccount.getBalance());
        assertEquals(new BigDecimal("900.00"), destinationAccount.getBalance()); // 500 + 400

        verify(accountRepository, times(1)).save(activeAccount);
        verify(accountRepository, times(1)).save(destinationAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Transfer: Insufficient balance on source throws InsufficientBalanceException")
    void testTransfer_InsufficientBalance() {
        when(accountRepository.findByAccountNumber("101000000001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("101000000002")).thenReturn(Optional.of(destinationAccount));

        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("101000000001")
                .toAccountNumber("101000000002")
                .amount(new BigDecimal("2000.00"))
                .build();

        assertThrows(InsufficientBalanceException.class, () -> transactionService.transfer(request));
        assertEquals(new BigDecimal("1000.00"), activeAccount.getBalance());
        assertEquals(new BigDecimal("500.00"), destinationAccount.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: To same account number throws InvalidTransactionException")
    void testTransfer_SameAccount_Fails() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("101000000001")
                .toAccountNumber("101000000001")
                .amount(new BigDecimal("100.00"))
                .build();

        assertThrows(InvalidTransactionException.class, () -> transactionService.transfer(request));
        verify(accountRepository, never()).save(any());
    }
}
