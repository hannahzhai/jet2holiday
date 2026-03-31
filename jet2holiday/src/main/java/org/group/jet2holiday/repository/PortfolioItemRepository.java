package org.group.jet2holiday.repository;

import java.util.List;
import org.group.jet2holiday.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    List<PortfolioItem> findByAccountId(Long accountId);
}
