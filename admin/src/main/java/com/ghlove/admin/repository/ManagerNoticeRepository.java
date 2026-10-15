package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ManagerNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerNoticeRepository extends JpaRepository<ManagerNotice, Long> {

    /**
     * AS-IS managerrequest-mapper.getNoticeList - 사용중이고 지금이 게시기간 안이며 공통('A') 또는 요청한 종류인 공지.
     * {@code now}는 호출부가 AS-IS {@code DATE_FORMAT(now(), '%Y%m%d%H%m%s')} 그대로 만든 값이다.
     */
    @Query("""
            select n from ManagerNotice n
             where n.useYn = 'Y'
               and n.startDate <= :now
               and n.endDate >= :now
               and n.noticeType in :types
            """)
    List<ManagerNotice> findOpen(@Param("now") String now, @Param("types") List<String> types);
}
