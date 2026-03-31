package org.group.jet2holiday.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.group.jet2holiday.dto.portfolio.PortfolioItemRequest;
import org.group.jet2holiday.dto.portfolio.PortfolioItemResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.exception.ResourceNotFoundException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
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
    public PortfolioItemResponse getPortfolioItem(@PathVariable("id") Long id) {
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
        item.setSymbol(request.getSymbol());
        item.setCompanyName(request.getCompanyName());
        item.setAssetType(request.getAssetType());
        item.setShares(request.getShares());
        item.setCostBasis(request.getCostBasis());
        item.setCurrency(request.getCurrency());

        PortfolioItem saved = portfolioItemRepository.save(item);
        return toResponse(saved);
    }

    @PutMapping("/{id}")
    public PortfolioItemResponse updatePortfolioItem(
            @PathVariable("id") Long id,
            @Valid @RequestBody PortfolioItemRequest request
    ) {
        Account account = getCurrentAccount();
        PortfolioItem item = portfolioItemRepository.findById(id)
                .filter(pi -> pi.getAccount().getId().equals(account.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found with id: " + id));

        item.setSymbol(request.getSymbol());
        item.setCompanyName(request.getCompanyName());
        item.setAssetType(request.getAssetType());
        item.setShares(request.getShares());
        item.setCostBasis(request.getCostBasis());
        item.setCurrency(request.getCurrency());

        PortfolioItem updated = portfolioItemRepository.save(item);
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePortfolioItem(@PathVariable("id") Long id) {
        Account account = getCurrentAccount();
        PortfolioItem item = portfolioItemRepository.findById(id)
                .filter(pi -> pi.getAccount().getId().equals(account.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found with id: " + id));

        portfolioItemRepository.delete(item);
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
}