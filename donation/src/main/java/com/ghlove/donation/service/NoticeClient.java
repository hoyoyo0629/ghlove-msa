package com.ghlove.donation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * 기금사업소개("지자체공지사항" 탭)용 - admin 서비스의 OP_NOTICE(LOCGOV_CODE로 필터링)를
 * 읽기 전용으로 조회한다. PointClient와 동일한 패턴 - 조회 실패해도 탭 자체가 깨지면
 * 안 되므로 예외를 던지지 않고 빈 목록으로 조용히 대체한다.
 */
@Component
@Slf4j
public class NoticeClient {

    private final RestClient restClient;

    public NoticeClient(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl) {
        this.restClient = RestClient.create(adminServiceBaseUrl);
    }

    public record NoticeSummary(Integer noticeId, String subject, String createdDate) {
    }

    public List<NoticeSummary> noticesByLocgov(String locgovCode) {
        try {
            List<NoticeSummary> notices = restClient.get()
                    .uri("/api/notices?locgovCode={locgovCode}", locgovCode)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<NoticeSummary>>() {
                    });
            return notices != null ? notices : List.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch locgov notices from admin service for locgovCode={} - showing empty list", locgovCode, e);
            return List.of();
        }
    }
}
