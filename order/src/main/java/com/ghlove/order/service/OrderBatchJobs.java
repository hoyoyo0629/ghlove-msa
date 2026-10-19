package com.ghlove.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * order 소유 배치 - 키는 AS-IS OP_BATCH_JOB.JOB_METHOD, 실행 여부·시각은 관리 화면 설정({@link BatchJobScheduler}).
 * 본문은 프록시를 타는 서비스 빈을 불러 트랜잭션을 보존한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderBatchJobs implements BatchJobSource {

    private final OffgiveOrderService offgiveOrderService;
    private final OrderBatchService orderBatchService;
    private final com.ghlove.order.repository.OrderItemRepository orderItemRepository;
    private final PointClient pointClient;
    private final AdminConfigClient adminConfigClient;

    @Override
    public Map<String, Supplier<String>> batchJobs() {
        Map<String, Supplier<String>> jobs = new LinkedHashMap<>();
        // AS-IS offgiveBatchService.ItemStatusUpdateBatch - "오프라인기부, 답례품 주문시 미수납된 답례품 상태 업데이트"
        jobs.put("allDayOffItemUpdateBatch", () -> {
            log.info(">>->> process allDayOffItemUpdateBatch() >>->>");
            return "주문취소 " + offgiveOrderService.cancelUnpaidDeleted(LocalDate.now()) + "건";
        });
        // AS-IS id 1000003 "장바구니 삭제 배치" 00:10
        jobs.put("deleteCartBatch", orderBatchService::deleteOldCartItems);
        // AS-IS id 2000109 "차세대 수납결과 배치->오프라인 기탁서 답례품 주문건 처리" 5분마다
        jobs.put("cntrSunapBatch", this::processPaidOffgiveOrders);
        // AS-IS id 1000001 "배송 완료 처리" 01:10 - 기준일 OP_CONFIG.SHIPPING_COMPLETE_DATE
        jobs.put("autoShippingCompleteBatch", () -> withDays(adminConfigClient.batchDays().shippingCompleteDate(),
                "배송완료 기준일", orderBatchService::autoShippingComplete));
        // AS-IS id 10 "구매 확정 처리" 01:20 - 기준일 OP_CONFIG.CONFIRM_PURCHASE_DATE
        jobs.put("autoConfirmPurchaseBatch", () -> withDays(adminConfigClient.batchDays().confirmPurchaseDate(),
                "구매확정 기준일", orderBatchService::autoConfirmPurchase));
        return jobs;
    }

    /** 쇼핑몰 설정의 기준일(문자열)로 실행 - 비어 있거나 숫자가 아니면 처리하지 않고 사유를 남긴다. */
    private static String withDays(String value, String label, java.util.function.IntFunction<String> job) {
        int days;
        try {
            days = Integer.parseInt(value.trim());
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalStateException(label + "이 설정되지 않았습니다(사이트 설정 > 배송): " + value);
        }
        return job.apply(days);
    }

    /**
     * cntrSunapBatch - AS-IS {@code NgDonationBatchServiceImpl.cntrSunapBatch}의 본문은 {@code procOffOrder}뿐이다
     * (수납완료(200) 기부의 미처리(OFF_PROC_YN='N') 오프라인 답례품 → 결제처리 → 'Y'). TO-BE는 같은 처리를 포인트 적립
     * 이벤트(point.ledger EARN)로 이미 하므로, 이 배치는 AS-IS처럼 주기적으로 훑어 이벤트가 놓친 건을 처리한다.
     * "수납완료"는 그 기부의 포인트 적립(EARN) 존재로 판정한다(적립은 수납완료에서만 일어나고, 차감이 적립보다 먼저
     * 도착하지 않게 이벤트 경로와 같은 기준을 쓴다). 처리 본문은 이벤트 경로와 같은 메서드라 중복 처리되지 않는다.
     */
    private String processPaidOffgiveOrders() {
        List<String> cntrSns = orderItemRepository.findDistinctCntrSnByOffProcYn("N");
        if (cntrSns.isEmpty()) {
            return "처리 0건";
        }
        int processed = 0;
        for (int i = 0; i < cntrSns.size(); i += 100) {
            Map<String, Long> earned = pointClient.earnedByCntrSn(cntrSns.subList(i, Math.min(i + 100, cntrSns.size())));
            for (String cntrSn : earned.keySet()) {
                try {
                    offgiveOrderService.onDonationPointCredited(cntrSn);
                    processed++;
                } catch (RuntimeException e) {
                    log.error("offline order error cntrSn={}", cntrSn, e);
                }
            }
        }
        return "미처리 기부 " + cntrSns.size() + "건 중 처리 " + processed + "건";
    }
}
