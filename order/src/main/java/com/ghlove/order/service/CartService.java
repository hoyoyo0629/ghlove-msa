package com.ghlove.order.service;

import com.ghlove.order.domain.CartItem;
import com.ghlove.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 장바구니 (AS-IS cart/index.html). 결제는 장바구니 자체를 하나의 주문으로 바꾸지 않고,
 * 선택된 행마다 기존 OrderService.createOrder()를 그대로 호출해 단일품목 주문 SAGA를
 * 재사용한다 (order.saga 계약을 다중품목으로 바꾸지 않기 위한 의도적 설계 - CartLine 하나당
 * Order 하나, "주문 완료"는 여러 개의 개별 주문이 동시에 생성되는 형태).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private static final String STATUS_APPROVED = "APPROVED";
    private static final String SOLD_OUT = "1";

    private final CartItemRepository cartItemRepository;
    private final GiftClient giftClient;
    private final LocgovClient locgovClient;
    private final PointClient pointClient;
    private final OrderService orderService;
    private final CouponService couponService;

    @Value("${ghlove.gift-service.base-url}")
    private String giftServiceBaseUrl;

    @Transactional
    public void add(Long userId, Long itemId, Integer quantity) {
        if (userId == null || userId <= 0) {
            throw new OrderException("회원 ID를 입력해 주세요.");
        }
        int qty = quantity == null || quantity <= 0 ? 1 : quantity;

        GiftItemInfo gift = giftClient.fetch(itemId);
        if (!STATUS_APPROVED.equals(gift.dataStatusCode()) || SOLD_OUT.equals(gift.soldOut())) {
            throw new OrderException("현재 장바구니에 담을 수 없는 답례품입니다.");
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndItemId(userId, itemId).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUserId(userId);
            c.setItemId(itemId);
            c.setQuantity(0);
            c.setCreatedDate(LocalDateTime.now());
            return c;
        });
        cartItem.setQuantity(cartItem.getQuantity() + qty);
        cartItem.setUpdatedDate(LocalDateTime.now());
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new OrderException("수량은 1개 이상이어야 합니다.");
        }
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new OrderException("장바구니 항목을 찾을 수 없습니다."));
        cartItem.setQuantity(quantity);
        cartItem.setUpdatedDate(LocalDateTime.now());
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void remove(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }
        cartItemRepository.deleteByCartItemIdInAndUserId(cartItemIds, userId);
    }

    /** 지자체별로 그룹핑한 장바구니 화면 데이터 (AS-IS displayBuyItems). 품절/미승인으로
     * 더는 주문할 수 없는 항목은 조용히 장바구니에서 제거한다 ("품절되면 자동으로 목록에서
     * 삭제됩니다" 안내문과 동일한 동작). remove()를 같은 클래스 안에서 직접 호출하므로
     * (자기 자신 호출은 Spring AOP 프록시를 거치지 않아 remove()의 @Transactional이
     * 무시된다) 이 메서드 자체도 @Transactional이어야 삭제가 실제 트랜잭션 안에서 실행된다. */
    @Transactional
    public List<CartGroup> view(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedDateDesc(userId);
        if (cartItems.isEmpty()) {
            return List.of();
        }

        Map<String, List<CartLine>> linesByLocgov = new LinkedHashMap<>();
        List<Long> unavailable = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            GiftItemInfo gift;
            try {
                gift = giftClient.fetch(cartItem.getItemId());
            } catch (OrderException e) {
                unavailable.add(cartItem.getCartItemId());
                continue;
            }
            if (!STATUS_APPROVED.equals(gift.dataStatusCode()) || SOLD_OUT.equals(gift.soldOut())) {
                unavailable.add(cartItem.getCartItemId());
                continue;
            }

            long lineTotal = (long) gift.salePrice() * cartItem.getQuantity();
            String thumbnailUrl = gift.thumbnailPath() != null ? giftServiceBaseUrl + gift.thumbnailPath() : null;
            CartLine line = new CartLine(cartItem.getCartItemId(), gift.itemId(), gift.itemName(), thumbnailUrl,
                    cartItem.getQuantity(), gift.salePrice(), lineTotal);

            String locgovCode = gift.locgovCode() != null ? gift.locgovCode() : "";
            linesByLocgov.computeIfAbsent(locgovCode, k -> new ArrayList<>()).add(line);
        }

        if (!unavailable.isEmpty()) {
            remove(userId, unavailable);
        }

        List<CartGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<CartLine>> entry : linesByLocgov.entrySet()) {
            String locgovCode = entry.getKey();
            List<CartLine> lines = entry.getValue();
            long groupTotal = lines.stream().mapToLong(CartLine::lineTotal).sum();
            String locgovNm = locgovCode.isEmpty() ? "지자체 미지정" : locgovClient.nameOf(locgovCode);
            long givePoint = locgovCode.isEmpty() ? 0 : pointClient.balanceByLocgov(userId, locgovCode);
            groups.add(new CartGroup(locgovCode, locgovNm, givePoint, lines, groupTotal));
        }
        return groups;
    }

    /** 주문결제(AS-IS order/step1.html) 화면용 - 선택된 장바구니 행만 남긴 그룹 목록. */
    public List<CartGroup> viewSelected(Long userId, List<Long> selectedCartItemIds) {
        if (selectedCartItemIds == null || selectedCartItemIds.isEmpty()) {
            return List.of();
        }
        List<CartGroup> all = view(userId);
        List<CartGroup> filtered = new ArrayList<>();
        for (CartGroup group : all) {
            List<CartLine> lines = group.lines().stream()
                    .filter(l -> selectedCartItemIds.contains(l.cartItemId()))
                    .toList();
            if (!lines.isEmpty()) {
                long groupTotal = lines.stream().mapToLong(CartLine::lineTotal).sum();
                filtered.add(new CartGroup(group.locgovCode(), group.locgovNm(), group.givePoint(), lines, groupTotal));
            }
        }
        return filtered;
    }

    /**
     * 선택된 장바구니 행들을 주문으로 전환한다. AS-IS는 지자체별 포인트가 부족하면 그
     * 그룹 전체를 "포인트가 부족합니다" 알림으로 막고 아무것도 진행하지 않는다 - 여기서도
     * 동일하게, 부족한 그룹이 하나라도 있으면 아무 주문도 만들지 않고 예외를 던진다.
     * couponIssueByCartItem은 장바구니행ID -> 적용할 쿠폰발급ID(OP_COUPON_USER.COUPON_USER_ID),
     * 선택 안 한 행은 맵에 없거나 null이면 쿠폰 미적용. 포인트 부족 판정은 할인 반영 후
     * 금액(lineTotal - discount) 기준으로 한다.
     */
    @Transactional
    public List<String> checkout(Long userId, List<Long> selectedCartItemIds, OrderService.DeliveryInfo deliveryInfo,
                                  Map<Long, Integer> couponIssueByCartItem) {
        if (selectedCartItemIds == null || selectedCartItemIds.isEmpty()) {
            throw new OrderException("답례품을 선택해 주세요.");
        }
        List<CartItem> selected = cartItemRepository.findByCartItemIdInAndUserId(selectedCartItemIds, userId);
        if (selected.isEmpty()) {
            throw new OrderException("답례품을 선택해 주세요.");
        }

        record Line(CartItem cartItem, GiftItemInfo gift, long lineTotal, Integer couponIssueId, long payable) {
        }
        Map<String, List<Line>> byLocgov = new LinkedHashMap<>();
        for (CartItem cartItem : selected) {
            GiftItemInfo gift = giftClient.fetch(cartItem.getItemId());
            if (!STATUS_APPROVED.equals(gift.dataStatusCode()) || SOLD_OUT.equals(gift.soldOut())) {
                throw new OrderException("'" + gift.itemName() + "'은(는) 현재 주문할 수 없습니다.");
            }
            String locgovCode = gift.locgovCode();
            long lineTotal = (long) gift.salePrice() * cartItem.getQuantity();
            Integer couponIssueId = couponIssueByCartItem != null ? couponIssueByCartItem.get(cartItem.getCartItemId()) : null;
            long discount = couponIssueId != null
                    ? couponService.previewDiscount(userId, couponIssueId, gift.itemId(), lineTotal, cartItem.getQuantity())
                    : 0;
            byLocgov.computeIfAbsent(locgovCode, k -> new ArrayList<>())
                    .add(new Line(cartItem, gift, lineTotal, couponIssueId, lineTotal - discount));
        }

        for (Map.Entry<String, List<Line>> entry : byLocgov.entrySet()) {
            String locgovCode = entry.getKey();
            long groupTotal = entry.getValue().stream().mapToLong(Line::payable).sum();
            long givePoint = pointClient.balanceByLocgov(userId, locgovCode);
            if (groupTotal > givePoint) {
                String locgovNm = locgovClient.nameOf(locgovCode);
                throw new OrderException(locgovNm + "의 포인트가 부족합니다.");
            }
        }

        List<String> orderIds = new ArrayList<>();
        for (Line line : byLocgov.values().stream().flatMap(List::stream).toList()) {
            var order = orderService.createOrder(userId, line.cartItem().getItemId(), line.cartItem().getQuantity(),
                    deliveryInfo, line.couponIssueId());
            orderIds.add(order.getOrderId());
        }
        remove(userId, selectedCartItemIds);
        return orderIds;
    }
}
