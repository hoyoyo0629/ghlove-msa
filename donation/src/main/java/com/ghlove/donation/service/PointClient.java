package com.ghlove.donation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * 마이페이지 "기부내역 조회"의 "기부포인트" 컬럼용 - 읽기 전용 조회. MemberClient와
 * 동일한 패턴이지만, 조회 실패 시 화면 전체가 깨지면 안 되므로(기부포인트 표시는
 * 부가정보) 예외를 던지지 않고 빈 맵으로 조용히 대체한다.
 */
@Component
@Slf4j
public class PointClient {

    private final RestClient restClient;

    public PointClient(@Value("${ghlove.point-service.base-url}") String pointServiceBaseUrl) {
        this.restClient = RestClient.create(pointServiceBaseUrl);
    }

    public Map<String, Long> earnedPointsByCntrSn(List<String> cntrSns) {
        if (cntrSns.isEmpty()) {
            return Map.of();
        }
        try {
            Map<String, Long> earned = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/ledger/earned")
                            .queryParam("cntrSns", cntrSns)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Long>>() {
                    });
            return earned != null ? earned : Map.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch earned points from point service - showing empty map", e);
            return Map.of();
        }
    }

    /** 기부금 변경신청(give-reqmng) 승인/신청 시 "포인트 사용이력 존재 여부" 확인용 -
     *  이 기부건으로 적립된 lot이 얼마나 소진됐는지(FIFO)를 그대로 읽는다. 적립 자체가
     *  없으면(아직 크레딧 전, 또는 이미 회수됨) null. */
    public EarnLot earnLotOf(String cntrSn) {
        try {
            List<EarnLot> lots = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/ledger/earned-lots")
                            .queryParam("cntrSns", List.of(cntrSn)).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EarnLot>>() {
                    });
            return lots != null && !lots.isEmpty() ? lots.get(0) : null;
        } catch (RestClientException e) {
            log.warn("Failed to fetch earn lot for cntrSn={}", cntrSn, e);
            return null;
        }
    }

    // 포인트 적립(쓰기)은 이 클라이언트에서 동기 REST로 호출하지 않는다 - give-reqmng
    // 승인의 "포인트생성"도 CntrReqmngService가 donation.lifecycle COMPLETED 이벤트를
    // 재발행하는 방식으로 처리한다(SFR-003 "동기 호출 금지", SFR-004 ★ 이벤트 기반 적립).
    // 이 클라이언트는 조회 전용으로만 남긴다.

    public record EarnLot(String cntrSn, Long earnedAmount, Long remainingAmount) {
    }

    /** 기부하기 화면의 "포인트 적립예상"용 지자체별 적립률(%). 조회 실패 시 표시를 생략할
     *  수 있도록 null을 돌려준다(적립 자체는 point 서비스가 이벤트로 처리하므로 여기서
     *  못 읽는다고 기부가 막히면 안 된다). */
    public java.math.BigDecimal pointRateOf(String locgovCode) {
        try {
            Map<String, java.math.BigDecimal> body = restClient.get()
                    .uri("/api/point-rate?locgovCode={locgovCode}", locgovCode)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, java.math.BigDecimal>>() {
                    });
            return body != null ? body.get("rate") : null;
        } catch (RestClientException e) {
            log.warn("Failed to fetch point rate for locgovCode={} - hiding estimate", locgovCode, e);
            return null;
        }
    }
}
