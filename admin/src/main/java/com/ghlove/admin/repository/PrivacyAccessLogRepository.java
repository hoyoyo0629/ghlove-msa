package com.ghlove.admin.repository;

import com.ghlove.admin.domain.PrivacyAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrivacyAccessLogRepository extends JpaRepository<PrivacyAccessLog, Long> {

    /** AS-IS {@code exceldownloadLogMapper.getExcelAccessLogCheck} - 같은 IP·로그인유형·사용자·URL로 기간 안에 남긴 접근로그가 있는지. */
    boolean existsByIpAndLoginTypeAndUserIdAndUrlAndCreatedAtBetween(String ip, String loginType, Long userId, String url,
                                                                      java.time.LocalDateTime from, java.time.LocalDateTime to);

    /** AS-IS {@code privacyAccessLogHistService.updatePrivacyAccessLog} - 사유/사유구분만 갱신. */
    @Modifying
    @Query("update PrivacyAccessLog p set p.reason = :reason, p.reasonType = :reasonType where p.id = :id")
    int updateReason(@Param("id") Long id, @Param("reason") String reason,
                     @Param("reasonType") String reasonType);
}
