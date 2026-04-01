package org.group.jet2holiday.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_item", uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_symbol", columnNames = {"account_id", "symbol"})
})
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_portfolio_item_account"))
    private Account account;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "asset_type", nullable = false, length = 20)
    private String assetType;

    @Column(name = "shares", nullable = false, precision = 19, scale = 8)
    private BigDecimal shares;

    @Column(name = "cost_basis", nullable = false, precision = 19, scale = 4)
    private BigDecimal costBasis;

    @Column(name = "currency", nullable = true, length = 10)
    private String currency = "USD";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public PortfolioItem() {
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccount(Account account) {
        this.account = account;
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
