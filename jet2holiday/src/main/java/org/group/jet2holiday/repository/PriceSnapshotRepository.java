package org.group.jet2holiday.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {
    Optional<PriceSnapshot> findTopBySymbolOrderBySnapshotDateDesc(String symbol);

    Optional<PriceSnapshot> findBySymbolAndSnapshotDate(String symbol, LocalDate snapshotDate);
}
