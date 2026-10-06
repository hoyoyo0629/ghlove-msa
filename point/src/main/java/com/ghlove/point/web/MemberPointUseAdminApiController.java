package com.ghlove.point.web;

import com.ghlove.point.domain.GCntrUsePoint;
import com.ghlove.point.repository.GCntrUsePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * admin 일반회원관리(메뉴 4101)가 호출하는 회원별 <b>포인트 사용</b> 조회 API (조회 전용).
 *
 * <p>AS-IS 상세화면의 "포인트 내역"은 {@code G_CNTR}(적립)과 {@code G_CNTR_USE_POINT}(사용)를
 * {@code UNION ALL}한 한 쿼리였다. MSA에서는 적립이 donation, 사용이 point 소유라 각자 내려주고
 * admin이 합쳐서 등록시각 역순으로 정렬하고 지자체별 잔액을 계산한다(AS-IS와 같은 공식).
 *
 * <p>{@code frstRegistPnttm}은 <b>yyyyMMddHHmmss 문자열</b>로 내려준다 - 이 테이블은 TIMESTAMP지만
 * donation의 {@code g_cntr.frst_regist_pnttm}은 VARCHAR(14)라서, admin이 두 쪽을 한 줄로 세우려면
 * 비교 가능한 같은 표기여야 한다(AS-IS는 한 DB에서 같은 타입이라 그냥 비교했다).
 */
@RestController
@RequestMapping("/api/admin/member-points")
@RequiredArgsConstructor
public class MemberPointUseAdminApiController {

    private static final DateTimeFormatter PNTTM = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final GCntrUsePointRepository gCntrUsePointRepository;

    /**
     * 포인트 사용 행 전체.
     *
     * <p>AS-IS 포인트 내역의 USE 쪽은 <b>use_se_code를 가리지 않는다</b>(사용·소멸 등 모두 보여준다) -
     * 잔액 누적계산 서브쿼리도 마찬가지다. 누적합계의 "사용포인트"만 '1'(사용)로 걸러진다.
     */
    @GetMapping("/{userId}/use-rows")
    public List<UseRow> useRows(@PathVariable Long userId) {
        return gCntrUsePointRepository.findByUserId(userId).stream()
                .map(MemberPointUseAdminApiController::toRow)
                .toList();
    }

    /** 회원별 사용포인트(USE_SE_CODE='1') 합계 일괄 조회 - 목록의 포인트잔액 계산용. */
    @GetMapping("/used-totals")
    public Map<Long, Long> usedTotals(@RequestParam List<Long> userIds) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (Object[] row : gCntrUsePointRepository.sumUsedPointByUserIds(userIds)) {
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return result;
    }

    private static UseRow toRow(GCntrUsePoint p) {
        return new UseRow(p.getUseSn(), p.getPointUseDe(), p.getCntrLocgovCode(), p.getCntrUsePoint(),
                p.getOrderCode(), p.getUseSeCode(),
                p.getFrstRegistPnttm() == null ? null : p.getFrstRegistPnttm().format(PNTTM));
    }

    /** 지자체명은 donation이 가진 G_LOCGOV에 있어 코드만 내려준다 - admin이 붙인다. */
    public record UseRow(Integer useSn, String pointUseDe, String cntrLocgovCode, Long cntrUsePoint,
                         String orderCode, String useSeCode, String frstRegistPnttm) {
    }
}
