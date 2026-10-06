package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OpEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpEmailRepository extends JpaRepository<OpEmail, Long> {

    List<OpEmail> findAllByOrderByEmailIdDesc();

    /**
     * AS-IS {@code emailMapper.getEmailList} / {@code getEmailListCount}의 WHERE(sqlEmailWhere) 이식
     * - 제목/내용 LIKE + SEND_DATE 범위다.
     *
     * <b>AS-IS 결함 2건은 사용자 확인 후 고쳤다(2026-10-03)</b>: ① AS-IS는 {@code searchType}이
     * 'S'/'C'일 때만 LIKE를 거는데 화면 select가 보내는 값은 SUBJECT/CONTENT라 어느 분기도 타지
     * 않아 검색어가 무효였고, ② 'C'(내용) 분기마저 CONTENT가 아니라 SUBJECT를 LIKE했다.
     * 화면이 보내는 값 그대로 제목은 SUBJECT를, 내용은 CONTENT를 보도록 바로잡았다.
     *
     * SEND_DATE는 yyyyMMddHHmmss 문자열이고 화면이 주는 날짜는 yyyyMMdd라 문자열로 비교한다
     * (AS-IS 동일) - 종료일을 같은 날로 주면 그 날 발송분은 '20260101120000' > '20260101'이라
     * 빠지는 것도 AS-IS 그대로다.
     */
    /* cast(... as String)는 필수다 - 이유는 PopupRepository.search 주석 참고. */
    @Query("""
            select e from OpEmail e
             where (cast(:subjectKeyword as String) is null
                     or e.subject like concat('%', cast(:subjectKeyword as String), '%'))
               and (cast(:contentKeyword as String) is null
                     or e.content like concat('%', cast(:contentKeyword as String), '%'))
               and (:startDate is null or e.sendDate >= :startDate)
               and (:endDate is null or e.sendDate <= :endDate)
             order by e.emailId desc
            """)
    List<OpEmail> search(@Param("subjectKeyword") String subjectKeyword,
                         @Param("contentKeyword") String contentKeyword,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);
}
