package org.group.jet2holiday.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.exception.ResourceNotFoundException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio-items")
public class PortfolioController {

    private final PortfolioItemRepository portfolioItemRepository;
    private final AccountRepository accountRepository;

    public PortfolioController(PortfolioItemRepository portfolioItemRepository,
                               AccountRepository accountRepository) {
        this.portfolioItemRepository = portfolioItemRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public List<PortfolioItemResponse> getPortfolioItems() {
        Account account = getCurrentAccount();
        return portfolioItemRepository.findByAccountId(account.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public PortfolioItemResponse getPortfolioItem(@PathVariable Long id) {
        Account account = getCurrentAccount();
        PortfolioItem item = portfolioItemRepository.findById(id)
                .filter(pi -> pi.getAccount().getId().equals(account.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found"));
        return toResponse(item);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioItemResponse createPortfolioItem(@Valid @RequestBody PortfolioItemRequest request) {
        Account account = getCurrentAccount();
        PortfolioItem item = new PortfolioItem();
        item.setAccount(account);
        item.setSymbol(request.symbol());
        item.setCompanyName(request.companyName());
        item.setAssetType(request.assetType());
        item.setShares(request.shares());
        item.setCostBasis(request.costBasis());
        item.setCurrency(request.currency());

        PortfolioItem saved = portfolioItemRepository.save(item);
        return toResponse(saved);
    }

    // default MVP assumption: use the first account seeded in the system
    private Account getCurrentAccount() {
        return accountRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> accountRepository.save(new Account()));
    }

    private PortfolioItemResponse toResponse(PortfolioItem item) {
        return new PortfolioItemResponse(
                item.getId(),
                item.getSymbol(),
                item.getCompanyName(),
                item.getAssetType(),
                item.getShares(),
                item.getCostBasis(),
                item.getCurrency()
        );
    }

    public record PortfolioItemRequest(
            @NotBlank String symbol,
            @NotBlank String companyName,
            @NotBlank String assetType,
            @NotNull @DecimalMin(value = "0.00000001", inclusive = false) BigDecimal shares,
            @NotNull @DecimalMin(value = "0.0001", inclusive = false) BigDecimal costBasis,
            @NotBlank String currency
    ) {
    }

    public record PortfolioItemResponse(
            Long id,
            String symbol,
            String companyName,
            String assetType,
            BigDecimal shares,
            BigDecimal costBasis,
            String currency
    ) {
    }
}
