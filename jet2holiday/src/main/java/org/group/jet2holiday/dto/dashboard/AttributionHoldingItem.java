package org.group.jet2holiday.dto.dashboard;

import java.math.BigDecimal;

public class AttributionHoldingItem {

    private String symbol;
    private String companyName;
    private String assetType;
    private BigDecimal marketValue;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercent;
    private BigDecimal contributionPercent;

    public AttributionHoldingItem() {
    }

    public AttributionHoldingItem(String symbol, String companyName, String assetType, BigDecimal marketValue,
                                  BigDecimal profitLoss, BigDecimal profitLossPercent, BigDecimal contributionPercent) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.assetType = assetType;
        this.marketValue = marketValue;
        this.profitLoss = profitLoss;
        this.profitLossPercent = profitLossPercent;
        this.contributionPercent = contributionPercent;
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

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public BigDecimal getProfitLossPercent() {
        return profitLossPercent;
    }

    public BigDecimal getContributionPercent() {
        return contributionPercent;
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

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public void setProfitLossPercent(BigDecimal profitLossPercent) {
        this.profitLossPercent = profitLossPercent;
    }

    public void setContributionPercent(BigDecimal contributionPercent) {
        this.contributionPercent = contributionPercent;
    }
}

