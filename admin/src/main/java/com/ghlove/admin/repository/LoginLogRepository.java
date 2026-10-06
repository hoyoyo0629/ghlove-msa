package com.ghlove.admin.repository;

import com.ghlove.admin.domain.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoginLogRepository extends JpaRepository<LoginLog, Integer> {

    List<LoginLog> findTop200ByOrderByLoginLogIdDesc();

    /** AS-IS getLoginLogListByParam - 접속일 범위(LOGIN_DATE 앞 8자리)로 걸러 최신순. */
    @Query("""
            select l from LoginLog l
             where (:startDate is null or substring(l.loginDate, 1, 8) >= :startDate)
               and (:endDate is null or substring(l.loginDate, 1, 8) <= :endDate)
             order by l.loginLogId desc
            """)
    List<LoginLog> searchByPeriod(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 상세화면이 "이 로그인 세션 동안의 메뉴사용이력"을 뽑을 때 쓰는 다음 로그인 시각 -
     * AS-IS getLoginLogDetailsDateInfo의 nextLoginDate다(같은 계정의 다음 로그인 로그).
     */
    @Query("""
            select min(l.loginDate) from LoginLog l
             where l.loginId = :loginId and l.loginDate > :loginDate
            """)
    Optional<String> findNextLoginDate(@Param("loginId") String loginId, @Param("loginDate") String loginDate);
}
