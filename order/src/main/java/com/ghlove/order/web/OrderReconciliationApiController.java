package com.ghlove.order.web;

import com.ghlove.order.repository.OrderReconciliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 포인트사용 정합성검증(admin 메뉴 7210)용 <b>조회 전용</b> API - AS-IS
 * {@code saleson.shop.pointcheck}의 주문측 집계를 admin에 내려준다.
 *
 * AS-IS는 운영관리 한 덩어리였기 때문에 주문항목·정산상세·기부포인트사용을 한 쿼리에서 조인했지만,
 * MSA에서는 각 표의 소유 서비스가 달라(주문=order, 사용포인트=point) 서비스별 집계를 admin이
 * 주문번호로 맞춰 합친다. 사용자 승인 후 추가한 엔드포인트다(2026-10-03).
 */
@RestController
@RequestMapping("/api/admin/reconciliation")
@RequiredArgsConstructor
public class OrderReconciliationApiController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderReconciliationRepository orderReconciliationRepository;

    /**
     * @param query     주문번호 부분일치(AS-IS와 동일)
     * @param startDate yyyyMMdd - 주문일자 시작(그 날 00:00:00부터)
     * @param endDate   yyyyMMdd - 주문일자 종료(그 날 23:59:59까지, AS-IS의 '235959')
     */
    @GetMapping("/order-amounts")
    public List<OrderReconciliationRepository.OrderAmountRow> orderAmounts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return orderReconciliationRepository.orderAmounts(query, startOfDay(startDate), endOfDay(endDate));
    }

    private static LocalDateTime startOfDay(String yyyymmdd) {
        LocalDate date = parse(yyyymmdd);
        return date == null ? null : date.atStartOfDay();
    }

    private static LocalDateTime endOfDay(String yyyymmdd) {
        LocalDate date = parse(yyyymmdd);
        return date == null ? null : date.atTime(LocalTime.of(23, 59, 59));
    }

    private static LocalDate parse(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(yyyymmdd, DAY);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
