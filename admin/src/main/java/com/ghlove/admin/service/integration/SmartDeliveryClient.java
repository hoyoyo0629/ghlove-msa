package com.ghlove.admin.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 스마트택배(sweettracker) 배송조회 연계 (AS-IS SmartDeliveryServiceImpl,
 * SmartDeliveryBatchServiceImpl). SFR-006이 "택배사 시스템과의 연계는 운영관리
 * 서비스가 담당"이라 명시하므로 order가 아니라 여기(admin)에 둔다 - order는
 * 배송 상태값만 갖고, 실시간 조회는 운영자가 이 화면에서 확인한다.
 * enabled=false(방화벽 미개방)면 모크 추적정보를 돌려준다.
 */
@Component
@Slf4j
public class SmartDeliveryClient {

    private final boolean enabled;
    private final RestClient restClient;
    private final String apiKey;
    private final String trackingInfoUrl;

    public SmartDeliveryClient(
            @Value("${ghlove.integrations.smart-delivery.enabled}") boolean enabled,
            @Value("${ghlove.integrations.smart-delivery.api-url}") String apiUrl,
            @Value("${ghlove.integrations.smart-delivery.api-key}") String apiKey,
            @Value("${ghlove.integrations.smart-delivery.tracking-info-url}") String trackingInfoUrl) {
        this.enabled = enabled;
        this.restClient = RestClient.create(apiUrl);
        this.apiKey = apiKey;
        this.trackingInfoUrl = trackingInfoUrl;
    }

    /**
     * 배치 autoSmartDeliveryShippingCompleteBatch용 배송조회 - AS-IS {@code SmartDeliveryServiceImpl.delivery}:
     * {@code {sweettracker.url}?t_code=&t_invoice=&t_key=}를 GET(내부 연계서버, 인증서 검증 없이)해 응답 JSON 키를 그대로 Map으로.
     * 응답의 {@code level}(1 배송준비중 ~ 6 배송완료)로 배치가 판정하고, {@code status} 키가 있으면 오류 응답이다.
     * 실패하면 빈 Map(AS-IS 그대로 - 배치는 아무것도 하지 않는다). enabled=false면 호출 없이 빈 Map.
     */
    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> trackingInfo(String carrierCode, String invoiceNo) {
        if (!enabled) {
            log.info("[smart-delivery] disabled - 배송조회 생략 carrier={} invoice={}", carrierCode, invoiceNo);
            return java.util.Map.of();
        }
        String url = String.format("%s?t_code=%s&t_invoice=%s&t_key=%s", trackingInfoUrl,
                carrierCode == null ? "" : carrierCode, invoiceNo == null ? "" : invoiceNo, apiKey);
        try {
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
            java.net.http.HttpClient http = java.net.http.HttpClient.newBuilder().sslContext(ssl)
                    .connectTimeout(java.time.Duration.ofSeconds(10)).build();
            java.net.http.HttpResponse<String> response = http.send(
                    java.net.http.HttpRequest.newBuilder(java.net.URI.create(url)).timeout(java.time.Duration.ofSeconds(30)).GET().build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(response.body(), java.util.Map.class);
        } catch (Exception e) {
            log.error("■■■API■■■ deliveryRequest EXCEPTION : {} {}", invoiceNo, e.toString());
            return java.util.Map.of();
        }
    }

    public DeliveryTrackingResult track(String carrierCode, String invoiceNo) {
        if (!enabled) {
            log.info("[smart-delivery] disabled - mock 배송조회 carrier={} invoice={}", carrierCode, invoiceNo);
            return DeliveryTrackingResult.mock(carrierCode, invoiceNo);
        }
        try {
            return restClient.get()
                    .uri("/api/v1/trackingInfo?t_key={key}&t_code={carrier}&t_invoice={invoice}",
                            apiKey, carrierCode, invoiceNo)
                    .retrieve().body(DeliveryTrackingResult.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("배송 조회에 실패했습니다: " + e.getMessage());
        }
    }
}
