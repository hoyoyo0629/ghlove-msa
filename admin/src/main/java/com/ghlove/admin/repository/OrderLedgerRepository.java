package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OrderLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrderLedgerRepository extends JpaRepository<OrderLedger, String>, JpaSpecificationExecutor<OrderLedger> {
    List<OrderLedger> findByStatus(String status);

    List<OrderLedger> findByStatusAndSettlementIdIsNull(String status);

    List<OrderLedger> findBySettlementId(Long settlementId);
}
