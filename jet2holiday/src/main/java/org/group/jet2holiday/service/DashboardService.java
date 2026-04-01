package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.group.jet2holiday.dto.dashboard.PerformanceResponse;
import org.group.jet2holiday.dto.dashboard.PerformanceResponse.PerformancePoint;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryItemResponse;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryResponse;
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

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;



    public DashboardService(AccountRepository accountRepository,
                PortfolioItemRepository portfolioItemRepository,
                PriceSnapshotRepository priceSnapshotRepository) {
            this.accountRepository = accountRepository;
            this.portfolioItemRepository = portfolioItemRepository;
            this.priceSnapshotRepository = priceSnapshotRepository;
        }
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        Account account = getCurrentAccount();
        List<PortfolioItem> holdings = portfolioItemRepository.findByAccountId(account.getId());

        BigDecimal cashBalance = safe(account.getCashBalance());
        List<DashboardSummaryItemResponse> items = new ArrayList<>();

        BigDecimal stockMarketValue = BigDecimal.ZERO;
        BigDecimal bondMarketValue = BigDecimal.ZERO;
        BigDecimal cashAssetValue = BigDecimal.ZERO;
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (PortfolioItem item : holdings) {
            BigDecimal shares = safe(item.getShares());
            BigDecimal costBasis = safe(item.getCostBasis());

            PriceSnapshot latestSnapshot = priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc(item.getSymbol())
                    .orElse(null);
            BigDecimal currentPrice = latestSnapshot == null ? costBasis : safe(latestSnapshot.getCurrentPrice());
            BigDecimal marketValue = shares.multiply(currentPrice);
            BigDecimal holdingCost = shares.multiply(costBasis);
            BigDecimal profitLoss = marketValue.subtract(holdingCost);
            BigDecimal profitLossPercent = holdingCost.compareTo(BigDecimal.ZERO) > 0
                    ? profitLoss.divide(holdingCost, 6, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                    : BigDecimal.ZERO;

            totalMarketValue = totalMarketValue.add(marketValue);
            totalCost = totalCost.add(holdingCost);

            String bucket = resolveAssetBucket(item.getAssetType());
            if ("bonds".equals(bucket)) {
                bondMarketValue = bondMarketValue.add(marketValue);
            } else if ("cash".equals(bucket)) {
                cashAssetValue = cashAssetValue.add(marketValue);
            } else {
                stockMarketValue = stockMarketValue.add(marketValue);
            }

            items.add(new DashboardSummaryItemResponse(
                    item.getId(),
                    item.getSymbol(),
                    item.getCompanyName(),
                    item.getAssetType(),
                    shares.setScale(4, RoundingMode.HALF_UP),
                    costBasis.setScale(4, RoundingMode.HALF_UP),
                    item.getCurrency(),
                    currentPrice.setScale(4, RoundingMode.HALF_UP),
                    marketValue.setScale(4, RoundingMode.HALF_UP),
                    profitLoss.setScale(4, RoundingMode.HALF_UP),
                    profitLossPercent.setScale(4, RoundingMode.HALF_UP),
                    latestSnapshot == null ? null : latestSnapshot.getSnapshotDate()
            ));
        }

        BigDecimal categoryCash = cashBalance.add(cashAssetValue);
        BigDecimal totalAssets = cashBalance.add(totalMarketValue);
        BigDecimal totalProfitLoss = totalMarketValue.subtract(totalCost);
        BigDecimal totalProfitLossPercent = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? totalProfitLoss.divide(totalCost, 6, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                : BigDecimal.ZERO;

        Map<String, BigDecimal> categorySummary = new LinkedHashMap<>();
        categorySummary.put("stocks", stockMarketValue.setScale(4, RoundingMode.HALF_UP));
        categorySummary.put("bonds", bondMarketValue.setScale(4, RoundingMode.HALF_UP));
        categorySummary.put("cash", categoryCash.setScale(4, RoundingMode.HALF_UP));

        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        allocation.put("stocks", toAllocationPct(stockMarketValue, totalAssets));
        allocation.put("bonds", toAllocationPct(bondMarketValue, totalAssets));
        allocation.put("cash", toAllocationPct(categoryCash, totalAssets));

        return new DashboardSummaryResponse(
                cashBalance.setScale(4, RoundingMode.HALF_UP),
                totalAssets.setScale(4, RoundingMode.HALF_UP),
                totalMarketValue.setScale(4, RoundingMode.HALF_UP),
                totalCost.setScale(4, RoundingMode.HALF_UP),
                totalProfitLoss.setScale(4, RoundingMode.HALF_UP),
                totalProfitLossPercent.setScale(4, RoundingMode.HALF_UP),
                allocation,
                categorySummary,
                items
        );
    }

    private BigDecimal toAllocationPct(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return part.divide(total, 6, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private String resolveAssetBucket(String assetType) {
        if (assetType == null) {
            return "stocks";
        }
        String normalized = assetType.trim().toUpperCase();
        if ("BOND".equals(normalized) || "BONDS".equals(normalized)) {
            return "bonds";
        }
        if ("CASH".equals(normalized)) {
            return "cash";
        }
        return "stocks";
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Account getCurrentAccount() {
        return accountRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> accountRepository.save(new Account()));
    }

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


}

