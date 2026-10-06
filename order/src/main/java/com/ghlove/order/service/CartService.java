package com.ghlove.order.service;

import com.ghlove.order.domain.CartItem;
import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.repository.CartItemRepository;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.repository.ShipmentRepository;
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
    // 멀티아이템 체크아웃(출고 단위 SAGA, 선택지 A) 전용 - 레거시 checkout()과 병행
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderSagaPublisher orderSagaPublisher;

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
        add(userId, itemId, quantity, null);
    }

    public void add(Long userId, Long itemId, Integer quantity, Long itemOptionId) {
        add(userId, itemId, quantity, itemOptionId, null, null);
    }

    /**
     * 옵션 선택형 담기 - itemOptionId가 있으면 gift에서 옵션명·추가금액을 스냅샷한다.
     * 같은 답례품이라도 옵션·각인이 다르면 별도 장바구니 행으로 담긴다(AS-IS 동일).
     * 각인(textOption)은 필수 추가정보 입력값(제목별 값을 '||'로 연결), 추가구성(additionItemIds)은
     * 본품과 별도 라인으로 함께 담긴다(docs/gift-option-redesign-plan.md Phase 4).
     */
    public void add(Long userId, Long itemId, Integer quantity, Long itemOptionId, String textOption,
                    List<Long> additionItemIds) {
        if (userId == null || userId <= 0) {
            throw new OrderException("회원 ID를 입력해 주세요.");
        }
        int qty = quantity == null || quantity <= 0 ? 1 : quantity;
        String text = textOption == null ? "" : textOption.trim();

        GiftItemInfo gift = giftClient.fetch(itemId);
        requireOrderable(gift, "현재 장바구니에 담을 수 없는 답례품입니다.");

        GiftClient.OptionInfo option = null;
        if (itemOptionId != null && itemOptionId > 0) {
            option = giftClient.fetchOption(itemOptionId);
            if (option.itemId() == null || !option.itemId().equals(itemId)) {
                throw new OrderException("선택한 옵션이 이 답례품의 옵션이 아닙니다.");
            }
            if (option.soldOut()) {
                throw new OrderException("품절된 옵션입니다.");
            }
        }

        try {
            addOrAccumulate(userId, gift, qty, option, text);
        } catch (DataIntegrityViolationException e) {
            log.info("Cart add raced on unique(userId={}, itemId={}, option, text) - retrying as accumulate", userId, gift.itemId());
            addOrAccumulate(userId, gift, qty, option, text);
        }

        // 추가구성: 본품과 별도 라인으로 담는다(옵션·각인 없음).
        if (additionItemIds != null) {
            for (Long additionItemId : additionItemIds) {
                if (additionItemId != null && additionItemId > 0 && !additionItemId.equals(itemId)) {
                    add(userId, additionItemId, qty, null, null, null);
                }
            }
        }
    }

    private void addOrAccumulate(Long userId, GiftItemInfo gift, int addQuantity, GiftClient.OptionInfo option,
                                 String textOption) {
        long optionId = option != null ? option.itemOptionId() : 0L;
        String text = textOption == null ? "" : textOption;
        CartItem cartItem = cartItemRepository
                .findByUserIdAndItemIdAndItemOptionIdAndTextOption(userId, gift.itemId(), optionId, text)
                .orElseGet(() -> {
                    CartItem c = new CartItem();
                    c.setUserId(userId);
                    c.setItemId(gift.itemId());
                    c.setItemOptionId(optionId);
                    c.setOptionName(option != null ? option.optionName() : null);
                    c.setOptionPrice(option != null && option.optionPrice() != null ? option.optionPrice() : 0);
                    c.setTextOption(text);
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

    /** 선택 옵션이 품절인가(자동제거 판정용). 옵션이 사라졌으면 주문불가로 간주한다. */
    private boolean isOptionSoldOut(Long itemOptionId) {
        if (itemOptionId == null || itemOptionId <= 0) {
            return false;
        }
        try {
            return giftClient.fetchOption(itemOptionId).soldOut();
        } catch (OrderException e) {
            return true;
        }
    }

    private void requireStock(GiftItemInfo gift, int quantity) {
        if (quantity > MAX_QUANTITY) {
            throw new OrderException("한 답례품은 최대 " + MAX_QUANTITY + "개까지 담을 수 있습니다.");
        }
        requireOrderMaxQuantity(gift, quantity);
        int stock = gift.stockQuantity() == null ? 0 : gift.stockQuantity();
        if (stock < quantity) {
            throw new OrderException("재고가 부족합니다. (현재 재고: " + stock + ")");
        }
    }

    /** AS-IS 답례품 상세의 orderMaxQuantity(1회 주문 최대 수량) 서버측 강제 - 프론트 +/- 제한과
     *  같은 상한을 담기/수량변경/체크아웃 어느 경로로 들어와도 넘지 못하게 한다. 0 이하/미설정은
     *  제한 없음(AS-IS와 동일). */
    private void requireOrderMaxQuantity(GiftItemInfo gift, int quantity) {
        Integer max = gift.orderMaxQuantity();
        if (max != null && max > 0 && quantity > max) {
            throw new OrderException("최대 주문 수량은 " + max + "개입니다.");
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
    /** 배송비 계산기 입력 한 줄 - 답례품의 배송정책 필드를 그대로 넘긴다(key/sequence는 장바구니행ID). */
    private static DeliveryFeeCalculator.Line shipLine(GiftItemInfo gift, Long key, int quantity, long lineTotal) {
        return new DeliveryFeeCalculator.Line(key, gift.shippingType(),
                gift.shipping() != null ? gift.shipping() : 0, gift.shippingFreeAmount(),
                gift.shippingItemCount(), gift.shippingExtraCharge1(), gift.shippingExtraCharge2(),
                gift.shippingGroupCode(), gift.shipmentGroupCode(), null, quantity, lineTotal, key);
    }

    private List<CartGroup> buildGroups(Long userId, List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return List.of();
        }

        // 배송비는 묶음배송 그룹 단위로 계산해야 하므로, 먼저 지자체별로 원자료를 모은 뒤
        // 그룹별로 DeliveryFeeCalculator를 돌린다(AS-IS Shipping.getShippingGroups와 동일).
        record Raw(CartItem cartItem, GiftItemInfo gift, int optionPrice, long lineTotal) {
        }
        Map<String, List<Raw>> rawByLocgov = new LinkedHashMap<>();
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
            // 옵션 품절도 아이템 품절과 동일하게 자동 제거한다(AS-IS: 품절되면 장바구니에서 자동 삭제).
            if (isOptionSoldOut(cartItem.getItemOptionId())) {
                unavailable.add(cartItem.getCartItemId());
                continue;
            }

            int optionPrice = cartItem.getOptionPrice() != null ? cartItem.getOptionPrice() : 0;
            long lineTotal = (long) (gift.salePrice() + optionPrice) * cartItem.getQuantity();
            String locgovCode = gift.locgovCode() != null ? gift.locgovCode() : "";
            rawByLocgov.computeIfAbsent(locgovCode, k -> new ArrayList<>())
                    .add(new Raw(cartItem, gift, optionPrice, lineTotal));
        }

        if (!unavailable.isEmpty()) {
            remove(userId, unavailable);
        }

        List<CartGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<Raw>> entry : rawByLocgov.entrySet()) {
            String locgovCode = entry.getKey();
            List<Raw> raws = entry.getValue();
            // 장바구니 단계라 배송지 미확정 → 제주/도서산간 판정 없음(islandType ""). 주문결제 화면에서
            // 주소(우편번호)를 넣으면 quote()가 추가배송비까지 반영해 다시 내려준다.
            List<DeliveryFeeCalculator.Line> shipLines = raws.stream()
                    .map(r -> shipLine(r.gift(), r.cartItem().getCartItemId(), r.cartItem().getQuantity(), r.lineTotal()))
                    .toList();
            Map<Long, Long> feeByCartItem = DeliveryFeeCalculator.compute(shipLines, "");

            List<CartLine> lines = new ArrayList<>();
            for (Raw r : raws) {
                long deliveryFee = feeByCartItem.getOrDefault(r.cartItem().getCartItemId(), 0L);
                String thumbnailUrl = r.gift().thumbnailPath() != null ? giftServiceBaseUrl + r.gift().thumbnailPath() : null;
                lines.add(new CartLine(r.cartItem().getCartItemId(), r.gift().itemId(), r.gift().itemName(), thumbnailUrl,
                        r.cartItem().getQuantity(), r.gift().salePrice(), r.lineTotal(), deliveryFee, r.lineTotal() + deliveryFee,
                        r.cartItem().getOptionName(), r.optionPrice(),
                        r.cartItem().getTextOption() == null || r.cartItem().getTextOption().isBlank() ? null : r.cartItem().getTextOption()));
            }
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
                                long lineTotal, long discount, long deliveryFee, long payable, String optionName,
                                String textOption) {
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
                                Map<Long, Integer> couponIssueByCartItem, String zipcode) {
        return toQuote(userId, priceSelected(userId, selectedCartItemIds, couponIssueByCartItem, zipcode));
    }

    /**
     * 선택된 장바구니 행 전체를 한 번에 검증하고 금액을 확정한다. 주문을 단 한 건도 만들기
     * 전에 실패 사유(품절/미승인/재고부족/쿠폰 중복선택/사용불가 쿠폰)를 여기서 모두 던지기
     * 때문에, "N건 중 K번째에서 실패해 앞선 주문만 이벤트가 나가는" 부분 체크아웃이 없다.
     */
    private List<PricedLine> priceSelected(Long userId, List<Long> selectedCartItemIds,
                                            Map<Long, Integer> couponIssueByCartItem, String zipcode) {
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
        // 배송비는 묶음배송 그룹 단위 계산이라 검증을 모두 마친 뒤 지자체 그룹별로 산정한다.
        record Draft(CartItem cartItem, GiftItemInfo gift, Integer couponIssueId, long lineTotal, long discount) {
        }
        List<Draft> drafts = new ArrayList<>();

        for (CartItem cartItem : selected) {
            GiftItemInfo gift = giftClient.fetch(cartItem.getItemId());
            if (!STATUS_APPROVED.equals(gift.dataStatusCode()) || SOLD_OUT.equals(gift.soldOut())) {
                throw new OrderException("'" + gift.itemName() + "'은(는) 현재 주문할 수 없습니다.");
            }
            // 옵션 품절이면 주문 단계에서 막는다(AS-IS OrderServiceImpl "재고가 없습니다" throw → /cart).
            if (cartItem.getItemOptionId() != null && cartItem.getItemOptionId() > 0) {
                GiftClient.OptionInfo option = giftClient.fetchOption(cartItem.getItemOptionId());
                if (option.soldOut()) {
                    throw new OrderException("'" + gift.itemName() + "'의 선택 옵션이 품절되어 주문할 수 없습니다.");
                }
            }
            int quantity = cartItem.getQuantity();
            Integer max = gift.orderMaxQuantity();
            if (max != null && max > 0 && quantity > max) {
                throw new OrderException("'" + gift.itemName() + "'의 최대 주문 수량은 " + max + "개입니다.");
            }
            int stock = gift.stockQuantity() == null ? 0 : gift.stockQuantity();
            if (stock < quantity) {
                throw new OrderException("'" + gift.itemName() + "'의 재고가 부족합니다. (현재 재고: " + stock + ")");
            }

            Integer couponIssueId =
                    couponIssueByCartItem != null ? couponIssueByCartItem.get(cartItem.getCartItemId()) : null;
            if (couponIssueId != null && !usedCoupons.add(couponIssueId)) {
                throw new OrderException("같은 쿠폰을 여러 답례품에 사용할 수 없습니다.");
            }

            int optionPrice = cartItem.getOptionPrice() != null ? cartItem.getOptionPrice() : 0;
            long lineTotal = (long) (gift.salePrice() + optionPrice) * quantity;
            long discount = couponIssueId != null
                    ? couponService.previewDiscount(userId, couponIssueId, gift.itemId(), lineTotal, quantity)
                    : 0;
            drafts.add(new Draft(cartItem, gift, couponIssueId, lineTotal, discount));
        }

        // 지자체 그룹마다 묶음배송 계산(AS-IS Shipping.getShippingGroups). 배송지 우편번호로
        // 제주/도서산간 추가배송비까지 반영한다. 그룹 배송비는 그룹 첫 라인에 배분된다.
        String islandType = orderService.islandTypeOf(zipcode);
        Map<Long, Long> feeByCartItem = new LinkedHashMap<>();
        Map<String, List<Draft>> byLocgov = new LinkedHashMap<>();
        for (Draft d : drafts) {
            String locgovCode = d.gift().locgovCode() != null ? d.gift().locgovCode() : "";
            byLocgov.computeIfAbsent(locgovCode, k -> new ArrayList<>()).add(d);
        }
        for (List<Draft> groupDrafts : byLocgov.values()) {
            List<DeliveryFeeCalculator.Line> shipLines = groupDrafts.stream()
                    .map(d -> shipLine(d.gift(), d.cartItem().getCartItemId(), d.cartItem().getQuantity(), d.lineTotal()))
                    .toList();
            feeByCartItem.putAll(DeliveryFeeCalculator.compute(shipLines, islandType));
        }

        // 원래 선택 순서 그대로 PricedLine 조립(대표품목 repLine = priced.get(0) 규칙 유지).
        List<PricedLine> priced = new ArrayList<>();
        for (Draft d : drafts) {
            long deliveryFee = feeByCartItem.getOrDefault(d.cartItem().getCartItemId(), 0L);
            priced.add(new PricedLine(d.cartItem(), d.gift(), d.couponIssueId(), d.lineTotal(), d.discount(), deliveryFee,
                    d.lineTotal() - d.discount() + deliveryFee));
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
                            l.deliveryFee(), l.payable(), l.cartItem().getOptionName(),
                            l.cartItem().getTextOption() == null || l.cartItem().getTextOption().isBlank()
                                    ? null : l.cartItem().getTextOption())).toList(),
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
        String zipcode = deliveryInfo != null ? deliveryInfo.zipcode() : null;
        List<PricedLine> priced = priceSelected(userId, selectedCartItemIds, couponIssueByCartItem, zipcode);

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
                    deliveryInfo, line.couponIssueId(),
                    line.cartItem().getOptionName(), line.cartItem().getOptionPrice());
            orderIds.add(order.getOrderId());
        }
        remove(userId, selectedCartItemIds);
        return orderIds;
    }

    // ==================== 멀티아이템 결제 (출고 단위 SAGA, 선택지 A) ====================

    /**
     * AS-IS처럼 <b>한 번의 결제 = 주문 1건</b>으로 만든다(설계안 docs/order-multiitem-redesign-plan.md
     * §2·§3). 장바구니 선택분을 지자체별 <b>출고(Shipment)</b>로 묶고, 각 출고 아래에 <b>품목
     * (OrderItem)</b>을 담아 주문 헤더 1건 + 출고 N + 품목 M을 저장한 뒤 출고마다
     * {@code SHIPMENT_CREATED}를 발행해 출고 단위 SAGA를 개시한다.
     *
     * <p>레거시 {@link #checkout}(행마다 단일품목 주문 N건)과 <b>병행</b>하는 신 경로다 -
     * gift/point 소비자와 읽기 화면이 신 모델로 전환된 뒤 체크아웃 컨트롤러를 이 메서드로
     * 바꾼다. 검증(품절/재고/최대수량/쿠폰중복/포인트부족)은 레거시와 완전히 동일한
     * {@link #priceSelected} + 지자체별 포인트 검증을 그대로 쓴다.
     *
     * @return 생성된 주문 헤더의 orderId (단 1건)
     */
    @Transactional
    public String checkoutMultiItem(Long userId, List<Long> selectedCartItemIds,
                                     OrderService.DeliveryInfo deliveryInfo,
                                     Map<Long, Integer> couponIssueByCartItem) {
        String zipcode = deliveryInfo != null ? deliveryInfo.zipcode() : null;
        List<PricedLine> priced = priceSelected(userId, selectedCartItemIds, couponIssueByCartItem, zipcode);

        Map<String, List<PricedLine>> byLocgov = groupByLocgov(priced);

        // 포인트 부족 판정은 출고(지자체) 단위 - 레거시 checkout()과 동일 규칙(배송비 포함 payable 기준)
        for (Map.Entry<String, List<PricedLine>> entry : byLocgov.entrySet()) {
            String locgovCode = entry.getKey();
            long groupPayable = entry.getValue().stream().mapToLong(PricedLine::payable).sum();
            long givePoint = locgovCode.isEmpty() ? 0 : pointClient.balanceByLocgov(userId, locgovCode);
            if (groupPayable > givePoint) {
                String locgovNm = locgovCode.isEmpty() ? "지자체 미지정" : locgovClient.nameOf(locgovCode);
                throw new OrderException(locgovNm + "의 포인트가 부족합니다.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        String orderId = orderService.generateOrderId();

        // 주문 헤더 (배송지·합계는 주문 단위 공통). 품목 컬럼(item_id 등)은 헤더에서 비운다.
        Order header = new Order();
        header.setOrderId(orderId);
        header.setUserId(userId);
        header.setOrderStatus(ShipmentSagaService.Status.PENDING);
        header.setPointAmount(priced.stream().mapToLong(PricedLine::payable).sum());
        header.setDeliveryFee(priced.stream().mapToLong(PricedLine::deliveryFee).sum());
        header.setDiscountAmount(priced.stream().mapToLong(PricedLine::discount).sum());
        header.setCreatedDate(now);
        header.setUpdatedDate(now);
        // 헤더의 품목 컬럼은 권위 데이터가 아니라 admin 주문목록·검색·마이주문 목록이 읽는
        // 표시용 요약 캐시다(정본은 OrderItem). 대표품목명 "외 N건" + 총수량 + 대표 itemId/sellerId.
        PricedLine repLine = priced.get(0);
        int totalQty = priced.stream().mapToInt(l -> l.cartItem().getQuantity() != null ? l.cartItem().getQuantity() : 0).sum();
        header.setItemId(repLine.gift().itemId());
        header.setSellerId(repLine.gift().sellerId());
        header.setItemName(priced.size() == 1 ? repLine.gift().itemName()
                : repLine.gift().itemName() + " 외 " + (priced.size() - 1) + "건");
        header.setQuantity(totalQty);
        header.setLocgovCode(byLocgov.size() == 1 ? repLine.gift().locgovCode() : null);
        if (deliveryInfo != null) {
            header.setReceiverName(deliveryInfo.receiverName());
            header.setReceiverPhone(deliveryInfo.receiverPhone());
            header.setDeliveryAddress(deliveryInfo.address());
            header.setDeliveryAddressDetail(deliveryInfo.addressDetail());
            header.setRequestNote(deliveryInfo.requestNote());
        }
        orderRepository.save(header);

        // 출고(지자체) + 품목 저장. 발행은 전량 저장 후(트랜잭션 커밋 뒤 실제 send).
        List<Shipment> shipments = new ArrayList<>();
        Map<Long, List<OrderItem>> itemsByShipment = new LinkedHashMap<>();
        for (Map.Entry<String, List<PricedLine>> entry : byLocgov.entrySet()) {
            String locgovCode = entry.getKey();
            List<PricedLine> lines = entry.getValue();

            Shipment shipment = new Shipment();
            shipment.setOrderId(orderId);
            shipment.setLocgovCode(locgovCode.isEmpty() ? null : locgovCode);
            shipment.setDeliveryFee(lines.stream().mapToLong(PricedLine::deliveryFee).sum());
            shipment.setShipmentStatus(ShipmentSagaService.Status.PENDING);
            shipment.setCreatedDate(now);
            shipment.setUpdatedDate(now);
            // pointAmount(출고 소계 = SAGA 차감단위)는 쿠폰 확정 후 아래에서 합산해 채운다.
            shipmentRepository.save(shipment);

            List<OrderItem> items = new ArrayList<>();
            long shipmentPoint = 0;
            for (PricedLine pl : lines) {
                // 쿠폰을 이 시점에 실제로 소진하고 할인액을 확정한다(레거시 createOrder와 동일).
                long discount = 0;
                if (pl.couponIssueId() != null) {
                    discount = couponService.applyToOrder(userId, pl.couponIssueId(), orderId,
                            pl.gift().itemId(), pl.lineTotal(), pl.cartItem().getQuantity());
                }
                long payable = pl.lineTotal() - discount + pl.deliveryFee();
                shipmentPoint += payable;

                OrderItem item = new OrderItem();
                item.setShipmentId(shipment.getShipmentId());
                item.setOrderId(orderId);
                item.setItemId(pl.gift().itemId());
                item.setItemOptionId(pl.cartItem().getItemOptionId() != null ? pl.cartItem().getItemOptionId() : 0L);
                item.setSellerId(pl.gift().sellerId());
                item.setItemName(pl.gift().itemName());
                item.setOptionName(pl.cartItem().getOptionName());
                item.setOptionPrice(pl.cartItem().getOptionPrice() != null ? pl.cartItem().getOptionPrice() : 0);
                item.setTextOption(pl.cartItem().getTextOption());
                item.setQuantity(pl.cartItem().getQuantity());
                item.setUnitPrice(pl.gift().salePrice());
                item.setPointAmount(payable);
                item.setItemStatus(ShipmentSagaService.Status.PENDING);
                item.setCouponIssueId(pl.couponIssueId());
                item.setDiscountAmount(discount);
                item.setCreatedDate(now);
                item.setUpdatedDate(now);
                orderItemRepository.save(item);
                items.add(item);
            }
            shipment.setPointAmount(shipmentPoint);
            shipmentRepository.save(shipment);

            shipments.add(shipment);
            itemsByShipment.put(shipment.getShipmentId(), items);
        }

        remove(userId, selectedCartItemIds);

        for (Shipment shipment : shipments) {
            orderSagaPublisher.publishShipmentCreated(shipment, userId, itemsByShipment.get(shipment.getShipmentId()));
        }
        return orderId;
    }
}
