package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.ShopConfig;
import com.ghlove.admin.repository.CommonCodeRepository;
import com.ghlove.admin.repository.ShopConfigRepository;
import com.ghlove.admin.service.integration.SmartDeliveryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 배치 autoSmartDeliveryShippingCompleteBatch(배송 완료 처리(스마트택배), AS-IS 23:30) - AS-IS
 * {@code JobServiceImpl.autoSmartDeliveryShippingCompleteBatch} + {@code SmartDeliveryBatchServiceImpl}.
 * 스마트택배 기준일(SMART_DELIVERY_CRITERIA_DATE) 지난 배송중 주문의 송장을 스마트택배로 조회해 배송완료(level 6)면
 * 배송완료로 바꾼다. 택배사 → 스마트택배 코드는 공통코드 SMART_DELIVERY_COMPANY(MAPPING_CODE = 배송업체 ID)로 찾고,
 * 코드가 없거나 송장번호에 숫자가 없으면 건너뛴다(AS-IS 그대로). 스마트택배 연계·코드표가 admin 소유라 admin이 돌리고
 * 주문 대상 조회·상태 변경은 order 내부 API로 한다.
 */
@Service
@Slf4j
public class SmartDeliveryBatchService {

    private final ShopConfigRepository shopConfigRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final SmartDeliveryClient smartDeliveryClient;
    private final RestClient order;

    public record SmartDeliveryTarget(String orderId, String carrierCode, String invoiceNo) {
    }

    public SmartDeliveryBatchService(ShopConfigRepository shopConfigRepository, CommonCodeRepository commonCodeRepository,
                                     SmartDeliveryClient smartDeliveryClient,
                                     @Value("${ghlove.order-service.base-url}") String orderBaseUrl,
                                     @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.shopConfigRepository = shopConfigRepository;
        this.commonCodeRepository = commonCodeRepository;
        this.smartDeliveryClient = smartDeliveryClient;
        this.order = RestClient.builder().baseUrl(orderBaseUrl).defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public String run() {
        String daysValue = shopConfigRepository.findById(ShopConfig.SHOP_CONFIG_ID)
                .map(ShopConfig::getSmartDeliveryCriteriaDate).orElse(null);
        int days;
        try {
            days = Integer.parseInt(Objects.requireNonNull(daysValue).trim());
        } catch (RuntimeException e) {
            throw new IllegalStateException("SmartDelivery 연동 기준일이 설정되지 않았습니다(사이트 설정 > 배송): " + daysValue);
        }
        List<SmartDeliveryTarget> targets = order.get()
                .uri(u -> u.path("/api/admin/orders/batch/smart-delivery-targets").queryParam("days", days).build())
                .retrieve().body(new ParameterizedTypeReference<List<SmartDeliveryTarget>>() {
                });
        if (targets == null) {
            targets = List.of();
        }
        // AS-IS getSmartDeliveryCode: OP_DELIVERY_COMPANY ⋈ OP_COMMON_CODE(SMART_DELIVERY_COMPANY, MAPPING_CODE = 배송업체 ID)
        Map<String, String> smartCodeByCompany = commonCodeRepository.findByCodeTypeOrderByOrdering("SMART_DELIVERY_COMPANY")
                .stream().filter(c -> c.getMappingCode() != null && !c.getMappingCode().isBlank())
                .collect(Collectors.toMap(CommonCode::getMappingCode, CommonCode::getId, (a, b) -> a));

        int successCount = 0;
        for (SmartDeliveryTarget t : targets) {
            String carrierCode = t.carrierCode() == null ? null : smartCodeByCompany.get(t.carrierCode());
            if (carrierCode == null) {
                continue;
            }
            String invoice = t.invoiceNo().replaceAll("[^0-9]", "");
            if (invoice.isBlank()) {
                continue;
            }
            Map<String, Object> result = smartDeliveryClient.trackingInfo(carrierCode, invoice);
            if (!result.isEmpty() && !result.containsKey("status")) {
                // level 1:배송준비중, 2:집화완료, 3:배송중, 4:지점도착, 5:배송출발, 6:배송완료
                if (Integer.parseInt(String.valueOf(result.get("level"))) == 6) {
                    order.post().uri("/api/admin/orders/batch/{orderId}/delivered", t.orderId()).retrieve().toBodilessEntity();
                    successCount++;
                }
            } else {
                log.error("[스마트택배 ERROR] :invoice = {}, code = {}, msg = {}", invoice, result.get("code"), result.get("msg"));
            }
        }
        log.info("[스마트택배 완료] SUCCESS COUNT {} 건, FAIL COUNT {} 건 ", successCount, targets.size() - successCount);
        return "대상 " + targets.size() + "건 / 배송완료 " + successCount + "건";
    }
}
