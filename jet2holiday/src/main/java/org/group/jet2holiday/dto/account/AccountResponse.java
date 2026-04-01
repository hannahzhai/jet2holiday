package org.group.jet2holiday.dto.account;

import java.math.BigDecimal;

public class AccountResponse {

    private Long id;
    private String accountName;
    private BigDecimal cashBalance;
    private String currency;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String accountName, BigDecimal cashBalance, String currency) {
        this.id = id;
        this.accountName = accountName;
        this.cashBalance = cashBalance;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}