package com.ghlove.gift.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * gift 소유 배치 - 키는 AS-IS OP_BATCH_JOB.JOB_METHOD, 실행 여부·시각은 관리 화면 설정({@link BatchJobScheduler}).
 * 본문은 프록시를 타는 {@link GiftBatchService}를 불러 트랜잭션을 보존한다.
 */
@Component
@RequiredArgsConstructor
public class GiftBatchJobs implements BatchJobSource {

    private final GiftBatchService giftBatchService;

    @Override
    public Map<String, Supplier<String>> batchJobs() {
        Map<String, Supplier<String>> jobs = new LinkedHashMap<>();
        // AS-IS id 36 "마감일자 공개여부 처리 배치" 02:10
        jobs.put("itemDisplayContorlBatch", giftBatchService::closeDisplayOfEndedItems);
        // AS-IS id 1000002 "상품 NEW 삭제 배치" 00:10
        jobs.put("deleteItemNewBatch", giftBatchService::clearNewLabel);
        return jobs;
    }
}
