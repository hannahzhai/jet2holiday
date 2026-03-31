package org.group.jet2holiday.dto.portfolio;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PortfolioItemRequest {

    @NotBlank
    private String symbol;

    @NotBlank
    private String companyName;

    @NotBlank
    private String assetType;

    @NotNull
    @DecimalMin(value = "0.00000001", inclusive = false)
    private BigDecimal shares;

    @NotNull
    @DecimalMin(value = "0.0001", inclusive = false)
    private BigDecimal costBasis;

    @NotBlank
    private String currency;

    public PortfolioItemRequest() {
    }

    public PortfolioItemRequest(String symbol, String companyName, String assetType, BigDecimal shares, BigDecimal costBasis, String currency) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.assetType = assetType;
        this.shares = shares;
        this.costBasis = costBasis;
        this.currency = currency;
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