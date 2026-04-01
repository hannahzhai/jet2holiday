package org.group.jet2holiday.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardSummaryResponse {

    private BigDecimal cashBalance;
    private BigDecimal totalAssets;
    private BigDecimal totalMarketValue;
    private BigDecimal totalCost;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercent;
    private Map<String, BigDecimal> allocation;
    private Map<String, BigDecimal> categorySummary;
    private List<DashboardSummaryItemResponse> items;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(BigDecimal cashBalance, BigDecimal totalAssets, BigDecimal totalMarketValue,
                                    BigDecimal totalCost, BigDecimal totalProfitLoss, BigDecimal totalProfitLossPercent,
                                    Map<String, BigDecimal> allocation, Map<String, BigDecimal> categorySummary,
                                    List<DashboardSummaryItemResponse> items) {
        this.cashBalance = cashBalance;
        this.totalAssets = totalAssets;
        this.totalMarketValue = totalMarketValue;
        this.totalCost = totalCost;
        this.totalProfitLoss = totalProfitLoss;
        this.totalProfitLossPercent = totalProfitLossPercent;
        this.allocation = allocation;
        this.categorySummary = categorySummary;
        this.items = items;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }

    public BigDecimal getTotalMarketValue() {
        return totalMarketValue;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public BigDecimal getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public BigDecimal getTotalProfitLossPercent() {
        return totalProfitLossPercent;
    }

    public Map<String, BigDecimal> getAllocation() {
        return allocation;
    }

    public Map<String, BigDecimal> getCategorySummary() {
        return categorySummary;
    }

    public List<DashboardSummaryItemResponse> getItems() {
        return items;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public void setTotalMarketValue(BigDecimal totalMarketValue) {
        this.totalMarketValue = totalMarketValue;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public void setTotalProfitLoss(BigDecimal totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }

    public void setTotalProfitLossPercent(BigDecimal totalProfitLossPercent) {
        this.totalProfitLossPercent = totalProfitLossPercent;
    }

    public void setAllocation(Map<String, BigDecimal> allocation) {
        this.allocation = allocation;
    }

    public void setCategorySummary(Map<String, BigDecimal> categorySummary) {
        this.categorySummary = categorySummary;
    }

    public void setItems(List<DashboardSummaryItemResponse> items) {
        this.items = items;
    }
}

