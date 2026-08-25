package com.ghlove.donation.repository;

import com.ghlove.donation.domain.CntrReqmng;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CntrReqmngRepository extends JpaRepository<CntrReqmng, Long> {
    List<CntrReqmng> findAllByOrderByFrstRegistPnttmDesc();

    List<CntrReqmng> findByLocgovCodeOrderByFrstRegistPnttmDesc(String locgovCode);

    List<CntrReqmng> findByCntrSnAndReqStatusCode(String cntrSn, String reqStatusCode);
}
