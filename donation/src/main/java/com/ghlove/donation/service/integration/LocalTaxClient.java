package com.ghlove.donation.service.integration;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.DonationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 세외수입 부과/수납 연계 (AS-IS NgDonationRelayServiceImpl - 서울시는 이택스,
 * 그 외 지자체는 위택스/지방세외수입시스템으로 갈라진다). 방화벽이 열리기 전까지는
 * `ghlove.integrations.local-tax.enabled=false`로 두면 실제 호출 없이 모크 응답으로
 * 지금까지의 "결제완료 모크" 동작을 그대로 유지한다 - true로 바꾸고 URL/인증정보만
 * 채우면 실제 연계로 전환되는 구조.
 */
@Component
@Slf4j
public class LocalTaxClient {

    private static final String SEOUL_PREFIX = "11";

    private static final java.time.format.DateTimeFormatter FAKE_EPAY_TS =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final boolean enabled;
    private final RestClient seoulClient;
    private final RestClient contryClient;
    private final String linkTrgtCd;

    public LocalTaxClient(
            @Value("${ghlove.integrations.local-tax.enabled}") boolean enabled,
            @Value("${ghlove.integrations.local-tax.seoul-base-url}") String seoulBaseUrl,
            @Value("${ghlove.integrations.local-tax.contry-base-url}") String contryBaseUrl,
            @Value("${ghlove.integrations.local-tax.link-trgt-cd}") String linkTrgtCd) {
        this.enabled = enabled;
        this.seoulClient = RestClient.create(seoulBaseUrl);
        this.contryClient = RestClient.create(contryBaseUrl);
        this.linkTrgtCd = linkTrgtCd;
    }

    public boolean enabled() {
        return enabled;
    }

    /** AS-IS 상수 LINK_TRGT_CD(연계대상코드) - 설정으로 뺐다. */
    public String linkTrgtCd() {
        return linkTrgtCd;
    }

    /** 지방세외 차세대 부과요청 응답 (AS-IS linkRstCd/linkRstMsg). */
    public record RelayResponse(String linkRstCd, String linkRstMsg) {
        public boolean success() {
            return "000".equals(linkRstCd);
        }
    }

    /** 서울 세외 부과요청 응답 (AS-IS는 enapbuNo만 쓰고, 오류는 gif_seoul에 쌓인다). */
    public record SeoulResponse(String enapbuNo, String errorCd, String errorMsg) {
    }

    /**
     * 지방세외수입 차세대 부과요청 (AS-IS {@code nextBugaRequest}의 통신 부분).
     *
     * <p>연계가 꺼져 있으면 <b>AS-IS가 LOCAL에서 만들던 가짜 응답과 같은 모양</b>을 돌려준다 -
     * 전자납부번호 {@code "99" + yyyyMMddHHmmssSSS}, {@code linkRstCd="000"},
     * {@code linkRstMsg="LOCAL FAKE : {전자납부번호}"}.
     */
    public RelayResponse sendNextBuga(NextBugaRequest request) {
        if (!enabled) {
            String fakeEpayNo = "99" + java.time.LocalDateTime.now().format(FAKE_EPAY_TS);
            log.warn("[local-tax] disabled - nextBuga relay skipped, fake epayNo {}", fakeEpayNo);
            return new RelayResponse("000", "LOCAL FAKE : " + fakeEpayNo);
        }
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> body = contryClient.post().uri("/nextBugaRequest")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(request.toPayload())
                    .retrieve().body(java.util.Map.class);
            if (body == null) {
                throw new DonationException("지방세외 부과요청 응답이 비어 있습니다.");
            }
            return new RelayResponse(str(body.get("linkRstCd")), str(body.get("linkRstMsg")));
        } catch (RestClientException e) {
            throw new DonationException("지방세외 부과요청에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 서울시 세외수입 부과정보 등록 (AS-IS {@code getSdonationCharge}의 통신 부분).
     * 연계가 꺼져 있으면 같은 규칙의 가짜 전자납부번호와 정상코드('0')를 돌려준다.
     */
    public SeoulResponse sendSeoulBuga(SeoulBugaRequest request) {
        if (!enabled) {
            String fakeEnapbuNo = "99" + java.time.LocalDateTime.now().format(FAKE_EPAY_TS);
            log.warn("[local-tax] disabled - seoulBuga relay skipped, fake enapbuNo {}", fakeEnapbuNo);
            return new SeoulResponse(fakeEnapbuNo, "0", "LOCAL FAKE");
        }
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> body = seoulClient.post().uri("/sntrBugaInsert")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(request.toPayload())
                    .retrieve().body(java.util.Map.class);
            if (body == null) {
                throw new DonationException("서울 세외 부과요청 응답이 비어 있습니다.");
            }
            return new SeoulResponse(str(body.get("enapbuNo")), str(body.get("errorCd")),
                    str(body.get("errorMsg")));
        } catch (RestClientException e) {
            throw new DonationException("서울 세외 부과요청에 실패했습니다: " + e.getMessage());
        }
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** 부과 등록 (AS-IS sntrBugaInsert/contryBugaInsert). */
    public LevyResult registerLevy(Donation donation, Locgov locgov) {
        if (!enabled) {
            log.info("[local-tax] disabled - mock 부과 등록 cntrSn={}", donation.getCntrSn());
            return LevyResult.mock(donation.getCntrSn());
        }
        try {
            return clientFor(locgov).post().uri("/buga")
                    .body(new BugaRequest(donation.getCntrSn(), donation.getUserId(), donation.getCntrDe(),
                            donation.getCntrLocgovCode(), donation.getCntrAmt()))
                    .retrieve().body(LevyResult.class);
        } catch (RestClientException e) {
            throw new DonationException("세외수입 부과 등록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 수납 확인 (AS-IS etaxSunapInfo/contrySunapInfo). */
    public PaymentConfirmResult confirmPayment(Donation donation, Locgov locgov, String bugaNo) {
        if (!enabled) {
            log.info("[local-tax] disabled - mock 수납 확인 cntrSn={} bugaNo={}", donation.getCntrSn(), bugaNo);
            return PaymentConfirmResult.mock();
        }
        try {
            return clientFor(locgov).get().uri("/sunap?bugaNo={bugaNo}", bugaNo)
                    .retrieve().body(PaymentConfirmResult.class);
        } catch (RestClientException e) {
            throw new DonationException("세외수입 수납 확인에 실패했습니다: " + e.getMessage());
        }
    }

    private RestClient clientFor(Locgov locgov) {
        boolean isSeoul = locgov != null && locgov.getLocgovCode() != null
                && locgov.getLocgovCode().startsWith(SEOUL_PREFIX);
        return isSeoul ? seoulClient : contryClient;
    }
}
