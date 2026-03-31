package org.group.jet2holiday.dto.portfolio;

import java.math.BigDecimal;

public class PortfolioItemResponse {

    private Long id;
    private String symbol;
    private String companyName;
    private String assetType;
    private BigDecimal shares;
    private BigDecimal costBasis;
    private String currency;

    public PortfolioItemResponse() {
    }

    public PortfolioItemResponse(Long id, String symbol, String companyName, String assetType, BigDecimal shares, BigDecimal costBasis, String currency) {
        this.id = id;
        this.symbol = symbol;
        this.companyName = companyName;
        this.assetType = assetType;
        this.shares = shares;
        this.costBasis = costBasis;
        this.currency = currency;
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
}