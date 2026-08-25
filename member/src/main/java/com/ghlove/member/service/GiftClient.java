package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 마이페이지 "관심답례품"/"답례품 후기"/"답례품Q&A" 카드에 건수를 보여주기 위한 읽기 전용
 * 조회 - OrderClient와 동일한 패턴의 의도적인 동기 호출 예외. 조회 실패 시 마이페이지
 * 자체가 깨지면 안 되므로 0건으로 조용히 대체한다.
 */
@Component
@Slf4j
public class GiftClient {

    private final RestClient restClient;

    public GiftClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
    }

    public GiftSummaryInfo mySummary(Long userId) {
        try {
            GiftSummaryInfo summary = restClient.get()
                    .uri("/api/my-summary?userId={userId}", userId)
                    .retrieve()
                    .body(GiftSummaryInfo.class);
            return summary != null ? summary : new GiftSummaryInfo(0, 0, 0);
        } catch (RestClientException e) {
            log.warn("Failed to fetch gift summary from gift service - showing zeros", e);
            return new GiftSummaryInfo(0, 0, 0);
        }
    }

    public record GiftSummaryInfo(int reviewCount, int inquiryCount, int wishlistCount) {
    }
}
