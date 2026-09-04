package com.ghlove.donation.repository;

import com.ghlove.donation.domain.CntrReqmng;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CntrReqmngRepository extends JpaRepository<CntrReqmng, Long> {
    List<CntrReqmng> findAllByOrderByFrstRegistPnttmDesc();

    List<CntrReqmng> findByLocgovCodeOrderByFrstRegistPnttmDesc(String locgovCode);

    List<CntrReqmng> findByCntrSnAndReqStatusCode(String cntrSn, String reqStatusCode);

    /** AS-IS list.jsp 검색조건(지자체/요청분류/승인여부/변경신청일자 범위) 재현 - 문자열
     *  파라미터가 null이면 해당 조건은 무시한다. startDate/endDate는 항상 non-null이어야
     *  한다(PostgreSQL JDBC가 "? IS NULL" 형태로 쓰인 timestamp 바인드 파라미터의 타입을
     *  추론하지 못해 "could not determine data type of parameter"로 실패하는 문제가 있어서,
     *  서비스 레이어에서 미지정 시 [1970-01-01, 먼 미래] sentinel 값으로 채워 넘긴다). */
    @Query("SELECT r FROM CntrReqmng r WHERE "
            + "(:locgovCode IS NULL OR r.locgovCode = :locgovCode) AND "
            + "(:cntrReqmngCode IS NULL OR r.cntrReqmngCode = :cntrReqmngCode) AND "
            + "(:reqStatusCode IS NULL OR r.reqStatusCode = :reqStatusCode) AND "
            + "r.frstRegistPnttm >= :startDate AND r.frstRegistPnttm < :endDate "
            + "ORDER BY r.frstRegistPnttm DESC")
    Page<CntrReqmng> search(@Param("locgovCode") String locgovCode,
                             @Param("cntrReqmngCode") String cntrReqmngCode,
                             @Param("reqStatusCode") String reqStatusCode,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate,
                             Pageable pageable);
}
