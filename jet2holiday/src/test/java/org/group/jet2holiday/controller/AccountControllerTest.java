package org.group.jet2holiday.controller;

import java.math.BigDecimal;
import java.util.Optional;
import org.group.jet2holiday.dto.account.AccountResponse;
import org.group.jet2holiday.dto.account.UpdateCashBalanceRequest;
import org.group.jet2holiday.dto.account.UpdateCashBalanceResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.exception.ResourceNotFoundException;
import org.group.jet2holiday.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountController accountController;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountName("Default Portfolio Account");
        testAccount.setCashBalance(BigDecimal.valueOf(1000.00));
        testAccount.setCurrency("USD");
    }

    @Test
    void getAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        AccountResponse result = accountController.getAccount(1L);

        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getAccountName(), result.getAccountName());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccount_NotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountController.getAccount(1L));
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void updateBalance() {
        UpdateCashBalanceRequest request = new UpdateCashBalanceRequest();
        request.setAmount(new BigDecimal("2000.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        UpdateCashBalanceResponse response = accountController.updateBalance(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getAccountId());
        assertEquals(new BigDecimal("2000.00"), response.getCashBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void updateBalance_NotFound() {
        UpdateCashBalanceRequest request = new UpdateCashBalanceRequest();
        request.setAmount(new BigDecimal("2000.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountController.updateBalance(1L, request));

        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }
}