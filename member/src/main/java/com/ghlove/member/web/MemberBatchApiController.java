package com.ghlove.member.web;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 배치용 회원 조회(내부 전용, /api/admin/** = X-Internal-Secret 보호) - admin 농협 연계 배치
 * (userCntrForNhBatch·userCntrLocForNhBatch)가 AS-IS {@code INNER JOIN op_user b ON ... b.mber_ci IS NOT NULL}을
 * 재현하려고 CI가 있는 회원의 회원번호→CI를 한 번에 받아 간다(회원 상태 조건 없음 - AS-IS 그대로).
 */
@RestController
@RequiredArgsConstructor
public class MemberBatchApiController {

    private final EntityManager entityManager;

    @GetMapping("/api/admin/members/batch/ci-map")
    @SuppressWarnings("unchecked")
    public Map<Long, String> ciMap(@org.springframework.web.bind.annotation.RequestParam(required = false) List<Long> userIds) {
        // userIds를 주면 그 회원만(국세청 영수증 배치처럼 100건 단위로 CI가 필요한 곳), 없으면 CI 있는 회원 전부(농협 연계).
        boolean byIds = userIds != null && !userIds.isEmpty();
        var query = entityManager.createNativeQuery("select user_id, mber_ci from member.op_user where mber_ci is not null"
                + (byIds ? " and user_id in (:ids)" : ""));
        if (byIds) {
            query.setParameter("ids", userIds);
        }
        List<Object[]> rows = query.getResultList();
        Map<Long, String> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put(((Number) r[0]).longValue(), (String) r[1]);
        }
        return map;
    }

    public record MailTarget(Long userId, String loginId, String userName, String email, String statusCode) {
    }

    /**
     * admin 배치 expirationPointSendMessageBatch(포인트 소멸 예정 안내 메일)의 수신자 -
     * AS-IS {@code INNER JOIN OP_USER U ... U.STATUS_CODE IN (4, 9)}(휴면·정상). AS-IS는 휴면이면 OP_USER_SLEEP의
     * 암호화된 이름·이메일을 복호화하지만, TO-BE는 휴면이어도 OP_USER에 그대로 있어 거기서 읽는다.
     */
    @GetMapping("/api/admin/members/batch/mail-targets")
    @SuppressWarnings("unchecked")
    public List<MailTarget> mailTargets(@org.springframework.web.bind.annotation.RequestParam List<Long> userIds) {
        List<Object[]> rows = entityManager.createNativeQuery("select user_id, login_id, user_name, email, status_code "
                        + "from member.op_user where status_code in ('ACTIVE', 'DORMANT') and user_id in (:ids)")
                .setParameter("ids", userIds).getResultList();
        return rows.stream().map(r -> new MailTarget(((Number) r[0]).longValue(), (String) r[1], (String) r[2],
                (String) r[3], (String) r[4])).toList();
    }
}
