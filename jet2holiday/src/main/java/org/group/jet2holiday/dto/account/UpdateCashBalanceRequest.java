package org.group.jet2holiday.dto.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdateCashBalanceRequest {

    @NotNull
    @DecimalMin(value = "0.0000", inclusive = true)
    private BigDecimal amount;

    public UpdateCashBalanceRequest() {
    }

    public UpdateCashBalanceRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}