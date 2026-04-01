package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(accountRepository, portfolioItemRepository, priceSnapshotRepository);
    }

    @Test
    void getSummary_ComputesTotalsAndThreeAssetClasses() {
        Account account = new Account();
        account.setId(1L);
        account.setCashBalance(new BigDecimal("1000"));

        PortfolioItem stock = new PortfolioItem();
        stock.setId(10L);
        stock.setSymbol("AAPL");
        stock.setCompanyName("Apple");
        stock.setAssetType("STOCK");
        stock.setShares(new BigDecimal("10"));
        stock.setCostBasis(new BigDecimal("100"));
        stock.setCurrency("USD");

        PortfolioItem bond = new PortfolioItem();
        bond.setId(11L);
        bond.setSymbol("AGG");
        bond.setCompanyName("AGG");
        bond.setAssetType("BOND");
        bond.setShares(new BigDecimal("5"));
        bond.setCostBasis(new BigDecimal("200"));
        bond.setCurrency("USD");

        PriceSnapshot stockSnapshot = new PriceSnapshot();
        stockSnapshot.setSymbol("AAPL");
        stockSnapshot.setSnapshotDate(LocalDate.of(2026, 4, 1));
        stockSnapshot.setCurrentPrice(new BigDecimal("120"));

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(portfolioItemRepository.findByAccountId(1L)).thenReturn(List.of(stock, bond));
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("AAPL")).thenReturn(Optional.of(stockSnapshot));
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("AGG")).thenReturn(Optional.empty());

        DashboardSummaryResponse response = dashboardService.getSummary();

        assertEquals(new BigDecimal("2200.0000"), response.getTotalMarketValue());
        assertEquals(new BigDecimal("2000.0000"), response.getTotalCost());
        assertEquals(new BigDecimal("200.0000"), response.getTotalProfitLoss());
        assertEquals(new BigDecimal("3200.0000"), response.getTotalAssets());
        assertEquals(new BigDecimal("1200.0000"), response.getCategorySummary().get("stocks"));
        assertEquals(new BigDecimal("1000.0000"), response.getCategorySummary().get("bonds"));
        assertEquals(new BigDecimal("1000.0000"), response.getCategorySummary().get("cash"));
        assertEquals(1, response.getAttribution().getTopContributors().size());
        assertEquals("AAPL", response.getAttribution().getTopContributors().get(0).getSymbol());
        assertEquals(new BigDecimal("100.0000"), response.getAttribution().getTopContributors().get(0).getContributionPercent());
        assertEquals(0, response.getAttribution().getTopDetractors().size());
        assertEquals(3, response.getAttribution().getByAssetType().size());
    }

    @Test
    void getSummary_UnknownAssetTypeFallsBackToStocks() {
        Account account = new Account();
        account.setId(2L);
        account.setCashBalance(BigDecimal.ZERO);

        PortfolioItem unknown = new PortfolioItem();
        unknown.setId(20L);
        unknown.setSymbol("X1");
        unknown.setCompanyName("Unknown");
        unknown.setAssetType("CRYPTO");
        unknown.setShares(new BigDecimal("2"));
        unknown.setCostBasis(new BigDecimal("50"));
        unknown.setCurrency("USD");

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(portfolioItemRepository.findByAccountId(2L)).thenReturn(List.of(unknown));
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("X1")).thenReturn(Optional.empty());

        DashboardSummaryResponse response = dashboardService.getSummary();

        assertEquals(new BigDecimal("100.0000"), response.getCategorySummary().get("stocks"));
        assertEquals(new BigDecimal("0.0000"), response.getCategorySummary().get("bonds"));
        assertEquals(new BigDecimal("0.0000"), response.getCategorySummary().get("cash"));
        assertEquals("stocks", response.getAttribution().getByAssetType().get(0).getAssetType());
    }

    @Test
    void getSummary_ZeroBaseAllocationIsSafe() {
        Account account = new Account();
        account.setId(3L);
        account.setCashBalance(BigDecimal.ZERO);

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(portfolioItemRepository.findByAccountId(3L)).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.getSummary();

        assertEquals(new BigDecimal("0.0000"), response.getAllocation().get("stocks"));
        assertEquals(new BigDecimal("0.0000"), response.getAllocation().get("bonds"));
        assertEquals(new BigDecimal("0.0000"), response.getAllocation().get("cash"));
        assertEquals(0, response.getAttribution().getTopContributors().size());
        assertEquals(0, response.getAttribution().getTopDetractors().size());
        assertEquals(new BigDecimal("0.0000"), response.getAttribution().getByAssetType().get(0).getContributionPercent());
    }
}
