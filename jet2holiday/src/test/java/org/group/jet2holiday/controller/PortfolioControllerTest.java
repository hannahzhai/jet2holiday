package org.group.jet2holiday.controller;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.exception.ResourceNotFoundException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioControllerTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private PortfolioController portfolioController;

    private Account testAccount;
    private PortfolioItem testPortfolioItem;
    private PortfolioController.PortfolioItemRequest testRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAccount = new Account();
        testAccount.setId(1L);
        
        testPortfolioItem = new PortfolioItem();
        testPortfolioItem.setId(1L);
        testPortfolioItem.setAccount(testAccount);
        testPortfolioItem.setSymbol("AAPL");
        testPortfolioItem.setCompanyName("Apple Inc.");
        testPortfolioItem.setAssetType("STOCK");
        testPortfolioItem.setShares(BigDecimal.valueOf(10));
        testPortfolioItem.setCostBasis(BigDecimal.valueOf(150.00));
        testPortfolioItem.setCurrency("USD");
        
        testRequest = new PortfolioController.PortfolioItemRequest(
                "AAPL",
                "Apple Inc.",
                "STOCK",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(150.00),
                "USD"
        );
        
        // simulate getCurrentAccount() behavior
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(testAccount));
    }

    @Test
    void getPortfolioItems() {
        // simulate repository behavior
        when(portfolioItemRepository.findByAccountId(testAccount.getId()))
                .thenReturn(Collections.singletonList(testPortfolioItem));
        

        List<PortfolioController.PortfolioItemResponse> response = portfolioController.getPortfolioItems();

        // verify response
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testPortfolioItem.getSymbol(), response.get(0).symbol());
        
        // verify method call
        verify(portfolioItemRepository, times(1)).findByAccountId(testAccount.getId());
    }

    @Test
    void getPortfolioItem() {

        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testPortfolioItem));
        

        PortfolioController.PortfolioItemResponse response = portfolioController.getPortfolioItem(1L);
        
        // verify response
        assertNotNull(response);
        assertEquals(testPortfolioItem.getSymbol(), response.symbol());
        verify(portfolioItemRepository, times(1)).findById(1L);
    }

    @Test
    void getPortfolioItem_NotFound() {
        // 模拟 repository 行为
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> portfolioController.getPortfolioItem(1L));
        
        // 验证方法调用
        verify(portfolioItemRepository, times(1)).findById(1L);
    }

    @Test
    void createPortfolioItem() {

        // simulate repository behavior
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(testPortfolioItem);
        
        // 执行测试
        PortfolioController.PortfolioItemResponse response = portfolioController.createPortfolioItem(testRequest);
        
        // 验证结果
        assertNotNull(response);
        assertEquals(testRequest.symbol(), response.symbol());
        
        // 验证方法调用
        verify(portfolioItemRepository, times(1)).save(any(PortfolioItem.class));
    }

    @Test
    void updatePortfolioItem() {
        // 模拟 repository 行为
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testPortfolioItem));
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(testPortfolioItem);
        
        // 执行测试
        PortfolioController.PortfolioItemResponse response = portfolioController.updatePortfolioItem(1L, testRequest);
        
        // 验证结果
        assertNotNull(response);
        assertEquals(testRequest.symbol(), response.symbol());
        
        // 验证方法调用
        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, times(1)).save(any(PortfolioItem.class));
    }

    @Test
    void updatePortfolioItem_NotFound() {
        // 模拟 repository 行为
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> portfolioController.updatePortfolioItem(1L, testRequest));
        
        // 验证方法调用
        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, never()).save(any(PortfolioItem.class));
    }

    @Test
    void deletePortfolioItem() {
        // 模拟 repository 行为
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testPortfolioItem));
        doNothing().when(portfolioItemRepository).delete(testPortfolioItem);
        
        // 执行测试
        portfolioController.deletePortfolioItem(1L);
        
        // 验证方法调用
        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, times(1)).delete(testPortfolioItem);
    }

    @Test
    void deletePortfolioItem_NotFound() {
        // 模拟 repository 行为
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> portfolioController.deletePortfolioItem(1L));
        
        // 验证方法调用
        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, never()).delete(any(PortfolioItem.class));
    }
}