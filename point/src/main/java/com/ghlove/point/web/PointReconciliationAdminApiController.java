package com.ghlove.point.web;

import com.ghlove.point.repository.GCntrUsePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 포인트사용 정합성검증(admin 메뉴 7210)용 <b>조회 전용</b> API - AS-IS
 * {@code pointcheck-mapper}가 한 쿼리에서 조인하던 {@code G_CNTR_USE_POINT} 집계를 admin에 내려준다.
 *
 * AS-IS는 주문항목·정산상세·기부포인트사용을 한 번에 조인했지만 MSA에서는 표의 소유 서비스가
 * 달라(주문=order, 사용포인트=point) 서비스별 집계를 admin이 주문번호로 맞춰 합친다.
 * 사용자 승인 후 추가한 엔드포인트다(2026-10-03).
 */
@RestController
@RequestMapping("/api/admin/reconciliation")
@RequiredArgsConstructor
public class PointReconciliationAdminApiController {

    private final GCntrUsePointRepository gCntrUsePointRepository;

    /** 주문번호별 사용완료 포인트. @param query 주문번호 부분일치(AS-IS와 동일) */
    @GetMapping("/cntr-used-points")
    public List<UsedPointRow> cntrUsedPoints(@RequestParam(required = false) String query) {
        return gCntrUsePointRepository.sumUsedPointByOrderCode(blankToNull(query)).stream()
                .map(r -> new UsedPointRow((String) r[0], r[1] == null ? 0L : ((Number) r[1]).longValue()))
                .toList();
    }

    public record UsedPointRow(String orderCode, long usedPoint) {
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
