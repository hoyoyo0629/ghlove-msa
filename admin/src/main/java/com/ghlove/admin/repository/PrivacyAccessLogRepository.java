package com.ghlove.admin.repository;

import com.ghlove.admin.domain.PrivacyAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PrivacyAccessLogRepository extends JpaRepository<PrivacyAccessLog, Long> {

    /**
     * AS-IS {@code exceldownload-log-mapper.getExceldownloadLogListByParam}의 안쪽 쿼리 -
     * <b>{@code TASK = '엑셀 다운로드'}인 행만</b> 보고, 행안부/시스템 담당자가 아니면
     * {@code MANAGER_ID = <본인>}으로 더 좁힌다(AS-IS adminUserId 분기). 등록일 범위는 AS-IS가
     * {@code CREATED_AT >= CONCAT(시작일,'000000')} / {@code <= CONCAT(종료일,'235959')}로 비교하는데,
     * TO-BE는 컬럼이 timestamp라 같은 뜻의 경계값으로 비교한다. 정렬은 AS-IS와 같이 등록일 역순.
     */
    @Query("""
            select p from PrivacyAccessLog p
             where p.task = :task
               and (:managerId is null or p.managerId = :managerId)
               and (:from is null or p.createdAt >= :from)
               and (:to is null or p.createdAt <= :to)
             order by p.createdAt desc, p.id desc
            """)
    List<PrivacyAccessLog> searchExcelDownloadLogs(@Param("task") String task,
                                                   @Param("managerId") Long managerId,
                                                   @Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);

    /** AS-IS {@code privacyAccessLogHistService.updatePrivacyAccessLog} - 사유/사유구분만 갱신. */
    @Modifying
    @Query("update PrivacyAccessLog p set p.reason = :reason, p.reasonType = :reasonType where p.id = :id")
    int updateReason(@Param("id") Long id, @Param("reason") String reason,
                     @Param("reasonType") String reasonType);
}
