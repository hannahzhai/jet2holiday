package org.group.jet2holiday.dto.advice;

import java.math.BigDecimal;

public class HoldingAdviceSnapshot {

    private String symbol;
    private String companyName;
    private BigDecimal shares;
    private BigDecimal costBasis;
    private BigDecimal latestPrice;
    private BigDecimal marketValue;
    private BigDecimal allocationPct;
    private BigDecimal unrealizedPnLPct;

    public HoldingAdviceSnapshot() {
    }

    public HoldingAdviceSnapshot(String symbol, String companyName, BigDecimal shares, BigDecimal costBasis,
                                 BigDecimal latestPrice, BigDecimal marketValue, BigDecimal allocationPct,
                                 BigDecimal unrealizedPnLPct) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.shares = shares;
        this.costBasis = costBasis;
        this.latestPrice = latestPrice;
        this.marketValue = marketValue;
        this.allocationPct = allocationPct;
        this.unrealizedPnLPct = unrealizedPnLPct;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public BigDecimal getShares() {
        return shares;
    }

    public BigDecimal getCostBasis() {
        return costBasis;
    }

    public BigDecimal getLatestPrice() {
        return latestPrice;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public BigDecimal getAllocationPct() {
        return allocationPct;
    }

    public BigDecimal getUnrealizedPnLPct() {
        return unrealizedPnLPct;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setShares(BigDecimal shares) {
        this.shares = shares;
    }

    public void setCostBasis(BigDecimal costBasis) {
        this.costBasis = costBasis;
    }

    public void setLatestPrice(BigDecimal latestPrice) {
        this.latestPrice = latestPrice;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public void setAllocationPct(BigDecimal allocationPct) {
        this.allocationPct = allocationPct;
    }

    public void setUnrealizedPnLPct(BigDecimal unrealizedPnLPct) {
        this.unrealizedPnLPct = unrealizedPnLPct;
    }
}

