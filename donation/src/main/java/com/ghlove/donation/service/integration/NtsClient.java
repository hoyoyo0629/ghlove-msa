package com.ghlove.donation.service.integration;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.MemberInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * 국세청 홈택스 전자기부금영수증 등록 연계 (AS-IS NgDonationRelayServiceImpl.
 * getAccessToken + sendNtsEreceipt). AS-IS는 배치로 재시도(sendNtsEreceiptOnBatch 등)
 * 하는 best-effort 성격이라, 이 서비스에서도 기부 완료 자체를 막지 않고 실패 시
 * DONATION_LEVY.NTS_STATUS=FAILED로만 남긴다. enabled=false(방화벽 미개방)면
 * 즉시 모크 등록번호를 돌려준다.
 */
@Component
@Slf4j
public class NtsClient {

    private final boolean enabled;
    private final RestClient loginClient;
    private final RestClient actionClient;
    private final String key;
    private final String id;
    private final String password;

    public NtsClient(
            @Value("${ghlove.integrations.nts.enabled}") boolean enabled,
            @Value("${ghlove.integrations.nts.login-url}") String loginUrl,
            @Value("${ghlove.integrations.nts.action-url}") String actionUrl,
            @Value("${ghlove.integrations.nts.key}") String key,
            @Value("${ghlove.integrations.nts.id}") String id,
            @Value("${ghlove.integrations.nts.password}") String password) {
        this.enabled = enabled;
        this.loginClient = RestClient.create(loginUrl);
        this.actionClient = RestClient.create(actionUrl);
        this.key = key;
        this.id = id;
        this.password = password;
    }

    public NtsReceiptResult registerReceipt(Donation donation, Locgov locgov, MemberInfo member) {
        if (!enabled) {
            log.info("[nts] disabled - mock 전자기부금영수증 등록 cntrSn={}", donation.getCntrSn());
            return NtsReceiptResult.mock(donation.getCntrSn());
        }
        try {
            String accessToken = getAccessToken();
            NtsReceiptRequest request = new NtsReceiptRequest(donation.getCntrSn(), member.userName(),
                    member.birthday(), locgov != null ? locgov.getBizrno() : null,
                    donation.getCntrAmt(), donation.getCntrDe());
            return actionClient.post().uri("")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve().body(NtsReceiptResult.class);
        } catch (RestClientException e) {
            throw new DonationException("국세청 전자기부금영수증 등록에 실패했습니다: " + e.getMessage());
        }
    }

    private String getAccessToken() {
        Map<?, ?> response = loginClient.post().uri("")
                .body(Map.of("key", key, "id", id, "password", password))
                .retrieve().body(Map.class);
        Object token = response != null ? response.get("accessToken") : null;
        if (token == null) {
            throw new DonationException("국세청 인증 토큰을 발급받지 못했습니다.");
        }
        return token.toString();
    }
}
