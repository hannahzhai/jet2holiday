package org.group.jet2holiday.controller;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.group.jet2holiday.dto.portfolio.PortfolioItemRequest;
import org.group.jet2holiday.dto.portfolio.PortfolioItemResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortfolioControllerTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private PortfolioController portfolioController;

    private Account testAccount;
    private PortfolioItem testPortfolioItem;
    private PortfolioItemRequest testRequest;

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

        testRequest = new PortfolioItemRequest(
                "AAPL",
                "Apple Inc.",
                "STOCK",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(150.00),
                "USD"
        );

        when(accountRepository.findAll()).thenReturn(Collections.singletonList(testAccount));
    }

    @Test
    void getPortfolioItems() {
        when(portfolioItemRepository.findByAccountId(testAccount.getId()))
                .thenReturn(Collections.singletonList(testPortfolioItem));

        List<PortfolioItemResponse> response = portfolioController.getPortfolioItems();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testPortfolioItem.getSymbol(), response.get(0).getSymbol());
        verify(portfolioItemRepository, times(1)).findByAccountId(testAccount.getId());
    }

    @Test
    void getPortfolioItem() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testPortfolioItem));

        PortfolioItemResponse response = portfolioController.getPortfolioItem(1L);

        assertNotNull(response);
        assertEquals(testPortfolioItem.getSymbol(), response.getSymbol());
        verify(portfolioItemRepository, times(1)).findById(1L);
    }

    @Test
    void getPortfolioItem_NotFound() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> portfolioController.getPortfolioItem(1L));
        verify(portfolioItemRepository, times(1)).findById(1L);
    }

    @Test
    void createPortfolioItem() {
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(testPortfolioItem);

        PortfolioItemResponse response = portfolioController.createPortfolioItem(testRequest);

        assertNotNull(response);
        assertEquals(testRequest.getSymbol(), response.getSymbol());
        verify(portfolioItemRepository, times(1)).save(any(PortfolioItem.class));
    }

    @Test
    void updatePortfolioItem() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testPortfolioItem));
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(testPortfolioItem);

        PortfolioItemResponse response = portfolioController.updatePortfolioItem(1L, testRequest);

        assertNotNull(response);
        assertEquals(testRequest.getSymbol(), response.getSymbol());
        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, times(1)).save(any(PortfolioItem.class));
    }

    @Test
    void updatePortfolioItem_NotFound() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> portfolioController.updatePortfolioItem(1L, testRequest));

        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, never()).save(any(PortfolioItem.class));
    }

    @Test
    void deletePortfolioItem() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testPortfolioItem));
        doNothing().when(portfolioItemRepository).delete(testPortfolioItem);

        portfolioController.deletePortfolioItem(1L);

        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, times(1)).delete(testPortfolioItem);
    }

    @Test
    void deletePortfolioItem_NotFound() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> portfolioController.deletePortfolioItem(1L));

        verify(portfolioItemRepository, times(1)).findById(1L);
        verify(portfolioItemRepository, never()).delete(any(PortfolioItem.class));
    }
}