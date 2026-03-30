package org.group.jet2holiday.repository;

import org.group.jet2holiday.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {
}
