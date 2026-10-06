package com.ghlove.point.repository;

import com.ghlove.point.domain.LocgovPointRate;
import com.ghlove.point.domain.LocgovPointRateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LocgovPointRateRepository extends JpaRepository<LocgovPointRate, LocgovPointRateId> {
    Optional<LocgovPointRate> findByStdrYearAndLocgovCode(String stdrYear, String locgovCode);

    /** admin 지자체관리 "포인트 지급률 변경이력" 팝업용 - 연도별 행 자체가 이력이다. */
    List<LocgovPointRate> findByLocgovCodeOrderByStdrYearDesc(String locgovCode);

    /**
     * admin 지자체관리(메뉴 4401) <b>목록</b>의 "포인트 지급률" 컬럼용 - 여러 지자체의 특정 연도
     * 지급률을 한 번에 읽는다. AS-IS는 목록 쿼리 안에서
     * {@code (SELECT point_rate FROM g_ctbny_setup WHERE locgov_code = ... AND stdr_year = 올해)}
     * 스칼라 서브쿼리로 붙였는데, TO-BE는 지급률이 point 소유라 admin이 한 번에 받아 채운다
     * (행마다 호출하면 N+1).
     */
    List<LocgovPointRate> findByStdrYearAndLocgovCodeIn(String stdrYear, Collection<String> locgovCodes);
}
