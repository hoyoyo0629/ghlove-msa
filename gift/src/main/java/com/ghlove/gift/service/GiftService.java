package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.GiftOrderStock;
import com.ghlove.gift.domain.GiftSubcategory;
import com.ghlove.gift.domain.GiftSubcategoryItem;
import com.ghlove.gift.domain.ItemImage;
import com.ghlove.gift.domain.Seller;
import com.ghlove.gift.event.GiftLifecyclePublisher;
import com.ghlove.gift.repository.CommonCodeRepository;
import com.ghlove.gift.repository.GiftOrderStockRepository;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.GiftSubcategoryItemRepository;
import com.ghlove.gift.repository.GiftSubcategoryRepository;
import com.ghlove.gift.repository.ItemImageRepository;
import com.ghlove.gift.repository.SeasonFoodItemRepository;
import com.ghlove.gift.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_STOPPED = "STOPPED";
    private static final String STATUS_DISCONTINUED = "DISCONTINUED";
    private static final String DISPLAY_TYPE_ALWAYS = "ALWAYS";
    private static final String DISPLAY_TYPE_LIMITED = "LIMITED";
    private static final String DISPLAY_ON = "Y";
    private static final String DISPLAY_OFF = "N";
    private static final String SOLD_OUT = "1";
    private static final String IN_STOCK = "0";
    private static final String RESERVATION_RESERVED = "RESERVED";
    private static final String RESERVATION_RESTORED = "RESTORED";
    private static final DateTimeFormatter CREATED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final GiftRepository giftRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final GiftOrderStockRepository giftOrderStockRepository;
    private final ItemImageRepository itemImageRepository;
    private final FileStorageService fileStorageService;
    private final GiftLifecyclePublisher giftLifecyclePublisher;
    private final SeasonFoodItemRepository seasonFoodItemRepository;
    private final SellerRepository sellerRepository;
    private final GiftSubcategoryRepository giftSubcategoryRepository;
    private final GiftSubcategoryItemRepository giftSubcategoryItemRepository;

    /** No-hardcoding principle: code labels always come from OP_COMMON_CODE, never a Java enum/switch. */
    public Map<String, String> codesOf(String codeType) {
        return commonCodeRepository.findByCodeTypeAndLanguageAndUseYnOrderByOrdering(codeType, "ko", "Y").stream()
                .collect(Collectors.toMap(
                        com.ghlove.gift.domain.CommonCode::getId,
                        com.ghlove.gift.domain.CommonCode::getLabel,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    /** 답례품몰 GNB "전체 카테고리" 메가메뉴 - 대분류(GIFT_CATEGORY) > 중분류(GiftSubcategory)
     *  > 개별품목(GiftSubcategoryItem) 3단계 트리를 조립한다. */
    public List<CategoryTreeGroup> categoryTree() {
        Map<String, String> categoryLabels = codesOf("GIFT_CATEGORY");
        Map<Long, List<GiftSubcategoryItem>> itemsBySubcategory = giftSubcategoryItemRepository
                .findAllByOrderBySubcategoryIdAscOrderingAsc().stream()
                .collect(Collectors.groupingBy(GiftSubcategoryItem::getSubcategoryId, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<GiftSubcategory>> subcategoriesByCode = giftSubcategoryRepository
                .findAllByOrderByCategoryCodeAscOrderingAsc().stream()
                .collect(Collectors.groupingBy(GiftSubcategory::getCategoryCode, LinkedHashMap::new, Collectors.toList()));

        return categoryLabels.entrySet().stream()
                .map(e -> {
                    String code = e.getKey();
                    List<CategoryTreeGroup.CategoryTreeItem> categories = subcategoriesByCode
                            .getOrDefault(code, List.of()).stream()
                            .map(sub -> new CategoryTreeGroup.CategoryTreeItem(sub.getName(),
                                    itemsBySubcategory.getOrDefault(sub.getSubcategoryId(), List.of()).stream()
                                            .map(GiftSubcategoryItem::getName).toList()))
                            .toList();
                    return new CategoryTreeGroup(code, e.getValue(), categories);
                })
                .toList();
    }

    public List<Gift> publicGifts(String categoryCode) {
        return publicGifts(categoryCode, null, null);
    }

    /** AS-IS 메인 화면의 "답례품 검색" 검색창 - 답례품명 부분일치 검색. */
    public List<Gift> publicGifts(String categoryCode, String keyword) {
        return publicGifts(categoryCode, keyword, null);
    }

    /** locgovCode는 답례품몰 GNB "지자체몰 선택하기" 지도 팝업에서 지자체를 고른 경우. */
    public List<Gift> publicGifts(String categoryCode, String keyword, String locgovCode) {
        List<Gift> gifts;
        if (keyword != null && !keyword.isBlank()) {
            gifts = giftRepository.findByDataStatusCodeAndDisplayFlagAndItemNameContainingIgnoreCaseOrderByItemIdDesc(
                    STATUS_APPROVED, DISPLAY_ON, keyword.trim());
        } else if (locgovCode != null && !locgovCode.isBlank()) {
            gifts = giftRepository.findByDataStatusCodeAndDisplayFlagAndLocgovCodeOrderByItemIdDesc(
                    STATUS_APPROVED, DISPLAY_ON, locgovCode);
        } else if (categoryCode == null || categoryCode.isBlank()) {
            gifts = giftRepository.findByDataStatusCodeAndDisplayFlagOrderByItemIdDesc(STATUS_APPROVED, DISPLAY_ON);
        } else {
            gifts = giftRepository.findByDataStatusCodeAndDisplayFlagAndCategoryCodeOrderByItemIdDesc(
                    STATUS_APPROVED, DISPLAY_ON, categoryCode);
        }
        return gifts.stream().filter(this::withinDisplayPeriod).toList();
    }

    /** 답례품몰 GNB "제철식품관" - AS-IS G_SEASON_FOOD_ITEM에서 이번 달로 등록된 답례품. */
    public List<Gift> seasonalGifts() {
        int thisMonth = LocalDate.now().getMonthValue();
        List<Long> itemIds = seasonFoodItemRepository.findBySeasonFoodMonth(thisMonth).stream()
                .map(com.ghlove.gift.domain.SeasonFoodItem::getItemId).toList();
        if (itemIds.isEmpty()) {
            return List.of();
        }
        return giftRepository.findAllById(itemIds).stream()
                .filter(g -> STATUS_APPROVED.equals(g.getDataStatusCode()) && DISPLAY_ON.equals(g.getDisplayFlag()))
                .filter(this::withinDisplayPeriod)
                .toList();
    }

    /** 답례품몰 GNB "마을기업관" - AS-IS OP_SELLER.COMMUNITY_BUSINESS_YN='Y'인 판매자의 답례품. */
    public List<Gift> communityBusinessGifts() {
        List<Long> sellerIds = sellerRepository.findByCommunityBusinessYn("Y").stream()
                .map(Seller::getSellerId).toList();
        if (sellerIds.isEmpty()) {
            return List.of();
        }
        return sellerIds.stream()
                .flatMap(sellerId -> giftRepository.findBySellerIdOrderByItemIdDesc(sellerId).stream())
                .filter(g -> STATUS_APPROVED.equals(g.getDataStatusCode()) && DISPLAY_ON.equals(g.getDisplayFlag()))
                .filter(this::withinDisplayPeriod)
                .toList();
    }

    /** DISPLAY_TYPE=LIMITED(한시노출)인 경우 노출기간(DISPLAY_START/END_DATE) 안에 있는지 확인한다. */
    private boolean withinDisplayPeriod(Gift gift) {
        if (!DISPLAY_TYPE_LIMITED.equals(gift.getDisplayType())) {
            return true;
        }
        String today = DATE_FORMAT.format(LocalDate.now());
        boolean afterStart = gift.getDisplayStartDate() == null || today.compareTo(gift.getDisplayStartDate()) >= 0;
        boolean beforeEnd = gift.getDisplayEndDate() == null || today.compareTo(gift.getDisplayEndDate()) <= 0;
        return afterStart && beforeEnd;
    }

    public Gift detail(Long itemId) {
        return giftRepository.findById(itemId)
                .orElseThrow(() -> new GiftException("답례품을 찾을 수 없습니다."));
    }

    /** 답례품 상세화면 "판매자" 표시용 (AS-IS view_search_item의 SELLER_NAME/SELLER_COMPANY_NAME
     *  조인 - crypto_dec 복호화 부분은 [[deferred-pii-encryption-view-crypto]] 결정에 따라 평문 그대로). */
    public Seller sellerOf(Long sellerId) {
        return sellerRepository.findById(sellerId).orElse(null);
    }

    /** 마이페이지 "답례품 후기"/"답례품Q&A" 목록에서 항목명/썸네일을 붙이는 데 쓴다. */
    public List<Gift> giftsOf(List<Long> itemIds) {
        return giftRepository.findAllById(itemIds);
    }

    public List<Gift> myGifts(Long sellerId) {
        return giftRepository.findBySellerIdOrderByItemIdDesc(sellerId);
    }

    /** 지자체 승인 대기함. */
    public List<Gift> pendingGifts() {
        return giftRepository.findByDataStatusCodeOrderByItemIdDesc(STATUS_PENDING);
    }

    /** 답례품 등록. 지자체 승인 전까지는 대민 화면에 노출되지 않는다 (PENDING/비노출). */
    @Transactional
    public Gift register(Long sellerId, String itemName, String itemSummary, String detailContent,
                          String categoryCode, String locgovCode, Integer salePrice, Integer stockQuantity,
                          String displayType, String displayStartDate, String displayEndDate,
                          Integer minDonationAmount, List<MultipartFile> images) {
        if (sellerId == null || sellerId <= 0) {
            throw new GiftException("제공자 ID를 입력해 주세요.");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new GiftException("답례품명을 입력해 주세요.");
        }
        if (!codesOf("GIFT_CATEGORY").containsKey(categoryCode)) {
            throw new GiftException("올바른 카테고리를 선택해 주세요.");
        }
        if (salePrice == null || salePrice <= 0) {
            throw new GiftException("가격은 0보다 커야 합니다.");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new GiftException("재고 수량은 0 이상이어야 합니다.");
        }
        String resolvedDisplayType = (displayType == null || displayType.isBlank()) ? DISPLAY_TYPE_ALWAYS : displayType;
        if (!codesOf("GIFT_DISPLAY_TYPE").containsKey(resolvedDisplayType)) {
            throw new GiftException("올바른 노출 유형을 선택해 주세요.");
        }

        Gift gift = new Gift();
        gift.setSellerId(sellerId);
        gift.setItemName(itemName);
        gift.setItemSummary(itemSummary);
        gift.setDetailContent(detailContent);
        gift.setCategoryCode(categoryCode);
        gift.setLocgovCode(locgovCode);
        gift.setSalePrice(salePrice);
        gift.setStockQuantity(stockQuantity);
        gift.setSoldOut(stockQuantity <= 0 ? SOLD_OUT : IN_STOCK);
        gift.setDisplayFlag(DISPLAY_OFF);
        gift.setDataStatusCode(STATUS_PENDING);
        gift.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
        gift.setDisplayType(resolvedDisplayType);
        gift.setDisplayStartDate(normalizeDate(displayStartDate));
        gift.setDisplayEndDate(normalizeDate(displayEndDate));
        gift.setMinDonationAmount(minDonationAmount);
        Gift saved = giftRepository.save(gift);
        giftLifecyclePublisher.publish(saved);

        if (images != null) {
            int ordering = 0;
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String storedName = fileStorageService.store(file);
                ItemImage itemImage = new ItemImage();
                itemImage.setItemId(saved.getItemId());
                itemImage.setImageName(storedName);
                itemImage.setOrdering(ordering++);
                itemImage.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
                itemImageRepository.save(itemImage);
            }
        }
        return saved;
    }

    /** ORDERING=0(첫 이미지)이 썸네일이다. */
    public List<ItemImage> imagesOf(Long itemId) {
        return itemImageRepository.findByItemIdOrderByOrderingAsc(itemId);
    }

    /** 목록 화면용: 여러 답례품의 썸네일(첫 이미지)을 한 번에 조회. */
    public Map<Long, String> thumbnailsOf(List<Long> itemIds) {
        return itemImageRepository.findByItemIdInOrderByOrderingAsc(itemIds).stream()
                .collect(Collectors.toMap(ItemImage::getItemId, ItemImage::getImageName, (a, b) -> a));
    }

    /** 목록 카드의 "신규" 배지(AS-IS itemNewFlag) - 등록 14일 이내 답례품. */
    public java.util.Set<Long> newItemIds(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return java.util.Set.of();
        }
        String cutoff = CREATED_DATE_FORMAT.format(LocalDateTime.now().minusDays(14));
        return giftRepository.findAllById(itemIds).stream()
                .filter(g -> g.getCreatedDate() != null && g.getCreatedDate().compareTo(cutoff) >= 0)
                .map(Gift::getItemId)
                .collect(Collectors.toSet());
    }

    /** 지자체 승인 처리. 승인권자 인증은 게이트웨이/운영 서비스 부재로 아직 없음 - 임시로 버튼 하나로 승인. */
    @Transactional
    public Gift approve(Long itemId) {
        Gift gift = detail(itemId);
        if (!STATUS_PENDING.equals(gift.getDataStatusCode())) {
            throw new GiftException("승인 대기 상태인 답례품만 승인할 수 있습니다.");
        }
        gift.setDataStatusCode(STATUS_APPROVED);
        gift.setDisplayFlag(DISPLAY_ON);
        Gift saved = giftRepository.save(gift);
        giftLifecyclePublisher.publish(saved);
        return saved;
    }

    @Transactional
    public Gift reject(Long itemId) {
        Gift gift = detail(itemId);
        if (!STATUS_PENDING.equals(gift.getDataStatusCode())) {
            throw new GiftException("승인 대기 상태인 답례품만 반려할 수 있습니다.");
        }
        gift.setDataStatusCode(STATUS_REJECTED);
        gift.setDisplayFlag(DISPLAY_OFF);
        Gift saved = giftRepository.save(gift);
        giftLifecyclePublisher.publish(saved);
        return saved;
    }

    // ---- 대표상품관리 (AS-IS opmanager/item/representative-item - 실제 운영사이트에 살아있는
    // 기능. OP_ITEM.REPRESENTATIVE_ITEM_YN 단순 플래그이며, 다수 상품이 동시에 대표상품일 수
    // 있다. AS-IS의 지자체담당자 전용 권한 제약("SYS 롤은 등록/삭제 불가")은 이 프로젝트의
    // 관리자 모델이 지자체별로 나뉘어 있지 않아 재현하지 않는다. ----

    public List<Gift> representativeGifts() {
        return giftRepository.findByRepresentativeItemYnOrderByItemIdDesc("Y");
    }

    @Transactional
    public Gift registerRepresentative(Long itemId) {
        Gift gift = detail(itemId);
        gift.setRepresentativeItemYn("Y");
        return giftRepository.save(gift);
    }

    @Transactional
    public Gift unregisterRepresentative(Long itemId) {
        Gift gift = detail(itemId);
        gift.setRepresentativeItemYn("N");
        return giftRepository.save(gift);
    }

    /**
     * 답례품 수정 (SFR-005). 승인 이전(PENDING) 항목은 자유롭게 수정 가능하고, 이미
     * 승인된(APPROVED) 항목은 노출/재고와 무관한 정보(설명·가격·노출정책 등)만 고쳐도
     * 재승인 절차 없이 즉시 반영한다 - AS-IS 수준의 엄격한 재검수 워크플로는 범위 밖.
     */
    @Transactional
    public Gift edit(Long itemId, Long sellerId, String itemName, String itemSummary, String detailContent,
                      String categoryCode, Integer salePrice, String displayType,
                      String displayStartDate, String displayEndDate, Integer minDonationAmount) {
        Gift gift = detail(itemId);
        if (!gift.getSellerId().equals(sellerId)) {
            throw new GiftException("본인이 등록한 답례품만 수정할 수 있습니다.");
        }
        if (STATUS_DISCONTINUED.equals(gift.getDataStatusCode())) {
            throw new GiftException("폐지된 답례품은 수정할 수 없습니다.");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new GiftException("답례품명을 입력해 주세요.");
        }
        if (!codesOf("GIFT_CATEGORY").containsKey(categoryCode)) {
            throw new GiftException("올바른 카테고리를 선택해 주세요.");
        }
        if (salePrice == null || salePrice <= 0) {
            throw new GiftException("가격은 0보다 커야 합니다.");
        }
        String resolvedDisplayType = (displayType == null || displayType.isBlank()) ? DISPLAY_TYPE_ALWAYS : displayType;
        if (!codesOf("GIFT_DISPLAY_TYPE").containsKey(resolvedDisplayType)) {
            throw new GiftException("올바른 노출 유형을 선택해 주세요.");
        }

        gift.setItemName(itemName);
        gift.setItemSummary(itemSummary);
        gift.setDetailContent(detailContent);
        gift.setCategoryCode(categoryCode);
        gift.setSalePrice(salePrice);
        gift.setDisplayType(resolvedDisplayType);
        gift.setDisplayStartDate(normalizeDate(displayStartDate));
        gift.setDisplayEndDate(normalizeDate(displayEndDate));
        gift.setMinDonationAmount(minDonationAmount);
        return giftRepository.save(gift);
    }

    /** 폐지 - 판매중지(STOPPED, 재개 가능)와 달리 되돌릴 수 없는 영구 종료. */
    @Transactional
    public Gift discontinue(Long itemId, Long sellerId) {
        Gift gift = detail(itemId);
        if (!gift.getSellerId().equals(sellerId)) {
            throw new GiftException("본인이 등록한 답례품만 폐지할 수 있습니다.");
        }
        if (STATUS_DISCONTINUED.equals(gift.getDataStatusCode())) {
            throw new GiftException("이미 폐지된 답례품입니다.");
        }
        gift.setDataStatusCode(STATUS_DISCONTINUED);
        gift.setDisplayFlag(DISPLAY_OFF);
        Gift saved = giftRepository.save(gift);
        giftLifecyclePublisher.publish(saved);
        return saved;
    }

    /** <input type=date>는 yyyy-MM-dd로 제출되는데 저장 형식은 yyyyMMdd라 하이픈을 제거한다. */
    private String normalizeDate(String value) {
        return (value == null || value.isBlank()) ? null : value.replace("-", "");
    }

    /** 판매중지. 승인된 상품을 제공자/지자체가 임시로 노출에서 내릴 때 사용. */
    @Transactional
    public Gift stop(Long itemId) {
        Gift gift = detail(itemId);
        if (!STATUS_APPROVED.equals(gift.getDataStatusCode())) {
            throw new GiftException("승인된 답례품만 판매중지할 수 있습니다.");
        }
        gift.setDataStatusCode(STATUS_STOPPED);
        gift.setDisplayFlag(DISPLAY_OFF);
        Gift saved = giftRepository.save(gift);
        giftLifecyclePublisher.publish(saved);
        return saved;
    }

    /**
     * 제공자용 수동 재고 조정 (분실/파손 등 정정 목적). 주문에 의한 정상적인 재고
     * 차감/복원은 order.saga 이벤트로 처리되는 reserveStockForOrder/
     * restoreStockForOrder를 통해서만 이뤄진다.
     */
    @Transactional
    public Gift adjustStock(Long itemId, int newQuantity) {
        if (newQuantity < 0) {
            throw new GiftException("재고 수량은 0 이상이어야 합니다.");
        }
        Gift gift = detail(itemId);
        gift.setStockQuantity(newQuantity);
        gift.setSoldOut(newQuantity <= 0 ? SOLD_OUT : IN_STOCK);
        return giftRepository.save(gift);
    }

    /**
     * order.saga의 ORDER_CREATED 이벤트에 반응해 재고를 차감한다. GIFT_ORDER_STOCK에
     * REF(주문ID)로 기록해 Kafka 재전달에도 중복 차감되지 않도록 한다.
     *
     * @return true면 예약 성공(호출자가 STOCK_RESERVED 발행), false면 재고 부족(STOCK_RESERVE_FAILED 발행)
     */
    @Transactional
    public boolean reserveStockForOrder(String orderId, Long itemId, int quantity) {
        if (giftOrderStockRepository.existsById(orderId)) {
            log.info("Order {} already processed for stock reservation - skipping duplicate event", orderId);
            return true;
        }

        Gift gift = giftRepository.findById(itemId).orElse(null);
        if (gift == null || gift.getStockQuantity() == null || gift.getStockQuantity() < quantity) {
            return false;
        }

        gift.setStockQuantity(gift.getStockQuantity() - quantity);
        gift.setSoldOut(gift.getStockQuantity() <= 0 ? SOLD_OUT : IN_STOCK);
        giftRepository.save(gift);

        GiftOrderStock reservation = new GiftOrderStock();
        reservation.setOrderId(orderId);
        reservation.setItemId(itemId);
        reservation.setQuantity(quantity);
        reservation.setStatus(RESERVATION_RESERVED);
        reservation.setCreatedDate(LocalDateTime.now());
        giftOrderStockRepository.save(reservation);
        return true;
    }

    /** order.saga의 ORDER_CANCELLED에 반응해, 이 서비스가 실제로 예약했던 재고만 복원한다. */
    @Transactional
    public void restoreStockForOrder(String orderId) {
        GiftOrderStock reservation = giftOrderStockRepository.findById(orderId).orElse(null);
        if (reservation == null) {
            log.info("Order {} was never reserved here - nothing to restore", orderId);
            return;
        }
        if (RESERVATION_RESTORED.equals(reservation.getStatus())) {
            log.info("Order {} stock already restored - skipping duplicate event", orderId);
            return;
        }

        giftRepository.findById(reservation.getItemId()).ifPresent(gift -> {
            gift.setStockQuantity(gift.getStockQuantity() + reservation.getQuantity());
            gift.setSoldOut(IN_STOCK);
            giftRepository.save(gift);
        });
        reservation.setStatus(RESERVATION_RESTORED);
        giftOrderStockRepository.save(reservation);
    }
}
