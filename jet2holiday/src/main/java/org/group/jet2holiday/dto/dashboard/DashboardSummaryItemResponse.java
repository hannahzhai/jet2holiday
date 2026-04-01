package org.group.jet2holiday.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DashboardSummaryItemResponse {

    private Long id;
    private String symbol;
    private String companyName;
    private String assetType;
    private BigDecimal shares;
    private BigDecimal costBasis;
    private String currency;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercent;
    private LocalDate snapshotDate;

    public DashboardSummaryItemResponse() {
    }

    public DashboardSummaryItemResponse(Long id, String symbol, String companyName, String assetType, BigDecimal shares,
                                        BigDecimal costBasis, String currency, BigDecimal currentPrice,
                                        BigDecimal marketValue, BigDecimal profitLoss, BigDecimal profitLossPercent,
                                        LocalDate snapshotDate) {
        this.id = id;
        this.symbol = symbol;
        this.companyName = companyName;
        this.assetType = assetType;
        this.shares = shares;
        this.costBasis = costBasis;
        this.currency = currency;
        this.currentPrice = currentPrice;
        this.marketValue = marketValue;
        this.profitLoss = profitLoss;
        this.profitLossPercent = profitLossPercent;
        this.snapshotDate = snapshotDate;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAssetType() {
        return assetType;
    }

    public BigDecimal getShares() {
        return shares;
    }

    public BigDecimal getCostBasis() {
        return costBasis;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public BigDecimal getProfitLossPercent() {
        return profitLossPercent;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public void setShares(BigDecimal shares) {
        this.shares = shares;
    }

    public void setCostBasis(BigDecimal costBasis) {
        this.costBasis = costBasis;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public void setProfitLossPercent(BigDecimal profitLossPercent) {
        this.profitLossPercent = profitLossPercent;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }
}

