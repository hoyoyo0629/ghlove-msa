package com.ghlove.admin.service;

import com.ghlove.admin.web.support.PointCheckParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 포인트사용 정합성검증 (메뉴 7210) - AS-IS saleson.shop.pointcheck 이식.
 *
 * <p>AS-IS는 주문번호 하나를 기준으로 세 곳의 금액을 모아 비교한다:
 * <ol>
 *   <li>{@code OP_ORDER_ITEM} ∪ {@code OP_ORDER_ITEM_HOLD} → 결제금액·취소금액·입금대기금액·미정주문건수</li>
 *   <li>{@code OP_REMITTANCE_DETAIL} → 정산금액</li>
 *   <li>{@code G_CNTR_USE_POINT}(USE_SE_CODE='1') → 사용완료 포인트</li>
 * </ol>
 * 그리고 <b>결제금액 − 입금대기금액 − 취소금액 == 사용완료 포인트</b>면 '금액일치', 아니면
 * '금액불일치'다. 목록 맨 위에는 전체 합계 행('합계')이 붙고, 그 행의 정합여부 칸에는 차액이 찍힌다.
 *
 * <p><b>MSA 구성 차이</b>: 세 표의 소유 서비스가 달라(주문=order, 사용포인트=point) 한 쿼리로
 * 조인할 수 없으므로, 각 서비스의 조회 전용 집계 API를 불러 admin이 주문번호로 맞춘다.
 * TO-BE 주문 모델 대응은 order 쪽 {@code OrderReconciliationRepository} 주석에 적어 두었다
 * (결제금액=포인트 결제액, 취소금액=상태 CANCELLED, 입금대기금액은 TO-BE에 해당 상태가 없어 0,
 * 정산금액·미정주문건수는 아직 쌓이는 표가 없어 0).
 */
