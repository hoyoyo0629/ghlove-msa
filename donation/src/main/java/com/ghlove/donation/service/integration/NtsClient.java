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

    // ==================== AS-IS 배치 전문 (NgDonationBatchServiceImpl.getAccessToken / sendNtsEreceipt*Batch) ====================

    @Value("${ghlove.integrations.nts.relay-login-url}")
    private String relayLoginUrl;
    @Value("${ghlove.integrations.nts.relay-action-url}")
    private String relayActionUrl;
    @Value("${ghlove.integrations.nts.relay-login-ip}")
    private String relayLoginIp;

    public boolean enabled() {
        return enabled;
    }

    /**
     * AS-IS {@code getAccessToken}: {@code {"data":{"id"}, "enc_data":{"pw": RSA(비밀번호)}}}를 로그인 주소로 POST
     * ({@code X-Forwarded-For: loginIp}) → 응답 {@code AccessToken}. 실패하면 null(AS-IS는 "null" 문자열이 된다).
     */
    public String relayAccessToken() {
        try {
            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("data", Map.of("id", id));
            body.put("enc_data", Map.of("pw", rsa(password)));
            Map<?, ?> res = postRelay(relayLoginUrl, body, null);
            return String.valueOf(res.get("AccessToken"));
        } catch (Exception e) {
            log.error("getAccessToken error {}", e.toString());
            return null;
        }
    }

    /**
     * AS-IS 전자기부영수증 1건 전송 - {@code data}(dntDt 수납일, conbCd 43, dntAmt 금액, cnbtSpstCnfrClCd 01) +
     * {@code enc_data}(extrOrgnNtplDscmEncCntn = RSA(CI), conbOrgTxprDscmNoEncCntn = RSA(단체 사업자번호))를 동작 주소로 POST
     * (X-Forwarded-For + Bearer 토큰) → 응답 Map({@code res-code}, {@code res-msg}).
     */
    public Map<?, ?> sendEreceipt(String accessToken, String sttemntPayDe, long taxAmt, String encryptedCi,
                                  String encryptedBizNo) throws Exception {
        return sendEreceipt(accessToken, sttemntPayDe, "43", taxAmt, encryptedCi, encryptedBizNo, null, null);
    }

    /**
     * cntrTaxTempBatch용 - 기부금코드(conbCd: 일반 43 / 특별재난지역 44)를 받고, AS-IS 2024.02 추가 항목
     * {@code elcrAplcd}(전자기부금영수증신청구분 01 발급/02 수정/03 삭제)·{@code elcrAdmNo}(관리번호=전자납부번호)를 싣는다(null이면 안 싣는다).
     */
    public Map<?, ?> sendEreceipt(String accessToken, String sttemntPayDe, String conbCd, long taxAmt, String encryptedCi,
                                  String encryptedBizNo, String elcrAplCd, String elcrAdmNo) throws Exception {
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("dntDt", sttemntPayDe);
        data.put("conbCd", conbCd);
        data.put("dntAmt", (int) taxAmt);
        data.put("cnbtSpstCnfrClCd", "01");
        if (elcrAplCd != null || elcrAdmNo != null) {
            data.put("elcrAplcd", elcrAplCd);
            data.put("elcrAdmNo", elcrAdmNo);
        }
        Map<String, Object> enc = new java.util.LinkedHashMap<>();
        enc.put("extrOrgnNtplDscmEncCntn", encryptedCi);
        enc.put("conbOrgTxprDscmNoEncCntn", encryptedBizNo);
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("data", data);
        body.put("enc_data", enc);
        return postRelay(relayActionUrl, body, accessToken);
    }

    /** AS-IS getRSA - 공개키(Base64 X.509)로 RSA 암호화 후 Base64. 실패하면 빈 문자열. */
    public String rsa(String plain) {
        if (plain == null) {
            return "";
        }
        try {
            java.security.PublicKey publicKey = java.security.KeyFactory.getInstance("RSA").generatePublic(
                    new java.security.spec.X509EncodedKeySpec(java.util.Base64.getDecoder().decode(key.getBytes())));
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            return java.util.Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes()));
        } catch (Exception e) {
            log.error("RSA 암호화 : Exception {}", e.toString());
            return "";
        }
    }

    /** AS-IS restFulToRelayServer - JSON POST(인증서 검증 없음), 국세청 주소면 X-Forwarded-For, 동작이면 Bearer. */
    private Map<?, ?> postRelay(String url, Map<String, Object> body, String accessToken) throws Exception {
        javax.net.ssl.TrustManager[] trustAll = {new javax.net.ssl.X509TrustManager() {
            public void checkClientTrusted(java.security.cert.X509Certificate[] c, String a) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] c, String a) {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        }};
        javax.net.ssl.SSLContext ssl = javax.net.ssl.SSLContext.getInstance("TLSv1.2");
        ssl.init(null, trustAll, new java.security.SecureRandom());
        java.net.http.HttpRequest.Builder req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                .timeout(java.time.Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-Forwarded-For", relayLoginIp)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                        new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)));
        if (accessToken != null) {
            req.header("Authorization", "Bearer " + accessToken);
        }
        java.net.http.HttpResponse<String> res = java.net.http.HttpClient.newBuilder().sslContext(ssl)
                .connectTimeout(java.time.Duration.ofSeconds(30)).build()
                .send(req.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
        return new com.fasterxml.jackson.databind.ObjectMapper().readValue(res.body(), Map.class);
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
