package com.ghlove.order.service;

import com.ghlove.order.domain.CartItem;
import com.ghlove.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 장바구니 (AS-IS cart/index.html). 결제는 장바구니 자체를 하나의 주문으로 바꾸지 않고,
 * 선택된 행마다 기존 OrderService.createOrder()를 그대로 호출해 단일품목 주문 SAGA를
 * 재사용한다 (order.saga 계약을 다중품목으로 바꾸지 않기 위한 의도적 설계 - CartLine 하나당
 * Order 하나, "주문 완료"는 여러 개의 개별 주문이 동시에 생성되는 형태).
 *
 * <p>그 설계 때문에 체크아웃은 "주문 N건을 만드는 도중 K번째에서 실패"할 수 있다. 그래서
 * 주문을 하나라도 만들기 전에 {@link #priceSelected}가 모든 줄을 한 번에 검증·확정하고,
 * 화면 금액도 같은 계산 결과({@link #quote})를 그대로 쓴다 - 화면에 보여준 결제 포인트와
 * 실제 차감액(POINT_AMOUNT)이 어긋날 수 없다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private static final String STATUS_APPROVED = "APPROVED";
    private static final String SOLD_OUT = "1";

    /** 한 답례품을 장바구니에 담을 수 있는 최대 수량 - 재고와 별개로 명백한 오입력을 막는 상한. */
    static final int MAX_QUANTITY = 999;

    private final CartItemRepository cartItemRepository;
    private final GiftClient giftClient;
    private final LocgovClient locgovClient;
    private final PointClient pointClient;
    private final OrderService orderService;
    private final CouponService couponService;

    @Value("${ghlove.gift-service.base-url}")
    private String giftServiceBaseUrl;

    // ==================== 담기 / 수량변경 / 삭제 ====================

    /**
     * 답례품 상세의 "장바구니에 담기". 트랜잭션을 열지 않는다 - 답례품 조회(HTTP)를 DB
     * 커넥션을 쥔 채로 하지 않기 위해서이고, UNIQUE(USER_ID, ITEM_ID) 경합으로 저장이
     * 실패했을 때 "이미 커밋된 남의 행"을 다시 읽어 합산 저장하려면 실패한 트랜잭션이
     * 먼저 끝나 있어야 하기 때문이다 (같은 트랜잭션 안에서는 rollback-only가 되어 재시도 불가).
     */
    public void add(Long userId, Long itemId, Integer quantity) {
        if (userId == null || userId <= 0) {
            throw new OrderException("회원 ID를 입력해 주세요.");
        }
        int qty = quantity == null || quantity <= 0 ? 1 : quantity;

        GiftItemInfo gift = giftClient.fetch(itemId);
        requireOrderable(gift, "현재 장바구니에 담을 수 없는 답례품입니다.");

        try {
            addOrAccumulate(userId, gift, qty);
        } catch (DataIntegrityViolationException e) {
            // 같은 답례품을 동시에 담아 UNIQUE(USER_ID, ITEM_ID)가 충돌한 경우 - 상대가
            // 방금 만든 행을 다시 읽어 수량을 합산한다 (예전에는 그대로 500으로 나갔다).
            log.info("Cart add raced on unique(userId={}, itemId={}) - retrying as accumulate", userId, gift.itemId());
            addOrAccumulate(userId, gift, qty);
        }
    }

    private void addOrAccumulate(Long userId, GiftItemInfo gift, int addQuantity) {
        CartItem cartItem = cartItemRepository.findByUserIdAndItemId(userId, gift.itemId()).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUserId(userId);
            c.setItemId(gift.itemId());
            c.setQuantity(0);
            c.setCreatedDate(LocalDateTime.now());
            return c;
        });
        int newQuantity = (cartItem.getQuantity() == null ? 0 : cartItem.getQuantity()) + addQuantity;
        requireStock(gift, newQuantity);
        cartItem.setQuantity(newQuantity);
        cartItem.setUpdatedDate(LocalDateTime.now());
        cartItemRepository.save(cartItem);
    }

    /** 수량 변경 - 결제 버튼까지 미루지 않고 이 시점에 재고/상한을 검증한다. */
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new OrderException("수량은 1개 이상이어야 합니다.");
        }
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new OrderException("장바구니 항목을 찾을 수 없습니다."));

        GiftItemInfo gift = giftClient.fetch(cartItem.getItemId());
        requireOrderable(gift, "현재 주문할 수 없는 답례품입니다.");
        requireStock(gift, quantity);

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedDate(LocalDateTime.now());
        cartItemRepository.save(cartItem);
    }

    public void remove(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }
        cartItemRepository.deleteByCartItemIdInAndUserId(cartItemIds, userId);
    }

    private void requireOrderable(GiftItemInfo gift, String message) {
        if (!STATUS_APPROVED.equals(gift.dataStatusCode()) || SOLD_OUT.equals(gift.soldOut())) {
            throw new OrderException(message);
        }
    }

    private void requireStock(GiftItemInfo gift, int quantity) {
        if (quantity > MAX_QUANTITY) {
            throw new OrderException("한 답례품은 최대 " + MAX_QUANTITY + "개까지 담을 수 있습니다.");
        }
        int stock = gift.stockQuantity() == null ? 0 : gift.stockQuantity();
        if (stock < quantity) {
            throw new OrderException("재고가 부족합니다. (현재 재고: " + stock + ")");
        }
    }

    // ==================== 조회 ====================

    /**
     * 지자체별로 그룹핑한 장바구니 화면 데이터 (AS-IS displayBuyItems).
     *
     * <p>더 이상 @Transactional이 아니다 - 항목 수만큼의 답례품 조회(HTTP)와 그룹 수만큼의
     * 지자체명/포인트 조회를 DB 커넥션을 잡은 채로 하고 있었다. 품절 항목 자동 삭제는
     * CartItemRepository.deleteByCartItemIdInAndUserId가 직접 트랜잭션을 연다.
     */
    public List<CartGroup> view(Long userId) {
        return buildGroups(userId, cartItemRepository.findByUserIdOrderByCreatedDateDesc(userId));
    }

    /**
     * 주문결제(AS-IS order/step1.html) 화면용 - 선택된 장바구니 행만 조회한다. 예전에는
     * view()로 장바구니 전체를 조회(= 전 항목 HTTP 조회)한 뒤 걸러냈다.
     */
    public List<CartGroup> viewSelected(Long userId, List<Long> selectedCartItemIds) {
        if (selectedCartItemIds == null || selectedCartItemIds.isEmpty()) {
            return List.of();
        }
        return buildGroups(userId,
                cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(selectedCartItemIds, userId));
    }

    /**
     * 품절/미승인으로 더는 주문할 수 없는 항목은 조용히 장바구니에서 제거한다 ("품절되면
     * 자동으로 목록에서 삭제됩니다" 안내문과 동일한 동작). 배송비는 배송지를 아직 모르는
     * 단계라 제주/도서산간 추가배송비 없이 기본 정책만 적용해 보여준다 - 주문결제 화면에서
     * 주소를 입력하면 {@link #quote}가 추가배송비까지 반영한 금액으로 다시 내려준다.
     */
    private List<CartGroup> buildGroups(Long userId, List<CartItem> cartItems) {
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
            long deliveryFee = orderService.deliveryFeeOf(gift, cartItem.getQuantity(), lineTotal, null);
            String thumbnailUrl = gift.thumbnailPath() != null ? giftServiceBaseUrl + gift.thumbnailPath() : null;
            CartLine line = new CartLine(cartItem.getCartItemId(), gift.itemId(), gift.itemName(), thumbnailUrl,
                    cartItem.getQuantity(), gift.salePrice(), lineTotal, deliveryFee, lineTotal + deliveryFee);

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
            long groupDeliveryFee = lines.stream().mapToLong(CartLine::deliveryFee).sum();
            String locgovNm = locgovCode.isEmpty() ? "지자체 미지정" : locgovClient.nameOf(locgovCode);
            long givePoint = locgovCode.isEmpty() ? 0 : pointClient.balanceByLocgov(userId, locgovCode);
            groups.add(new CartGroup(locgovCode, locgovNm, givePoint, lines, groupTotal, groupDeliveryFee,
                    groupTotal + groupDeliveryFee));
        }
        return groups;
    }

    // ==================== 결제금액 산정 (미리보기 = 실제 결제) ====================

    /** 체크아웃 한 줄의 확정 금액. payable = lineTotal - discount + deliveryFee 로,
     *  OrderService가 주문에 저장하는 POINT_AMOUNT와 정확히 같은 식이다. */
    public record CheckoutLine(Long cartItemId, Long itemId, String itemName, Integer quantity,
                                long lineTotal, long discount, long deliveryFee, long payable) {
    }

    /** 지자체 그룹 - 포인트 부족(주문불가) 판정은 배송비까지 더한 groupPayable 기준이다
     *  (groupTotal 기준으로 판정하면 배송비 때문에 SAGA에서 포인트 차감이 실패한다). */
    public record CheckoutGroup(String locgovCode, String locgovNm, long givePoint, List<CheckoutLine> lines,
                                 long groupTotal, long groupDiscount, long groupDeliveryFee, long groupPayable) {
    }

    public record CheckoutQuote(List<CheckoutGroup> groups, long totalPoint, long totalDiscount,
                                 long totalDeliveryFee, long totalPayable) {
    }

    /** 검증까지 마친 한 줄 - 주문 생성에 필요한 원자료(장바구니행/답례품)를 그대로 들고 있어
     *  createOrder가 답례품을 다시 조회하지 않는다. */
    private record PricedLine(CartItem cartItem, GiftItemInfo gift, Integer couponIssueId,
                               long lineTotal, long discount, long deliveryFee, long payable) {
    }

    /**
     * 주문결제 화면의 실시간 금액 미리보기 - 쿠폰을 고르거나 배송지를 입력할 때마다
     * 호출한다. 실제 결제({@link #checkout})와 완전히 같은 {@link #priceSelected}를 쓰므로
     * 화면에 뜬 금액이 곧 차감될 포인트다.
     */
    public CheckoutQuote quote(Long userId, List<Long> selectedCartItemIds,
                                Map<Long, Integer> couponIssueByCartItem, String deliveryAddress) {
        return toQuote(userId, priceSelected(userId, selectedCartItemIds, couponIssueByCartItem, deliveryAddress));
    }

    /**
     * 선택된 장바구니 행 전체를 한 번에 검증하고 금액을 확정한다. 주문을 단 한 건도 만들기
     * 전에 실패 사유(품절/미승인/재고부족/쿠폰 중복선택/사용불가 쿠폰)를 여기서 모두 던지기
     * 때문에, "N건 중 K번째에서 실패해 앞선 주문만 이벤트가 나가는" 부분 체크아웃이 없다.
     */
    private List<PricedLine> priceSelected(Long userId, List<Long> selectedCartItemIds,
                                            Map<Long, Integer> couponIssueByCartItem, String deliveryAddress) {
        if (selectedCartItemIds == null || selectedCartItemIds.isEmpty()) {
            throw new OrderException("답례품을 선택해 주세요.");
        }
        Set<Long> requested = new LinkedHashSet<>(selectedCartItemIds);
        List<CartItem> selected =
                cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.copyOf(requested), userId);
        if (selected.isEmpty()) {
            throw new OrderException("답례품을 선택해 주세요.");
        }
        if (selected.size() != requested.size()) {
            throw new OrderException("장바구니에서 찾을 수 없는 답례품이 있습니다. 장바구니를 다시 확인해 주세요.");
        }

        // 같은 쿠폰을 두 줄에 고르면 첫 주문이 쿠폰을 사용처리한 뒤 두 번째 줄에서
        // "사용할 수 없는 쿠폰입니다"로 터진다 - 주문을 만들기 전에 여기서 막는다.
        Set<Integer> usedCoupons = new HashSet<>();
        List<PricedLine> priced = new ArrayList<>();

        for (CartItem cartItem : selected) {
            GiftItemInfo gift = giftClient.fetch(cartItem.getItemId());
            if (!STATUS_APPROVED.equals(gift.dataStatusCode()) || SOLD_OUT.equals(gift.soldOut())) {
                throw new OrderException("'" + gift.itemName() + "'은(는) 현재 주문할 수 없습니다.");
            }
            int quantity = cartItem.getQuantity();
            int stock = gift.stockQuantity() == null ? 0 : gift.stockQuantity();
            if (stock < quantity) {
                throw new OrderException("'" + gift.itemName() + "'의 재고가 부족합니다. (현재 재고: " + stock + ")");
            }

            Integer couponIssueId =
                    couponIssueByCartItem != null ? couponIssueByCartItem.get(cartItem.getCartItemId()) : null;
            if (couponIssueId != null && !usedCoupons.add(couponIssueId)) {
                throw new OrderException("같은 쿠폰을 여러 답례품에 사용할 수 없습니다.");
            }

            long lineTotal = (long) gift.salePrice() * quantity;
            long discount = couponIssueId != null
                    ? couponService.previewDiscount(userId, couponIssueId, gift.itemId(), lineTotal, quantity)
                    : 0;
            long deliveryFee = orderService.deliveryFeeOf(gift, quantity, lineTotal, deliveryAddress);
            priced.add(new PricedLine(cartItem, gift, couponIssueId, lineTotal, discount, deliveryFee,
                    lineTotal - discount + deliveryFee));
        }
        return priced;
    }

    private CheckoutQuote toQuote(Long userId, List<PricedLine> priced) {
        Map<String, List<PricedLine>> byLocgov = groupByLocgov(priced);
        List<CheckoutGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<PricedLine>> entry : byLocgov.entrySet()) {
            String locgovCode = entry.getKey();
            List<PricedLine> lines = entry.getValue();
            groups.add(new CheckoutGroup(locgovCode,
                    locgovCode.isEmpty() ? "지자체 미지정" : locgovClient.nameOf(locgovCode),
                    locgovCode.isEmpty() ? 0 : pointClient.balanceByLocgov(userId, locgovCode),
                    lines.stream().map(l -> new CheckoutLine(l.cartItem().getCartItemId(), l.gift().itemId(),
                            l.gift().itemName(), l.cartItem().getQuantity(), l.lineTotal(), l.discount(),
                            l.deliveryFee(), l.payable())).toList(),
                    lines.stream().mapToLong(PricedLine::lineTotal).sum(),
                    lines.stream().mapToLong(PricedLine::discount).sum(),
                    lines.stream().mapToLong(PricedLine::deliveryFee).sum(),
                    lines.stream().mapToLong(PricedLine::payable).sum()));
        }
        return new CheckoutQuote(groups,
                groups.stream().mapToLong(CheckoutGroup::groupTotal).sum(),
                groups.stream().mapToLong(CheckoutGroup::groupDiscount).sum(),
                groups.stream().mapToLong(CheckoutGroup::groupDeliveryFee).sum(),
                groups.stream().mapToLong(CheckoutGroup::groupPayable).sum());
    }

    private Map<String, List<PricedLine>> groupByLocgov(List<PricedLine> priced) {
        Map<String, List<PricedLine>> byLocgov = new LinkedHashMap<>();
        for (PricedLine line : priced) {
            String locgovCode = line.gift().locgovCode() != null ? line.gift().locgovCode() : "";
            byLocgov.computeIfAbsent(locgovCode, k -> new ArrayList<>()).add(line);
        }
        return byLocgov;
    }

    // ==================== 결제 ====================

    /**
     * 선택된 장바구니 행들을 주문으로 전환한다. AS-IS는 지자체별 포인트가 부족하면 그
     * 그룹 전체를 "포인트가 부족합니다" 알림으로 막고 아무것도 진행하지 않는다 - 여기서도
     * 동일하게, 부족한 그룹이 하나라도 있으면 아무 주문도 만들지 않고 예외를 던진다.
     * couponIssueByCartItem은 장바구니행ID -> 적용할 쿠폰발급ID(OP_COUPON_USER.COUPON_USER_ID),
     * 선택 안 한 행은 맵에 없거나 null이면 쿠폰 미적용.
     *
     * <p>검증(priceSelected)이 전부 끝난 뒤에야 createOrder를 돌린다. 그럼에도 주문 생성
     * 도중 예외가 나면 이 트랜잭션이 통째로 롤백되는데, OrderSagaPublisher가 커밋 이후에만
     * 발행하므로 롤백된 주문의 ORDER_CREATED가 브로커로 새어나가지 않는다.
     */
    @Transactional
    public List<String> checkout(Long userId, List<Long> selectedCartItemIds, OrderService.DeliveryInfo deliveryInfo,
                                  Map<Long, Integer> couponIssueByCartItem) {
        String address = deliveryInfo != null ? deliveryInfo.address() : null;
        List<PricedLine> priced = priceSelected(userId, selectedCartItemIds, couponIssueByCartItem, address);

        for (Map.Entry<String, List<PricedLine>> entry : groupByLocgov(priced).entrySet()) {
            String locgovCode = entry.getKey();
            long groupPayable = entry.getValue().stream().mapToLong(PricedLine::payable).sum();
            long givePoint = locgovCode.isEmpty() ? 0 : pointClient.balanceByLocgov(userId, locgovCode);
            if (groupPayable > givePoint) {
                String locgovNm = locgovCode.isEmpty() ? "지자체 미지정" : locgovClient.nameOf(locgovCode);
                throw new OrderException(locgovNm + "의 포인트가 부족합니다.");
            }
        }

        List<String> orderIds = new ArrayList<>();
        for (PricedLine line : priced) {
            var order = orderService.createOrder(userId, line.gift(), line.cartItem().getQuantity(),
                    deliveryInfo, line.couponIssueId());
            orderIds.add(order.getOrderId());
        }
        remove(userId, selectedCartItemIds);
        return orderIds;
    }
}
