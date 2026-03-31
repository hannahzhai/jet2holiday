package org.group.jet2holiday.dto.account;

import java.math.BigDecimal;

public class UpdateCashBalanceResponse {

    private Long accountId;
    private BigDecimal cashBalance;
    private String message;

    public UpdateCashBalanceResponse() {
    }

    public UpdateCashBalanceResponse(Long accountId, BigDecimal cashBalance, String message) {
        this.accountId = accountId;
        this.cashBalance = cashBalance;
        this.message = message;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}