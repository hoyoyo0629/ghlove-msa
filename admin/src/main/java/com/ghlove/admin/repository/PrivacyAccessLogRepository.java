package com.ghlove.admin.repository;

import com.ghlove.admin.domain.PrivacyAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrivacyAccessLogRepository extends JpaRepository<PrivacyAccessLog, Long> {

    /** AS-IS {@code privacyAccessLogHistService.updatePrivacyAccessLog} - 사유/사유구분만 갱신. */
    @Modifying
    @Query("update PrivacyAccessLog p set p.reason = :reason, p.reasonType = :reasonType where p.id = :id")
    int updateReason(@Param("id") Long id, @Param("reason") String reason,
                     @Param("reasonType") String reasonType);
}
