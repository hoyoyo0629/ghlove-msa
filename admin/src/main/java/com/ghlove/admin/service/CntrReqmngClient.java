package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 기부금 변경신청 관리 (AS-IS give-reqmng) - 실제 데이터/도메인효과(기부취소, 포인트생성)는
 * donation 서비스(G_CNTR_REQMNG)에 있고, 이 클라이언트가 그 cross-service API를 호출한다.
 * CtbnyOpratnClient와 동일한 "쓰기 대행" 패턴.
 */
@Component
public class CntrReqmngClient {

    private final RestClient restClient;

    public CntrReqmngClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
    }

    public List<Row> listAll() {
        return list(null);
    }

    public List<Row> listByLocgov(String locgovCode) {
        return list(locgovCode);
    }

    private List<Row> list(String locgovCode) {
        try {
            var uri = locgovCode == null || locgovCode.isBlank()
                    ? "/api/cntr-reqmng"
                    : "/api/cntr-reqmng?locgovCode={locgovCode}";
            List<Row> rows = restClient.get()
                    .uri(uri, locgovCode == null ? new Object[0] : new Object[]{locgovCode})
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Row>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** AS-IS list.jsp의 검색조건(지자체/요청분류/승인여부/기간)+페이지네이션 재현. */
    public SearchResult search(String locgovCode, String cntrReqmngCode, String reqStatusCode,
                                String startDate, String endDate, int page, int size) {
        try {
            PageResponse<Row> result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/cntr-reqmng/search")
                            .queryParamIfPresent("locgovCode", java.util.Optional.ofNullable(blankToNull(locgovCode)))
                            .queryParamIfPresent("cntrReqmngCode", java.util.Optional.ofNullable(blankToNull(cntrReqmngCode)))
                            .queryParamIfPresent("reqStatusCode", java.util.Optional.ofNullable(blankToNull(reqStatusCode)))
                            .queryParamIfPresent("startDate", java.util.Optional.ofNullable(blankToNull(startDate)))
                            .queryParamIfPresent("endDate", java.util.Optional.ofNullable(blankToNull(endDate)))
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageResponse<Row>>() {
                    });
            return result != null
                    ? new SearchResult(result.content(), result.totalElements(), result.totalPages(), result.page(), result.size())
                    : new SearchResult(List.of(), 0, 0, page, size);
        } catch (RestClientException e) {
            return new SearchResult(List.of(), 0, 0, page, size);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public DonationInfo donationInfo(String cntrSn) {
        try {
            return restClient.get()
                    .uri("/api/cntr-reqmng/donation/{cntrSn}", cntrSn)
                    .retrieve()
                    .body(DonationInfo.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기부내역을 찾을 수 없습니다."));
        }
    }

    public void submit(String cntrSn, String cntrReqmngCode, String discription, String taxSysCancelDe,
                        String relatedDocDptNm, String relatedDocNum, String relatedDocDe, Long managerId) {
        try {
            restClient.post().uri("/api/cntr-reqmng")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(toForm(cntrSn, cntrReqmngCode, discription, taxSysCancelDe, relatedDocDptNm,
                            relatedDocNum, relatedDocDe, managerId))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "변경신청 등록에 실패했습니다."));
        }
    }

    public void approve(Long reqId, Long managerId) {
        try {
            restClient.post().uri("/api/cntr-reqmng/{reqId}/approve?managerId={managerId}", reqId, managerId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "승인 처리에 실패했습니다."));
        }
    }

    public void cancel(Long reqId, Long managerId) {
        try {
            restClient.post().uri("/api/cntr-reqmng/{reqId}/cancel?managerId={managerId}", reqId, managerId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "요청취소 처리에 실패했습니다."));
        }
    }

    private static MultiValueMap<String, String> toForm(String cntrSn, String cntrReqmngCode, String discription,
                                                          String taxSysCancelDe, String relatedDocDptNm,
                                                          String relatedDocNum, String relatedDocDe, Long managerId) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cntrSn", cntrSn);
        body.add("cntrReqmngCode", cntrReqmngCode);
        body.add("discription", discription);
        if (taxSysCancelDe != null) {
            body.add("taxSysCancelDe", taxSysCancelDe);
        }
        if (relatedDocDptNm != null) {
            body.add("relatedDocDptNm", relatedDocDptNm);
        }
        if (relatedDocNum != null) {
            body.add("relatedDocNum", relatedDocNum);
        }
        if (relatedDocDe != null) {
            body.add("relatedDocDe", relatedDocDe);
        }
        body.add("managerId", String.valueOf(managerId));
        return body;
    }

    private static String extractMessage(RestClientResponseException e, String fallback) {
        try {
            var body = e.getResponseBodyAs(Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? msg.toString() : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public record Row(Long reqId, String loginId, String userName, String locgovCode, String sttemntPayDe,
                       BigDecimal cntrAmt, String cntrReqmngCode, String discription, Long frstRegisterId,
                       String frstRegistPnttm, String reqStatusCode, String cntrSn, String apprDt, String cancleDt) {
    }

    public record DonationInfo(String cntrSn, String cntrDe, Long userId, String locgovCode, BigDecimal cntrAmt,
                                String cntrSttusCode, boolean pointsUsed, String userName, String loginId) {
    }

    public record SearchResult(List<Row> content, long totalElements, int totalPages, int page, int size) {
    }

    private record PageResponse<T>(List<T> content, long totalElements, int totalPages, int page, int size) {
    }
}