@Service
@RequiredArgsConstructor
public class PointCheckService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** AS-IS 합계 행의 주문번호 칸. */
    public static final String TOTAL_ROW_LABEL = "합계";

    private final OrderClient orderClient;
    private final PointClient pointClient;
    private final LocgovClient locgovClient;

    /** AS-IS PointCheck 도메인 - 화면 11컬럼. 금액은 AS-IS처럼 천단위 콤마 문자열로 내린다. */
    @Getter
    public static class PointCheckRow {
        private final String orderCode;
        private final String locgovNm;
        private final String orderStatusDesc;
        private final String orderAmt;
        private final String orderAmtCancel;
        private final String orderAmt0;
        private final String remittanceAmt;
        private final String cntrUsePoint;
        private final String matchYn;
        private final String holdCnt;
        private final String createdDate;

        PointCheckRow(String orderCode, String locgovNm, String orderStatusDesc, long orderAmt,
                      long orderAmtCancel, long orderAmt0, long remittanceAmt, long cntrUsePoint,
                      String matchYn, long holdCnt, String createdDate) {
            this.orderCode = orderCode;
            this.locgovNm = locgovNm;
            this.orderStatusDesc = orderStatusDesc;
            this.orderAmt = comma(orderAmt);
            this.orderAmtCancel = comma(orderAmtCancel);
            this.orderAmt0 = comma(orderAmt0);
            this.remittanceAmt = comma(remittanceAmt);
            this.cntrUsePoint = comma(cntrUsePoint);
            this.matchYn = matchYn;
            this.holdCnt = comma(holdCnt);
            this.createdDate = createdDate;
        }

        /** AS-IS TO_CHAR(..., '999,999,999,999'). */
        private static String comma(long value) {
            return String.format("%,d", value);
        }
    }

    /**
     * AS-IS getPointCheckList - 주문일자 범위가 비어 있으면 <b>올해 1월 1일 ~ 오늘</b>로 채우는
     * 기본값까지 AS-IS 컨트롤러와 같다.
     */
    public List<PointCheckRow> getPointCheckList(PointCheckParam param) {
        applyDefaultPeriod(param);

        List<OrderClient.OrderAmountRow> orders = orderClient.reconciliationOrderAmounts(
                param.getQuery(), param.getSearchStartDate(), param.getSearchEndDate());
        Map<String, Long> usedPoints = pointClient.cntrUsedPointsByOrderCode(param.getQuery());
        Map<String, String> locgovNames = locgovNames();

        List<PointCheckRow> rows = new ArrayList<>();
        long totalAmt = 0;
        long totalCancel = 0;
        long totalAmt0 = 0;
        long totalRemittance = 0;
        long totalUsed = 0;
        long totalHold = 0;

        for (OrderClient.OrderAmountRow order : orders) {
            long used = usedPoints.getOrDefault(order.orderCode(), 0L);
            // AS-IS: 결제금액 - 입금대기금액 - 취소금액 == 사용완료 포인트
            boolean matched = (order.orderAmt() - order.orderAmt0() - order.orderAmtCancel()) == used;

            if (param.getMatchYnType() != null && !param.getMatchYnType().isBlank()
                    && !param.getMatchYnType().equals(matched ? "Y" : "N")) {
                continue;
            }

            rows.add(new PointCheckRow(order.orderCode(),
                    locgovNames.getOrDefault(order.locgovCode(), order.locgovCode()),
                    orderStatusDesc(order.orderStatus()),
                    order.orderAmt(), order.orderAmtCancel(), order.orderAmt0(),
                    order.remittanceAmt(), used,
                    matched ? "금액일치" : "금액불일치",
                    order.holdCnt(), dayOf(order.createdDate())));

            totalAmt += order.orderAmt();
            totalCancel += order.orderAmtCancel();
            totalAmt0 += order.orderAmt0();
            totalRemittance += order.remittanceAmt();
            totalUsed += used;
            totalHold += order.holdCnt();
        }

        // AS-IS는 T_COPY(TP=1/2)로 같은 결과를 두 번 묶어 '합계' 행을 만들고, ORDER BY ... desc로
        // 목록 맨 위에 둔다. 합계 행의 정합여부 칸에는 차액(결제-취소-입금대기-사용포인트)이 찍힌다.
        long diff = totalAmt - totalCancel - totalAmt0 - totalUsed;
        rows.add(0, new PointCheckRow(TOTAL_ROW_LABEL, null, null, totalAmt, totalCancel, totalAmt0,
                totalRemittance, totalUsed, String.format("%,d", diff), totalHold, null));
        return rows;
    }

    /** AS-IS 컨트롤러 - 시작일·종료일이 둘 다 비면 올해 1월 1일 ~ 오늘. */
    public void applyDefaultPeriod(PointCheckParam param) {
        boolean noStart = param.getSearchStartDate() == null || param.getSearchStartDate().isBlank();
        boolean noEnd = param.getSearchEndDate() == null || param.getSearchEndDate().isBlank();
        if (noStart && noEnd) {
            param.setSearchStartDate(DAY.format(Year.now().atMonth(1).atDay(1)));
            param.setSearchEndDate(DAY.format(LocalDate.now()));
        }
    }

    /**
     * AS-IS는 주문상태 코드(0/10/20/...)를 한글 라벨로 바꿔 '+'로 이어 붙인다(한 주문에 여러
     * 항목 상태가 섞일 수 있어서다). TO-BE od_order는 코드가 아니라 이름(CONFIRMED 등)이라
     * 같은 뜻의 한글 라벨로 바꿔 준다.
     */
    private static String orderStatusDesc(String statuses) {
        if (statuses == null || statuses.isBlank()) {
            return null;
        }
        List<String> labels = new ArrayList<>();
        for (String status : statuses.split("\\+")) {
            labels.add(STATUS_LABELS.getOrDefault(status.trim(), status.trim()));
        }
        return String.join("+", labels);
    }

    /** TO-BE od_order.order_status(이름) → AS-IS 화면 라벨. */
    private static final Map<String, String> STATUS_LABELS = Map.of(
            "CREATED", "결제대기",
            "PAID", "결제완료",
            "PREPARING", "배송준비중",
            "SHIPPING", "배송중",
            "DELIVERED", "배송완료",
            "CONFIRMED", "구매확정",
            "PARTIALLY_CONFIRMED", "부분 구매확정",
            "CLAIM_REQUESTED", "클레임 접수",
            "CANCELLED", "취소완료");

    /** AS-IS op:date - 주문일자는 날짜만 보여준다. */
    private static String dayOf(LocalDateTime value) {
        return value == null ? null : value.toLocalDate().toString();
    }

    /** 지자체명 - AS-IS는 g_locgov의 상위지자체명 + ' ' + 지자체명이다. */
    private Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> {
            String upper = l.upperLocgovNm();
            String name = l.locgovNm();
            names.put(l.locgovCode(),
                    (upper == null || upper.isBlank() ? "" : upper + " ") + (name == null ? "" : name));
        });
        return names;
    }
}
