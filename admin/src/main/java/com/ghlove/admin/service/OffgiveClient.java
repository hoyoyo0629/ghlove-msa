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

/** 오프라인기부 접수 관리 (AS-IS opmanager/offgive) - 실제 데이터/도메인효과는 donation
 *  서비스에 있고(OffgiveClient), 여기는 admin 콘솔의 로그인/RBAC과 화면만 담당한다. */
@Component
public class OffgiveClient {

    private final RestClient restClient;

    public OffgiveClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public List<Donation> list() {
        try {
            List<Donation> list = restClient.get().uri("/api/offgive").retrieve()
                    .body(new ParameterizedTypeReference<List<Donation>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** AS-IS offgive/list.jsp 검색조건(지자체/접수일자 범위) 재현. */
    /**
     * 기부금 접수관리 목록(메뉴 15101) - AS-IS 검색조건 전체. 이름은 회원(member) 소유라
     * USER_ID만 오고 admin이 채운다.
     */
    public List<AdminRow> adminSearch(String shKeyword, String shText, String startDate, String endDate,
                                       java.math.BigDecimal amountFrom, java.math.BigDecimal amountTo,
                                       String cntrSttusCode, String rceptBankCode, String rceptBankNm,
                                       String role) {
        try {
            List<AdminRow> rows = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/admin/offgive/search");
                        addIfPresent(uriBuilder, "shKeyword", shKeyword);
                        addIfPresent(uriBuilder, "shText", shText);
                        addIfPresent(uriBuilder, "startDate", startDate);
                        addIfPresent(uriBuilder, "endDate", endDate);
                        if (amountFrom != null) {
                            uriBuilder.queryParam("amountFrom", amountFrom);
                        }
                        if (amountTo != null) {
                            uriBuilder.queryParam("amountTo", amountTo);
                        }
                        addIfPresent(uriBuilder, "cntrSttusCode", cntrSttusCode);
                        addIfPresent(uriBuilder, "rceptBankCode", rceptBankCode);
                        addIfPresent(uriBuilder, "rceptBankNm", rceptBankNm);
                        // AS-IS getOffgiveList는 role로 접수경로(OFF_MAIN/OFF_SUB=200, OFF_CENTER=300)와 지점명 조건을 가른다
                        addIfPresent(uriBuilder, "role", role);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<AdminRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (org.springframework.web.client.RestClientException e) {
            return List.of();
        }
    }

    private static void addIfPresent(org.springframework.web.util.UriBuilder uriBuilder, String name,
                                      String value) {
        if (value != null && !value.isBlank()) {
            uriBuilder.queryParam(name, value);
        }
    }

    /** AS-IS 목록 11컬럼의 원본 - 기부상태는 TO-BE 낱말, 이름은 USER_ID로만 온다. */
    public record AdminRow(String cntrSn, String cntrSttusCode, String elctrnPayNo, String sttemntPayDe,
                            String cntrLocgovCode, String upperLocgovNm, String locgovNm, Long userId,
                            BigDecimal cntrAmt, String rceptBankCode, String rceptBankNm,
                            String frstRegistPnttm, String prjSubject, Long cntrPoint,
                            String rtnpsntReqstCode) {
    }

    public List<Donation> search(String locgovCode, String startDate, String endDate) {
        try {
            List<Donation> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/offgive/search")
                            .queryParamIfPresent("locgovCode", java.util.Optional.ofNullable(blankToNull(locgovCode)))
                            .queryParamIfPresent("startDate", java.util.Optional.ofNullable(blankToNull(startDate)))
                            .queryParamIfPresent("endDate", java.util.Optional.ofNullable(blankToNull(endDate)))
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Donation>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public Donation get(String cntrSn) {
        try {
            return restClient.get().uri("/api/offgive/{cntrSn}", cntrSn).retrieve().body(Donation.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기부내역을 찾을 수 없습니다."));
        }
    }

    /** 기탁서 상세(AS-IS getOffgive)의 g_cntr 부분 + 포인트. */
    public DetailResult detail(String cntrSn) {
        try {
            return restClient.get().uri("/api/offgive/{cntrSn}/detail", cntrSn).retrieve().body(DetailResult.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기부내역을 찾을 수 없습니다."));
        }
    }

    public record Detail(String cntrSn, String frstRegistPnttm, String infoAgreAt, String upperLocgovCode,
                         String locgovCode, String locgovNm, BigDecimal cntrAmt, BigDecimal cntrAmtYearTotal,
                         String rceptBankNm, String elctrnPayNo, String cntrSttusCode, Long reqId,
                         String prjSubject, Long userId) {
    }

    public record DetailResult(Detail offgive, long cntrPoint, long cntrUsePoint, long cntrBlcePoint) {
    }

    /** AS-IS maxCheck - 개인 연간 잔여한도. */
    public BigDecimal limit(Long userId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/offgive/limit");
                        if (userId != null) {
                            uriBuilder.queryParam("userId", userId);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve().body(BigDecimal.class);
        } catch (RestClientException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * AS-IS selectDesignatedDonationList(FRONT/모금중/DONATION). locgovCode가 있으면 그 지자체만
     * (기탁서 화면 사업 선택), 없으면 전 지자체(지정기부목록 엑셀, sort=BSNS).
     */
    public List<DesignatedRow> designatedDonationList(String locgovCode, String sort) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/offgive/designated-donation-list");
                    addIfPresent(uriBuilder, "locgovCode", locgovCode);
                    addIfPresent(uriBuilder, "sort", sort);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<DesignatedRow>>() {
                });
    }

    public record DesignatedRow(Long prjId, String prjSubject, String locgovNm, String bsnsType,
                                String bsnsSubType, String prjStDt, String prjEdDt, Long targetAmt, Long sumAmt) {
    }

    /** AS-IS give-state options-by-locgovCode - [{LOCGOV_CODE, LOCGOV_NM}]. */
    public List<Map<String, Object>> locgovOptions(String code) {
        return restClient.get().uri("/api/offgive/locgov-options?code={c}", code).retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
    }

    /** AS-IS getLocGovLmtt - 기부제한 중이 아니면 null. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> locgovLmtt(String cityCode) {
        return restClient.get().uri("/api/offgive/locgov-lmtt?cityCode={c}", cityCode).retrieve().body(Map.class);
    }

    /** AS-IS getLocgovMapngCode - 매핑이 없으면 null. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> locgovMapngCode(String locgovCode) {
        return restClient.get().uri("/api/offgive/locgov-mapng-code?locgovCode={c}", locgovCode).retrieve()
                .body(Map.class);
    }

    /** AS-IS selectLocgovByAdmCd(행정기관코드 앞 5자리 → 고향사랑 지자체코드), 없으면 null. */
    @SuppressWarnings("unchecked")
    public String locgovCodeByAdmCd(String admCd5) {
        Map<String, Object> body = restClient.get().uri("/api/offgive/adm-locgov-code?admCd={c}", admCd5)
                .retrieve().body(Map.class);
        return body == null || body.get("locgovCode") == null ? null : body.get("locgovCode").toString();
    }

    /** AS-IS offgive/view/{locgovCode} - {bassAdres}, 지자체가 없으면 null. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> locgovView(String locgovCode) {
        return restClient.get().uri("/api/offgive/locgov-view/{c}", locgovCode).retrieve().body(Map.class);
    }

    public record GiftLine(Long itemId, Long itemOptionId, Integer quantity) {
    }

    /** AS-IS form.jsp가 {@code #offgive}로 직렬화해 POST하는 전체 필드. */
    public record RegisterRequest(
            Long userId, String userName, String jumin1, String jumin2, String mberCi, String mberDi,
            String phoneNumber, String address, String addressDetail,
            String psitnLocgovCode, String locgovCode, BigDecimal amount, Long dsgnDntnBizId,
            String rtnpsntReqstCode, String rcvUserName, String rcvPhoneNumber,
            String rcvAddress, String rcvAddressDetail, List<GiftLine> giftOrders,
            String rceptBankCode, String rceptBankNm, String rcepterNm,
            String elctrnPayNo, String signatureImage, String birthday, String rcvPost,
            String locgovUpperCode, String linkMngKey,
            // AS-IS insertOffgive 즉석가입 USER_DETAIL.POST·GENDER(폼 #post, #gender)
            String post, String gender) {
    }

    public RegisterResult register(RegisterRequest request) {
        try {
            return restClient.post().uri("/api/offgive")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve().body(RegisterResult.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "오프라인 기부 등록에 실패했습니다."));
        }
    }

    /** 수납처리(AS-IS 수납확인 배치 대체 - 관리자 수동 트리거). */
    public Donation confirm(String cntrSn) {
        try {
            return restClient.post().uri("/api/offgive/{cntrSn}/confirm", cntrSn)
                    .retrieve().body(Donation.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "수납처리에 실패했습니다."));
        }
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

    public record Donation(String cntrSn, String cntrDe, Long userId, String cntrLocgovCode, BigDecimal cntrAmt,
                            String cntrSttusCode, String cntrPathCode, String rceptBankCode, String rceptBankNm,
                            String rcepterNm, String elctrnPayNo, String psitnLocgovCode, String rtnpsntReqstCode,
                            Long dsgnDntnBizId, String frstRegistPnttm, String signatureFileNm) {
    }

    public record WalkIn(Long userId, String loginId, String tempPassword) {
    }

    public record RegisterResult(Donation donation, WalkIn walkIn) {
    }
}
