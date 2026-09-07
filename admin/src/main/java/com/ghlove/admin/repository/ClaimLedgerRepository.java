package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ClaimLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClaimLedgerRepository extends JpaRepository<ClaimLedger, Long>, JpaSpecificationExecutor<ClaimLedger> {
}
