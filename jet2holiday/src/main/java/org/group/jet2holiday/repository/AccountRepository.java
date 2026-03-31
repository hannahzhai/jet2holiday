package org.group.jet2holiday.repository;

import org.group.jet2holiday.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}