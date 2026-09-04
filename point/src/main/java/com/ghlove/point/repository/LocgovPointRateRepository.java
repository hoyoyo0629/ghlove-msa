package com.ghlove.point.repository;

import com.ghlove.point.domain.LocgovPointRate;
import com.ghlove.point.domain.LocgovPointRateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocgovPointRateRepository extends JpaRepository<LocgovPointRate, LocgovPointRateId> {
    Optional<LocgovPointRate> findByStdrYearAndLocgovCode(String stdrYear, String locgovCode);

    /** admin 지자체관리 "포인트 지급률 변경이력" 팝업용 - 연도별 행 자체가 이력이다. */
    List<LocgovPointRate> findByLocgovCodeOrderByStdrYearDesc(String locgovCode);
}
