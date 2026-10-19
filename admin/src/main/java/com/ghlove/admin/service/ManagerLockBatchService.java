package com.ghlove.admin.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 배치 updateLockForManagerBatch("사용되지 않는 사용자 잠금처리", AS-IS 01:35, AS-IS에서 중지 상태) -
 * AS-IS {@code UserServiceImpl.updateLockForManager} + user-mapper {@code updateLockForManager}: ISMS 설정 UNUSED_MANAGER(일수)가
 * 있으면, 마지막 로그인(없으면 등록일)이 그 일수 전 날짜보다 앞선 관리자를 로그인 실패횟수 10000으로 잠근다
 * (로그인은 실패횟수 기준으로 잠김 판정 - [[admin-manager-member-account-link]]). 날짜는 AS-IS처럼 yyyyMMddHHmmss 문자열 비교.
 */
@Service
@RequiredArgsConstructor
public class ManagerLockBatchService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final EntityManager entityManager;

    @Transactional
    @SuppressWarnings("unchecked")
    public String lockUnusedManagers() {
        List<Object> values = entityManager.createNativeQuery(
                "select value from admin.op_config_isms where key = 'UNUSED_MANAGER'").getResultList();
        String unusedManager = values.isEmpty() || values.get(0) == null ? null : values.get(0).toString().trim();
        if (unusedManager == null || unusedManager.isEmpty()) {
            return "UNUSED_MANAGER 미설정 - 처리 안 함";
        }
        int days;
        try {
            days = Integer.parseInt(unusedManager);
        } catch (NumberFormatException e) {
            // AS-IS StringUtils.string2integer는 숫자가 아니면 0
            days = 0;
        }
        String until = LocalDate.now().minusDays(days).format(YMD) + "000000";
        int locked = entityManager.createNativeQuery("update admin.op_manager set login_fail_count = 10000, updated_date = :now "
                        + "where login_date < :until or (login_date is null and created_date < :until)")
                .setParameter("now", LocalDateTime.now().format(DATETIME))
                .setParameter("until", until)
                .executeUpdate();
        return "잠금 " + locked + "건(미사용 " + days + "일)";
    }
}
