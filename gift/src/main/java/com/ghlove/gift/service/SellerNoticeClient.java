package com.ghlove.gift.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.util.List;

/**
 * 판매자 셀프포털의 "판매자 공지사항"(AS-IS /seller/sys-notice) 조회가 admin 서비스의 내부
 * API(/api/seller-notices)를 호출한다. 판매자 공지 데이터(op_sys_notice_seller)는 admin이
 * 소유하므로, SellerOrderClient(gift→order)와 동일하게 공유시크릿(X-Internal-Secret)으로 호출한다.
 */
@Component
public class SellerNoticeClient {

    private static final String HEADER_SECRET = "X-Internal-Secret";

    private final RestClient restClient;
    private final String adminSecret;

    public SellerNoticeClient(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl,
                              @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.create(adminServiceBaseUrl);
        this.adminSecret = adminSecret;
    }

    public record NoticeRow(Long noticeId, String subject, Long hits, String noticeFlag,
                            String frstCrtDt, int attachedFileCnt) {
    }

    public record NoticeFile(Long fileId, String fileName) {
    }

    public record NoticeDetail(Long noticeId, String subject, String content, Long hits,
                               String frstCrtDt, List<NoticeFile> files) {
    }

    public List<NoticeRow> list(String keyword, String startDate, String endDate) {
        try {
            List<NoticeRow> rows = restClient.get()
                    .uri(b -> withParams(b.path("/api/seller-notices"), keyword, startDate, endDate).build())
                    .header(HEADER_SECRET, adminSecret)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<NoticeRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    private static UriBuilder withParams(UriBuilder b, String keyword, String startDate, String endDate) {
        if (keyword != null && !keyword.isBlank()) {
            b.queryParam("keyword", keyword);
        }
        if (startDate != null && !startDate.isBlank()) {
            b.queryParam("startDate", startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            b.queryParam("endDate", endDate);
        }
        return b;
    }

    public NoticeDetail detail(Long noticeId) {
        try {
            return restClient.get()
                    .uri("/api/seller-notices/{id}", noticeId)
                    .header(HEADER_SECRET, adminSecret)
                    .retrieve()
                    .body(NoticeDetail.class);
        } catch (RestClientResponseException e) {
            return null;
        }
    }

    /** 첨부파일 다운로드 - admin이 내려준 바이트와 Content-Disposition 헤더를 그대로 판매자에게 전달한다. */
    public ResponseEntity<byte[]> downloadFile(Long fileId) {
        return restClient.get()
                .uri("/api/seller-notices/files/{fileId}", fileId)
                .header(HEADER_SECRET, adminSecret)
                .retrieve()
                .toEntity(byte[].class);
    }
}
