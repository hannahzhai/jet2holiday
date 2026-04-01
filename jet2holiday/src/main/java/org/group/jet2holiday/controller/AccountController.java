package org.group.jet2holiday.controller;

import jakarta.validation.Valid;
import org.group.jet2holiday.dto.account.AccountResponse;
import org.group.jet2holiday.dto.account.UpdateCashBalanceRequest;
import org.group.jet2holiday.dto.account.UpdateCashBalanceResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.exception.ResourceNotFoundException;
import org.group.jet2holiday.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // GET /api/account/{id}
    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return toResponse(account);
    }

    // PUT /api/account/{id}/cash-balance
    @PutMapping("/{id}/cash-balance")
    public UpdateCashBalanceResponse updateBalance(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCashBalanceRequest request
    ) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        account.setCashBalance(request.getAmount());
        accountRepository.save(account);

        return new UpdateCashBalanceResponse(id, request.getAmount(), "Cash balance updated");
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountName(),
                account.getCashBalance(),
                account.getCurrency()
        );
    }
}