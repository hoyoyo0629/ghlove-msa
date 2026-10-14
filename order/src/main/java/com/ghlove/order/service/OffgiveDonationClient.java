package com.ghlove.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 오프라인 답례품 정리 배치가 donation에 묻는 기부 쪽 조건 - AS-IS는 한 DB라 selectOffItem/selectGiveSunapCheck가
 * G_CNTR를 직접 조인했다. G_CNTR는 donation 소유라 기부일련번호 목록을 넘겨 "오프라인·미납·삭제됨"인 것만 받는다.
 * /api/offgive/**는 donation의 내부 전용 경로라 공유시크릿을 싣는다.
 *
 * <p>실패를 삼키지 않는다 - 배치가 오류로 보고되고(배치 실행로그 7209) 아무 주문도 바뀌지 않는다.
 */
@Component
public class OffgiveDonationClient {

    private final RestClient restClient;

    public OffgiveDonationClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
                                 @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** 주어진 기부일련번호 중 오프라인(200·300)·미납·삭제된 기부의 일련번호. */
    public List<String> deletedUnpaidOffline(List<String> cntrSns) {
        List<String> deleted = restClient.post()
                .uri("/api/offgive/deleted-unpaid")
                .body(cntrSns)
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {
                });
        return deleted != null ? deleted : List.of();
    }
}
