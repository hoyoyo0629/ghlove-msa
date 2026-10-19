package com.ghlove.admin.web;

import com.ghlove.admin.domain.ShopConfig;
import com.ghlove.admin.repository.ShopConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 배치가 읽는 쇼핑몰 설정(내부 전용) - AS-IS 배치는 {@code configMapper.getShopConfig(SHOP_CONFIG_ID)}로 OP_CONFIG를 직접 읽었다.
 * MSA에서는 OP_CONFIG가 admin 소유라 order 배치(배송완료·구매확정·스마트택배)가 이 API로 기준일을 가져간다.
 * 값은 "사이트 설정 > 배송" 화면이 저장한 그대로(문자열, 일 수).
 */
@RestController
@RequiredArgsConstructor
public class ShopConfigInternalApiController {

    private final ShopConfigRepository shopConfigRepository;

    @Value("${ghlove.internal.admin-secret}")
    private String adminSecret;

    public record BatchDays(String shippingCompleteDate, String confirmPurchaseDate, String smartDeliveryCriteriaDate) {
    }

    @GetMapping("/api/admin/shop-config/batch-days")
    public BatchDays batchDays(@RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        if (secret == null || !secret.equals(adminSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        ShopConfig c = shopConfigRepository.findById(ShopConfig.SHOP_CONFIG_ID).orElse(new ShopConfig());
        return new BatchDays(c.getShippingCompleteDate(), c.getConfirmPurchaseDate(), c.getSmartDeliveryCriteriaDate());
    }
}
