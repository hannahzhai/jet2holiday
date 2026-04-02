package org.group.jet2holiday.repository;

import org.group.jet2holiday.entity.MarketInstrument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketInstrumentRepository extends JpaRepository<MarketInstrument, Long> {
    Page<MarketInstrument> findByAssetTypeAndEnabledTrue(String assetType, Pageable pageable);
}
