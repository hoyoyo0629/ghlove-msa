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

    private final boolean enabled;
    private final RestClient seoulClient;
    private final RestClient contryClient;

    public LocalTaxClient(
            @Value("${ghlove.integrations.local-tax.enabled}") boolean enabled,
            @Value("${ghlove.integrations.local-tax.seoul-base-url}") String seoulBaseUrl,
            @Value("${ghlove.integrations.local-tax.contry-base-url}") String contryBaseUrl) {
        this.enabled = enabled;
        this.seoulClient = RestClient.create(seoulBaseUrl);
        this.contryClient = RestClient.create(contryBaseUrl);
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
