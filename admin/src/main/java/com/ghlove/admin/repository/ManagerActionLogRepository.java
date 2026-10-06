package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ManagerActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerActionLogRepository extends JpaRepository<ManagerActionLog, Integer> {

    List<ManagerActionLog> findTop200ByOrderByActionLogIdDesc();

    /**
     * AS-IS getManagerActionLogListByParam - 로그인 상세화면이 그 로그인 시각부터 다음 로그인
     * 시각까지의 메뉴사용이력을 조회한다(srchStartCreated ~ srchEndCreated). 종료가 비어 있으면
     * (= 마지막 로그인) 이후 전체를 본다.
     */
    @Query("""
            select a from ManagerActionLog a
             where a.loginId = :loginId
               and (:startCreated is null or a.createdDate >= :startCreated)
               and (:endCreated is null or a.createdDate < :endCreated)
             order by a.actionLogId desc
            """)
    List<ManagerActionLog> searchByLoginAndPeriod(@Param("loginId") String loginId,
                                                  @Param("startCreated") String startCreated,
                                                  @Param("endCreated") String endCreated);
}
