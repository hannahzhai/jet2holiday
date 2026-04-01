package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.group.jet2holiday.dto.dashboard.PerformanceResponse;
import org.group.jet2holiday.dto.dashboard.PerformanceResponse.PerformancePoint;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;

    public DashboardService(
            AccountRepository accountRepository,
            PortfolioItemRepository portfolioItemRepository,
            PriceSnapshotRepository priceSnapshotRepository
    ) {
        this.accountRepository = accountRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public PerformanceResponse getPerformance(String range) {
        Account account = getCurrentAccount();
        List<PortfolioItem> holdings = portfolioItemRepository.findByAccountId(account.getId());

        if (holdings.isEmpty()) {
            return new PerformanceResponse(range, List.of());
        }

        // 计算日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDate(endDate, range);

        // 获取所有相关的价格快照
        List<PriceSnapshot> snapshots = new ArrayList<>();
        for (PortfolioItem holding : holdings) {
            List<PriceSnapshot> symbolSnapshots = priceSnapshotRepository.findBySymbolAndSnapshotDateBetween(
                    holding.getSymbol(), startDate, endDate);
            snapshots.addAll(symbolSnapshots);
        }

        // 按日期分组计算总收益
        Map<LocalDate, BigDecimal> dailyProfitLoss = calculateDailyProfitLoss(holdings, snapshots);

        // 转换为性能点列表
        List<PerformancePoint> points = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyProfitLoss.entrySet()) {
            points.add(new PerformancePoint(entry.getKey(), entry.getValue()));
        }

        // 按日期排序
        points.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));

        return new PerformanceResponse(range, points);
    }

    private LocalDate getStartDate(LocalDate endDate, String range) {
        return switch (range) {
            case "1W" -> endDate.minusWeeks(1);
            case "1M" -> endDate.minusMonths(1);
            case "3M" -> endDate.minusMonths(3);
            case "1Y" -> endDate.minusYears(1);
            default -> endDate.minusMonths(1);
        };
    }

    private Map<LocalDate, BigDecimal> calculateDailyProfitLoss(
            List<PortfolioItem> holdings, List<PriceSnapshot> snapshots) {
        Map<LocalDate, BigDecimal> dailyProfitLoss = new HashMap<>();

        // 按日期和符号分组快照
        Map<LocalDate, Map<String, BigDecimal>> dateSymbolPrices = new HashMap<>();
        for (PriceSnapshot snapshot : snapshots) {
            dateSymbolPrices
                    .computeIfAbsent(snapshot.getSnapshotDate(), k -> new HashMap<>())
                    .put(snapshot.getSymbol(), snapshot.getCurrentPrice());
        }

        // 计算每天的总收益
        for (Map.Entry<LocalDate, Map<String, BigDecimal>> entry : dateSymbolPrices.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, BigDecimal> prices = entry.getValue();
            BigDecimal totalPnL = BigDecimal.ZERO;

            for (PortfolioItem holding : holdings) {
                BigDecimal currentPrice = prices.get(holding.getSymbol());
                if (currentPrice != null) {
                    // 计算单个持仓的收益：(当前价格 - 成本价) × 股数
                    BigDecimal profitLoss = currentPrice
                            .subtract(holding.getCostBasis())
                            .multiply(holding.getShares());
                    totalPnL = totalPnL.add(profitLoss);
                }
            }

            dailyProfitLoss.put(date, totalPnL);
        }

        return dailyProfitLoss;
    }

    private Account getCurrentAccount() {
        return accountRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> accountRepository.save(new Account()));
    }
}
