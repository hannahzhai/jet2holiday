package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.group.jet2holiday.client.LlmAdviceClient;
import org.group.jet2holiday.client.LlmAdviceResult;
import org.group.jet2holiday.dto.advice.AdviceGenerateRequest;
import org.group.jet2holiday.dto.advice.AdviceGenerateResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ExternalApiException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdviceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    @Mock
    private LlmAdviceClient llmAdviceClient;

    @InjectMocks
    private AdviceService adviceService;

    private Account account;
    private PortfolioItem item;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setCashBalance(new BigDecimal("1000"));

        item = new PortfolioItem();
        item.setId(10L);
        item.setAccount(account);
        item.setSymbol("AAPL");
        item.setCompanyName("Apple");
        item.setShares(new BigDecimal("5"));
        item.setCostBasis(new BigDecimal("150"));

        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setSymbol("AAPL");
        snapshot.setSnapshotDate(LocalDate.now());
        snapshot.setCurrentPrice(new BigDecimal("180"));

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(portfolioItemRepository.findByAccountId(1L)).thenReturn(List.of(item));
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("AAPL"))
                .thenReturn(Optional.of(snapshot));
        when(llmAdviceClient.modelName()).thenReturn("gpt-4o-mini");
    }

    @Test
    void generateAdvice_usesLlmResponseWhenAvailable() {
        when(llmAdviceClient.generateAdvice(anyString())).thenReturn(new LlmAdviceResult(
                "Portfolio is moderately aggressive.",
                "MEDIUM",
                List.of("Add one defensive ETF."),
                List.of("Watch concentration in tech.")
        ));

        AdviceGenerateResponse response = adviceService.generateAdvice(new AdviceGenerateRequest());

        assertEquals("Portfolio is moderately aggressive.", response.getSummary());
        assertEquals("MEDIUM", response.getRiskLevel());
        assertFalse(response.isFallbackUsed());
        assertEquals(1, response.getHoldings().size());
    }

    @Test
    void generateAdvice_fallsBackWhenLlmFails() {
        when(llmAdviceClient.generateAdvice(anyString()))
                .thenThrow(new ExternalApiException("provider down"));

        AdviceGenerateResponse response = adviceService.generateAdvice(new AdviceGenerateRequest());

        assertTrue(response.isFallbackUsed());
        assertFalse(response.getSuggestions().isEmpty());
        assertFalse(response.getRiskWarnings().isEmpty());
    }
}
