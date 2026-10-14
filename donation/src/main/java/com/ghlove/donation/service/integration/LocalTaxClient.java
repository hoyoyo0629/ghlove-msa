package com.ghlove.donation.service.integration;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.DonationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 세외수입 부과/수납 연계 (AS-IS NgDonationRelayServiceImpl - 서울시는 이택스,
 * 그 외 지자체는 위택스/지방세외수입시스템으로 갈라진다).
 *
 * <p><b>기탁서 등록(15102) 부과요청</b>({@link #sendNextBuga}/{@link #sendSeoulBuga})은 AS-IS 통신을
 * 그대로 옮겼다 - 실제 연계 주소로 JSON을 POST하고(차세대는 AS-IS restFulToRelayServerHttps처럼
 * 인증서 검증 없이 HTTPS), local 프로파일(AS-IS {@code ServiceType.LOCAL})일 때만 AS-IS LOCAL 분기와
 * 같은 가짜 응답을 만든다.
 *
 * <p>{@link #registerLevy}/{@link #confirmPayment}는 기부 완료처리가 쓰는 TO-BE 모크 경로다
 * ({@code ghlove.integrations.local-tax.enabled}, 납부게이트웨이 이식 라운드 대기 - AS-IS 로직 아님).
 */
@Component
@Slf4j
public class LocalTaxClient {

    private static final String SEOUL_PREFIX = "11";

    private static final java.time.format.DateTimeFormatter FAKE_EPAY_TS =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /** AS-IS TIMEOUT_VALUE(10초) / setReadTimeout(30000). */
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    private final boolean enabled;
    private final boolean serviceTypeLocal;
    private final RestClient seoulClient;
    private final RestClient contryClient;
    private final String nextBugaRequestUrl;
    private final String seoulBugaUrl;
    private final String linkTrgtCd;

    public LocalTaxClient(
            Environment environment,
            @Value("${ghlove.integrations.local-tax.enabled}") boolean enabled,
            @Value("${ghlove.integrations.local-tax.seoul-base-url}") String seoulBaseUrl,
            @Value("${ghlove.integrations.local-tax.contry-base-url}") String contryBaseUrl,
            @Value("${ghlove.integrations.local-tax.next-buga-request-url}") String nextBugaRequestUrl,
            @Value("${ghlove.integrations.local-tax.seoul-buga-url}") String seoulBugaUrl,
            @Value("${ghlove.integrations.local-tax.link-trgt-cd}") String linkTrgtCd) {
        this.enabled = enabled;
        this.serviceTypeLocal = environment.acceptsProfiles(Profiles.of("local"));
        this.seoulClient = RestClient.create(seoulBaseUrl);
        this.contryClient = RestClient.create(contryBaseUrl);
        this.nextBugaRequestUrl = nextBugaRequestUrl;
        this.seoulBugaUrl = seoulBugaUrl;
        this.linkTrgtCd = linkTrgtCd;
    }

    public boolean enabled() {
        return enabled;
    }

    /** AS-IS {@code ServiceType.LOCAL} - local 프로파일이면 부과요청 연계를 호출하지 않는다. */
    public boolean serviceTypeLocal() {
        return serviceTypeLocal;
    }

    /** AS-IS 상수 LINK_TRGT_CD(연계대상코드) - 설정으로 뺐다. */
    public String linkTrgtCd() {
        return linkTrgtCd;
    }

    /** 서울 세외 부과요청 응답 (AS-IS는 enapbuNo만 쓰고, 오류는 gif_seoul에 쌓인다). */
    public record SeoulResponse(String enapbuNo, String errorCd, String errorMsg) {
    }

    /**
     * 지방세외수입 차세대 부과요청 (AS-IS {@code nextBugaRequest}의 통신 부분) - 응답 JSON을 그대로
     * Map으로 돌려준다(AS-IS도 {@code SERVICE_RELAY_RESULT.putAll(jsonObject)}로 응답 키를 전부 넘긴다).
     *
     * <p>local 프로파일(AS-IS {@code ServiceType.LOCAL})이면 <b>AS-IS LOCAL 분기와 같은 가짜 응답</b> -
     * 전자납부번호 {@code "99" + yyyyMMddHHmmssSSS}, {@code linkRstCd="000"},
     * {@code linkRstMsg="LOCAL FAKE : {전자납부번호}"}.
     */
    public Map<String, Object> sendNextBuga(NextBugaRequest request) throws IOException {
        if (serviceTypeLocal) {
            String fakeEpayNo = "99" + java.time.LocalDateTime.now().format(FAKE_EPAY_TS);
            log.warn("[LOCAL DEV] nextBugaRequest relay skipped, fake epayNo {}", fakeEpayNo);
            Map<String, Object> fake = new LinkedHashMap<>();
            fake.put("linkRstCd", "000");
            fake.put("linkRstMsg", "LOCAL FAKE : " + fakeEpayNo);
            // 실연계 응답은 요청의 연계관리키를 돌려준다 - AS-IS 화면이 그 값을 받아(기탁서 #linkMngKey,
            // 사용자 사이트 op.donation.js response.result.linkMngKey) 기부일련번호로 쓴다. AS-IS LOCAL 가짜
            // 응답엔 이 키가 없어 로컬 기탁서 등록이 빈 기부일련번호로 들어갔다 - 실응답과 같게 실어 준다.
            fake.put("linkMngKey", request.linkMngKey());
            return fake;
        }
        return parseJson(postJson(nextBugaRequestUrl, request.toPayload()));
    }

    /**
     * 서울시 세외수입 부과정보 등록 (AS-IS {@code OffgiveManagerController.getSdonationCharge}의 통신
     * 부분) - AS-IS는 응답의 {@code enapbuNo}만 쓴다. local 프로파일이면 AS-IS 서울 부과요청 LOCAL
     * 분기(sntrBugaInsert)와 같은 가짜 응답(errorCode "0", 전자납부번호 같은 규칙)을 만든다.
     */
    public SeoulResponse sendSeoulBuga(SeoulBugaRequest request) throws IOException {
        if (serviceTypeLocal) {
            String fakeEnapbuNo = "99" + java.time.LocalDateTime.now().format(FAKE_EPAY_TS);
            log.warn("[LOCAL DEV] getSdonationCharge relay skipped, fake enapbuNo {}", fakeEnapbuNo);
            return new SeoulResponse(fakeEnapbuNo, "0", "");
        }
        Map<String, Object> body = parseJson(postJson(seoulBugaUrl, request.toPayload()));
        // AS-IS String.valueOf(jsonObject.get("enapbuNo")) - 값이 없으면 문자열 "null"이 된다(그대로 둔다)
        return new SeoulResponse(String.valueOf(body.get("enapbuNo")), str(body.get("errorCode")),
                str(body.get("errorMsg")));
    }

    /**
     * AS-IS restFulToRelayServer(Https) - JSON POST(연결 10초/읽기 30초), HTTPS면 AS-IS
     * SSLSocketFactoryMaker처럼 인증서·호스트명 검증 없이 붙는다(내부 연계서버 사설 인증서).
     */
    private static String postJson(String url, Map<String, Object> payload) throws IOException {
        HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
        try {
            if (http instanceof HttpsURLConnection https) {
                https.setSSLSocketFactory(trustAllSocketFactory());
                https.setHostnameVerifier((hostname, session) -> true);
            }
            http.setConnectTimeout(CONNECT_TIMEOUT);
            http.setReadTimeout(READ_TIMEOUT);
            http.setDefaultUseCaches(false);
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            try (OutputStreamWriter osw = new OutputStreamWriter(http.getOutputStream(), StandardCharsets.UTF_8)) {
                osw.write(JSON.writeValueAsString(payload));
                osw.flush();
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } finally {
            http.disconnect();
        }
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON = new com.fasterxml.jackson.databind.ObjectMapper();

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String response) throws IOException {
        Map<String, Object> body = JSON.readValue(response, LinkedHashMap.class);
        return body != null ? body : new LinkedHashMap<>();
    }

    private static SSLSocketFactory trustAllSocketFactory() throws IOException {
        try {
            TrustManager[] trustAll = {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAll, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new IOException("SSL 초기화 실패", e);
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
