package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * 메인 화면("특정사업에 기부하기")에 진행중인 지정기부사업을 보여주기 위한 읽기 전용
 * 조회 - order의 GiftClient와 동일한 패턴의 의도적인 동기 호출 예외. 조회 실패 시
 * 메인 화면 자체가 깨지면 안 되므로 빈 목록으로 조용히 대체한다.
 */
@Component
@Slf4j
public class DonationClient {

    private final RestClient restClient;

    public DonationClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
                          @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        // donation의 /api/my-summary(회원별 기부총액)는 내부 전용이라 공유 시크릿을 실어야 통과한다.
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<DesignatedProjectInfo> openProjects() {
        try {
            List<DesignatedProjectInfo> projects = restClient.get()
                    .uri("/api/designated-projects")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<DesignatedProjectInfo>>() {
                    });
            return projects != null ? projects : List.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch open designated projects from donation service - showing empty list", e);
            return List.of();
        }
    }

    public GiveStateInfo giveState() {
        try {
            GiveStateInfo state = restClient.get()
                    .uri("/api/give-state")
                    .retrieve()
                    .body(GiveStateInfo.class);
            return state != null ? state : GiveStateInfo.empty();
        } catch (RestClientException e) {
            log.warn("Failed to fetch give-state from donation service - showing zeros", e);
            return GiveStateInfo.empty();
        }
    }

    /** 마이페이지 "기부내역 조회" 카드용. */
    public java.math.BigDecimal myTotal(Long userId) {
        return mySummary(userId).totalAmt();
    }

    /** 메인화면 로그인 계정 박스 "올해 기부액"/"기부총액"용. */
    public DonationSummary mySummary(Long userId) {
        try {
            DonationSummary summary = restClient.get()
                    .uri("/api/my-summary?userId={userId}", userId)
                    .retrieve()
                    .body(DonationSummary.class);
            return summary != null ? summary : DonationSummary.empty();
        } catch (RestClientException e) {
            log.warn("Failed to fetch donation summary from donation service - showing 0", e);
            return DonationSummary.empty();
        }
    }

    public record DonationSummary(java.math.BigDecimal totalAmt, java.math.BigDecimal thisYearAmt) {
        static DonationSummary empty() {
            return new DonationSummary(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO);
        }
    }

    /** "회원 정보 수정" 화면의 관심지자체 칩 목록용. */
    public List<InterestLocgovInfo> interestLocgovsOf(Long userId) {
        try {
            List<InterestLocgovInfo> locgovs = restClient.get()
                    .uri("/api/interest-locgovs?userId={userId}", userId)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<InterestLocgovInfo>>() {
                    });
            return locgovs != null ? locgovs : List.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch interest locgovs from donation service - showing empty list", e);
            return List.of();
        }
    }

    public record InterestLocgovInfo(String locgovCode, String locgovName) {
    }

    /** "회원 정보 수정" 화면의 관심지자체 추가용 시/도·시/군/구 드롭다운. */
    public List<LocgovInfo> allLocgovs() {
        try {
            List<LocgovInfo> locgovs = restClient.get()
                    .uri("/api/locgovs")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<LocgovInfo>>() {
                    });
            return locgovs != null ? locgovs : List.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch locgovs from donation service - showing empty list", e);
            return List.of();
        }
    }

    public record LocgovInfo(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }
}
