package org.group.jet2holiday.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountController accountController;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化测试数据
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountName("Default Portfolio Account");
        testAccount.setCashBalance(BigDecimal.valueOf(1000.00));
        testAccount.setCurrency("USD");
    }

    @Test
    void getAccount() {
        // 模拟 repository 行为
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        
        // 执行测试
        Account result = accountController.getAccount(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getAccountName(), result.getAccountName());
        
        // 验证方法调用
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccount_NotFound() {
        // 模拟 repository 行为
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> accountController.getAccount(1L));
        
        // 验证方法调用
        verify(accountRepository, times(1)).findById(1L);
    }


    @Test
    void updateBalance_NotFound() {
        // 准备请求数据
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", "2000.00");
        
        // 模拟 repository 行为
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> accountController.updateBalance(1L, requestBody));
        
        // 验证方法调用
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }
}