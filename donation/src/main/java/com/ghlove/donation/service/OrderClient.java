package com.ghlove.donation.service;

import com.ghlove.donation.service.OffgiveService.GiftLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * 오프라인기부접수(기탁서 등록) 대표답례품 주문 생성 - order 서비스의
 * {@code POST /api/admin/offgive-orders}(AS-IS saveOffGiveOrder 이식, '입금대기' 주문)를 호출한다.
 * AS-IS {@code OffgiveServiceImpl.insertOffgive}가 {@code orderService.saveOffGiveOrder()}를
 * 직접 호출하던 자리를 cross-service API 호출로 대체한다. 기부일련번호(AS-IS linkMngKey → FILLER9)를
 * 같이 넘겨, 수납완료 뒤 order가 그 기부의 주문을 찾아 결제처리한다.
 */
@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${ghlove.order-service.base-url}") String orderServiceBaseUrl,
                        @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(orderServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public record GiftLineDto(Long itemId, Long itemOptionId, Integer quantity) {
        static GiftLineDto of(GiftLine line) {
            return new GiftLineDto(line.itemId(), line.itemOptionId(), line.quantity());
        }
    }

    public record OffgiveOrderRequest(Long userId, String cntrSn, List<GiftLineDto> items, String receiverName,
                                       String receiverPhone, String deliveryAddress,
                                       String deliveryAddressDetail, String zipcode) {
    }

    /**
     * @param cntrSn  기부일련번호 - AS-IS giveOrderVo.setLinkMngKey(offgive.getLinkMngKey()) → FILLER9
     * @param zipcode AS-IS receiver.setReceiveZipcode(offgive.getRcvPost()) - 배송지 우편번호
     */
    public void createOffgiveOrder(Long userId, String cntrSn, List<GiftLine> items, String receiverName,
                                    String receiverPhone, String deliveryAddress, String deliveryAddressDetail,
                                    String zipcode) {
        OffgiveOrderRequest request = new OffgiveOrderRequest(userId, cntrSn,
                items.stream().map(GiftLineDto::of).toList(), receiverName, receiverPhone, deliveryAddress,
                deliveryAddressDetail, zipcode);
        try {
            restClient.post().uri("/api/admin/offgive-orders").body(request).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new DonationException(extractMessage(e, "답례품 주문 생성에 실패했습니다."));
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
}
