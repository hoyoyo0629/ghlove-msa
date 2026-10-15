package com.ghlove.member.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 관리자 메인화면(AS-IS {@code /opmanager} main/index.jsp)의 회원 몫 - AS-IS {@code getOpmanagerMainInfo}의
 * 금일·전체 가입자 2행과 {@code notificationAgreeMemberCount}(알림 수신동의 4종) 포팅.
 * AS-IS 정상회원 {@code STATUS_CODE '9'} → TO-BE {@code 'ACTIVE'}.
 *
 * <p>수신동의는 AS-IS 정본대로 {@code '0'}(수신)만 센다. TO-BE 가입 경로 일부가 'Y'/'N'으로 저장하고 있어
 * 그 회원들은 여기서 빠진다 - 별도 과제(docs/memory/receive-sms-encoding-split.md).
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final String ACTIVE = "ACTIVE";
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final EntityManager entityManager;

    /** {todaySubscribers, allSubscribers}. */
    public Map<String, Long> subscribers() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("todaySubscribers", num(entityManager.createNativeQuery(
                        "select count(*) from member.op_user a where a.created_date like :today and a.status_code = :active")
                .setParameter("today", LocalDate.now().format(YMD) + "%").setParameter("active", ACTIVE)
                .getSingleResult()));
        result.put("allSubscribers", num(entityManager.createNativeQuery(
                        "select count(*) from member.op_user a where a.status_code = :active")
                .setParameter("active", ACTIVE).getSingleResult()));
        return result;
    }

    /** AS-IS 응답 키 그대로: pbanc_count / sms_count / email_count / kakao_count. */
    public Map<String, Long> notificationAgree() {
        Object[] r = (Object[]) entityManager.createNativeQuery("""
                        select count(case when oud.receive_pbanc = '0' then 1 end),
                               count(case when oud.receive_sms = '0' then 1 end),
                               count(case when oud.receive_email = '0' then 1 end),
                               count(case when oud.receive_kakao = '0' then 1 end)
                        from member.op_user_detail oud
                        join member.op_user ou on ou.user_id = oud.user_id
                        where ou.status_code = :active and oud.use_flag = 'Y' and oud.leave_code is null
                        """)
                .setParameter("active", ACTIVE).getSingleResult();
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("pbanc_count", num(r[0]));
        result.put("sms_count", num(r[1]));
        result.put("email_count", num(r[2]));
        result.put("kakao_count", num(r[3]));
        return result;
    }

    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }
}
