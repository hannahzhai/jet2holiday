package org.group.jet2holiday.controller;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // GET /api/account/{id}
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountRepository.findById(id).orElseThrow();
    }

    // PUT /api/account/{id}/cash-balance
    @PutMapping("/{id}/cash-balance")
    public Map<String, Object> updateBalance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        // 从 JSON body 读取 amount
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        Account account = accountRepository.findById(id)
                .orElseThrow();

        account.setCashBalance(amount);
        accountRepository.save(account);

        Map<String, Object> result = new HashMap<>();
        result.put("accountId", id);
        result.put("cashBalance", amount);
        result.put("message", "Cash balance updated");
        return result;
    }
}