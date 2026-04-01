package org.group.jet2holiday.repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioItemRepositoryTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    public PortfolioItemRepositoryTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        // 准备测试数据
        Account account = new Account();
        account.setId(1L);

        PortfolioItem item = new PortfolioItem();
        item.setId(1L);
        item.setAccount(account);
        item.setSymbol("AAPL");
        item.setCompanyName("Apple Inc.");
        item.setAssetType("STOCK");
        item.setShares(BigDecimal.valueOf(10));
        item.setCostBasis(BigDecimal.valueOf(150.00));
        item.setCurrency("USD");

        // 模拟行为
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // 执行测试
        Optional<PortfolioItem> result = portfolioItemRepository.findById(1L);

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("AAPL", result.get().getSymbol());
        assertEquals("Apple Inc.", result.get().getCompanyName());

        // 验证方法调用
        verify(portfolioItemRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByAccountId() {
        // 准备测试数据
        Account account = new Account();
        account.setId(1L);

        PortfolioItem item1 = new PortfolioItem();
        item1.setId(1L);
        item1.setAccount(account);
        item1.setSymbol("AAPL");

        PortfolioItem item2 = new PortfolioItem();
        item2.setId(2L);
        item2.setAccount(account);
        item2.setSymbol("MSFT");

        List<PortfolioItem> expectedItems = List.of(item1, item2);

        // 模拟行为
        when(portfolioItemRepository.findByAccountId(1L)).thenReturn(expectedItems);

        // 执行测试
        List<PortfolioItem> result = portfolioItemRepository.findByAccountId(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("MSFT", result.get(1).getSymbol());

        // 验证方法调用
        verify(portfolioItemRepository, times(1)).findByAccountId(1L);
    }

    @Test
    void testSave() {
        // 准备测试数据
        Account account = new Account();
        account.setId(1L);

        PortfolioItem item = new PortfolioItem();
        item.setAccount(account);
        item.setSymbol("AAPL");
        item.setCompanyName("Apple Inc.");
        item.setAssetType("STOCK");
        item.setShares(BigDecimal.valueOf(10));
        item.setCostBasis(BigDecimal.valueOf(150.00));
        item.setCurrency("USD");

        PortfolioItem savedItem = new PortfolioItem();
        savedItem.setId(1L);
        savedItem.setAccount(account);
        savedItem.setSymbol("AAPL");
        savedItem.setCompanyName("Apple Inc.");
        savedItem.setAssetType("STOCK");
        savedItem.setShares(BigDecimal.valueOf(10));
        savedItem.setCostBasis(BigDecimal.valueOf(150.00));
        savedItem.setCurrency("USD");

        // 模拟行为
        when(portfolioItemRepository.save(item)).thenReturn(savedItem);

        // 执行测试
        PortfolioItem result = portfolioItemRepository.save(item);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AAPL", result.getSymbol());

        // 验证方法调用
        verify(portfolioItemRepository, times(1)).save(item);
    }

    @Test
    void testDelete() {
        // 准备测试数据
        PortfolioItem item = new PortfolioItem();
        item.setId(1L);

        // 模拟行为
        doNothing().when(portfolioItemRepository).delete(item);

        // 执行测试
        portfolioItemRepository.delete(item);

        // 验证方法调用
        verify(portfolioItemRepository, times(1)).delete(item);
    }
}