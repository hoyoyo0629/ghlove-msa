package com.ghlove.admin.repository;

import com.ghlove.admin.domain.PointLedgerStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLedgerStatRepository extends JpaRepository<PointLedgerStat, Long> {
}
