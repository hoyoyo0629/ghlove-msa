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

    public GiftClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
                      @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        // gift의 /api/admin/*(탈퇴 시 관심답례품 삭제)는 내부 전용이라 공유 시크릿을 실어야 통과한다.
        // 나머지 조회(/api/my-summary)는 시크릿을 요구하지 않지만 함께 실어도 무방하다.
        this.restClient = RestClient.builder()
                .baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret)
                .build();
    }

    /**
     * 탈퇴 시 관심답례품 전량 삭제 (AS-IS deleteSecedeGeneralCustomerIntrstRtnpsnt).
     * 실패하면 member가 탈퇴를 중단해야 하므로(관심답례품이 orphan으로 남지 않도록)
     * 예외를 삼키지 않는다.
     */
    public void deleteWishlistOnWithdrawal(Long userId) {
        try {
            restClient.post()
                    .uri("/api/admin/wishlist/delete-on-withdrawal?userId={userId}", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new MemberException("관심답례품 삭제 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
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
