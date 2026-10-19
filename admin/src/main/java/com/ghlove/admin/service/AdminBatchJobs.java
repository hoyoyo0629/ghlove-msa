package com.ghlove.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * admin 소유 배치 - 키는 AS-IS OP_BATCH_JOB.JOB_METHOD, 실행 여부·시각은 관리 화면 설정({@link BatchJobScheduler}).
 * 본문은 프록시를 타는 서비스 빈을 불러 트랜잭션을 보존한다.
 */
@Component
@RequiredArgsConstructor
public class AdminBatchJobs implements BatchJobSource {

    private final AdminMainService adminMainService;
    private final NoBugaLocgovMailService noBugaLocgovMailService;
    private final SmartDeliveryBatchService smartDeliveryBatchService;
    private final NhBatchService nhBatchService;
    private final Nuri2PresentOrderService nuri2PresentOrderService;
    private final ManagerLockBatchService managerLockBatchService;
    private final PointExpirationMailService pointExpirationMailService;

    @Override
    public Map<String, Supplier<String>> batchJobs() {
        Map<String, Supplier<String>> jobs = new LinkedHashMap<>();
        // AS-IS id 2000103 "보고용대시보드" 매일 18:00 - 메인 "기부현황 복사" 영역의 원천(REPORT_DASHBOARD)
        jobs.put("confirmdonationForReportBatch", adminMainService::reportDashboardBatch);
        // AS-IS id 2000105 "3일 기부현황 내역 없는 지자체" 월·목 09:00 - 메일 발송
        jobs.put("getNoBugaLocgovListBatch", noBugaLocgovMailService::send);
        // AS-IS id 1000005 "배송 완료 처리(스마트택배)" 23:30 - 스마트택배 연계·코드표가 admin 소유
        jobs.put("autoSmartDeliveryShippingCompleteBatch", smartDeliveryBatchService::run);
        // AS-IS id 2000001/2000106/2000107 "농협 연계" 04:25/04:30/04:35 - 농협 분석 DB(stat_*) 반출
        jobs.put("userCntrForNhBatch", nhBatchService::userCntr);
        jobs.put("userCntrLocForNhBatch", nhBatchService::userCntrLocgov);
        jobs.put("locForNhBatch", nhBatchService::locgov);
        // AS-IS id 2000120 "답례품 주문내역 알림톡 전송" 09:00 - 누리2 발송대기 표(NURI2_NRMSG_DATA)
        jobs.put("sendNuri2PresentOrder", nuri2PresentOrderService::send);
        // AS-IS id 24 "사용되지 않는 사용자 잠금처리" 01:35 - AS-IS에서 중지 상태
        jobs.put("updateLockForManagerBatch", managerLockBatchService::lockUnusedManagers);
        // AS-IS id 2 "기부 포인트 만료 안내 메일발송" 02:30 - AS-IS에서 중지 상태
        jobs.put("expirationPointSendMessageBatch", pointExpirationMailService::send);
        return jobs;
    }
}
