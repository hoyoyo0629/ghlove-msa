package com.ghlove.donation.repository;

import com.ghlove.donation.domain.CntrReceiptLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CntrReceiptLogRepository extends JpaRepository<CntrReceiptLog, Integer> {

    @Query("SELECT COALESCE(MAX(r.cntrOutptSn), 0) FROM CntrReceiptLog r")
    int maxCntrOutptSn();

    long countByCntrSn(String cntrSn);
}
