package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.GiftOrderStock;
import com.ghlove.gift.domain.GiftSubcategory;
import com.ghlove.gift.domain.GiftSubcategoryItem;
import com.ghlove.gift.domain.ItemImage;
import com.ghlove.gift.domain.SeasonFood;
import com.ghlove.gift.domain.SeasonFoodItem;
import com.ghlove.gift.domain.Seller;
import com.ghlove.gift.event.GiftLifecyclePublisher;
import com.ghlove.gift.repository.CommonCodeRepository;
import com.ghlove.gift.repository.GiftOptionRepository;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final String LABEL_NONE = "1";
    /** 제철 월 선택 상한 - AS-IS `opmanager/i18n/item/form.jsp:5152`의 "3개까지 가능합니다". */
    private static final int SEASON_FOOD_MONTH_LIMIT = 3;
    private static final DateTimeFormatter CREATED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final GiftRepository giftRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final GiftOrderStockRepository giftOrderStockRepository;
    private final com.ghlove.gift.repository.GiftShipmentStockRepository giftShipmentStockRepository;
    private final GiftOptionRepository giftOptionRepository;
    private final ItemImageRepository itemImageRepository;
    private final FileStorageService fileStorageService;
    private final GiftLifecyclePublisher giftLifecyclePublisher;
    private final SeasonFoodItemRepository seasonFoodItemRepository;
    private final com.ghlove.gift.repository.SeasonFoodRepository seasonFoodRepository;
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

    /** 답례품 수정화면에서 체크상태를 되살리기 위한 조회 (AS-IS ItemManagerController:563). */
    public List<Integer> seasonFoodMonthsOf(Long itemId) {
        return seasonFoodItemRepository.findByItemIdOrderBySeasonFoodMonth(itemId).stream()
                .map(SeasonFoodItem::getSeasonFoodMonth)
                .toList();
    }

    /**
     * 답례품의 제철 월 지정을 통째로 갈아끼운다 - AS-IS `ItemServiceImpl:1471~1479`가
     * 답례품 저장 때마다 `deleteSeasonFoodItem` 후 선택된 월만큼 `insertSeasonFoodItem` 하는 것과 같다.
     * AS-IS 폼은 최대 3개까지만 체크되게 막지만(`item/form.jsp:5152`) 그건 화면단 제약뿐이라
     * 서버에서도 같이 막는다 - 운영자 화면을 거치지 않는 호출로 제한이 무력화되면 안 된다.
     */
    @Transactional
    public void replaceSeasonFoodMonths(Long itemId, List<Integer> months, Long managerId) {
        List<Integer> distinct = months == null ? List.of()
                : months.stream().filter(java.util.Objects::nonNull).distinct().sorted().toList();
        if (distinct.stream().anyMatch(m -> m < 1 || m > 12)) {
            throw new GiftException("제철 월은 1~12 사이여야 합니다.");
        }
        if (distinct.size() > SEASON_FOOD_MONTH_LIMIT) {
            throw new GiftException("제철 월 선택은 " + SEASON_FOOD_MONTH_LIMIT + "개까지 가능합니다.");
        }
        seasonFoodItemRepository.deleteByItemId(itemId);
        for (Integer month : distinct) {
            SeasonFoodItem row = new SeasonFoodItem();
            row.setItemId(itemId);
            row.setSeasonFoodMonth(month);
            row.setFrstRegisterId(managerId);
            row.setFrstRegistPnttm(LocalDateTime.now());
            seasonFoodItemRepository.save(row);
        }
    }

    /** 답례품몰 GNB "제철식품관" - AS-IS G_SEASON_FOOD_ITEM에서 지정 월(없으면 이번 달)로 등록된 답례품.
     *  locgovCode(시도/시군구)로 추가 필터한다(AS-IS seasonList의 시도·시군구 검색). */
    public List<Gift> seasonalGifts(Integer month, String locgovCode) {
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : LocalDate.now().getMonthValue();
        List<Long> itemIds = seasonFoodItemRepository.findBySeasonFoodMonth(targetMonth).stream()
                .map(com.ghlove.gift.domain.SeasonFoodItem::getItemId).toList();
        if (itemIds.isEmpty()) {
            return List.of();
        }
        return giftRepository.findAllById(itemIds).stream()
                .filter(g -> STATUS_APPROVED.equals(g.getDataStatusCode()) && DISPLAY_ON.equals(g.getDisplayFlag()))
                .filter(this::withinDisplayPeriod)
                .filter(g -> matchesLocgov(g, locgovCode))
                .toList();
    }

    /** locgovCode 필터 - 빈값이면 전체, 시도코드(끝 000)면 앞 2자리로, 시군구코드면 정확히 매칭. */
    private boolean matchesLocgov(Gift g, String locgovCode) {
        if (locgovCode == null || locgovCode.isBlank()) {
            return true;
        }
        String gc = g.getLocgovCode();
        if (gc == null) {
            return false;
        }
        if (locgovCode.endsWith("000") && locgovCode.length() >= 2 && gc.length() >= 2) {
            return gc.substring(0, 2).equals(locgovCode.substring(0, 2));
        }
        return gc.equals(locgovCode);
    }

    /** 제철식품관 첫 화면의 월별 키워드 카드(1~12월). AS-IS는 데이터가 있는 월만 내려주지만, 화면이
     *  12개월 카드를 그리므로 없는 달은 빈 키워드로 채워 1~12월을 모두 반환한다. */
    public List<SeasonFoodKeyword> seasonFoodKeywords() {
        Map<Integer, String> byMonth = new java.util.HashMap<>();
        for (SeasonFood sf : seasonFoodRepository.findAllByOrderBySeasonFoodMonthAscRegSeqAsc()) {
            byMonth.putIfAbsent(sf.getSeasonFoodMonth(), sf.getSeasonFoodKeyword());
        }
        List<SeasonFoodKeyword> result = new java.util.ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(new SeasonFoodKeyword(m, byMonth.getOrDefault(m, "")));
        }
        return result;
    }

    public record SeasonFoodKeyword(int month, String keyword) {
    }

    /** 답례품몰 GNB "마을기업관" - AS-IS OP_SELLER.COMMUNITY_BUSINESS_YN='Y'인 판매자의 답례품. */
    public List<Gift> communityBusinessGifts(String locgovCode) {
        List<Long> sellerIds = sellerRepository.findByCommunityBusinessYn("Y").stream()
                .map(Seller::getSellerId).toList();
        if (sellerIds.isEmpty()) {
            return List.of();
        }
        return sellerIds.stream()
                .flatMap(sellerId -> giftRepository.findBySellerIdOrderByItemIdDesc(sellerId).stream())
                .filter(g -> STATUS_APPROVED.equals(g.getDataStatusCode()) && DISPLAY_ON.equals(g.getDisplayFlag()))
                .filter(this::withinDisplayPeriod)
                .filter(g -> matchesLocgov(g, locgovCode))
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
        gift.setItemLabel(LABEL_NONE);
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

    /**
     * 배송비/택배사 설정 (SFR-005 재검토 라운드 - "지자체 정책 관리"/운영 성격이라 등록·수정
     * 폼과는 별개의 독립 액션으로 뒀다, {@link com.ghlove.gift.domain.Gift}의 배송 필드
     * 주석 참고). order 서비스가 이 값을 읽어 주문 시점에 배송비를 계산한다.
     */
    @Transactional
    public Gift updateShippingPolicy(Long itemId, String deliveryCompanyName, String shippingType, Integer shipping,
                                      Integer shippingFreeAmount, Integer shippingExtraCharge1,
                                      Integer shippingExtraCharge2, boolean itemReturnAllowed) {
        Gift gift = detail(itemId);
        if (shippingType == null || shippingType.isBlank() || !codesOf("GIFT_SHIPPING_TYPE").containsKey(shippingType)) {
            throw new GiftException("올바른 배송비 구분을 선택해 주세요.");
        }
        if (shipping != null && shipping < 0) {
            throw new GiftException("배송비는 0원 이상이어야 합니다.");
        }
        gift.setDeliveryCompanyName(deliveryCompanyName);
        gift.setShippingType(shippingType);
        gift.setShipping(shipping != null ? shipping : 0);
        gift.setShippingFreeAmount(shippingFreeAmount);
        gift.setShippingExtraCharge1(shippingExtraCharge1);
        gift.setShippingExtraCharge2(shippingExtraCharge2);
        gift.setItemReturnFlag(itemReturnAllowed ? "Y" : "N");
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

    // ---- 답례품 관리자 직접 CRUD (AS-IS opmanager/item - ItemManagerController 1단계 대응).
    // 셀러 self-service(register/edit)와 달리 관리자가 셀러를 대신해 직접 등록/수정한다 -
    // 소유권 검증이 없고, 관리자가 직접 만든 상품이라 승인 대기 없이 즉시 APPROVED+노출로
    // 시작한다(관리자가 만드는 시점에 이미 검수를 마쳤다고 간주). ----

    @Transactional
    public Gift adminCreate(Long sellerId, String itemName, String itemSummary, String detailContent,
                             String categoryCode, String locgovCode, Integer salePrice, Integer stockQuantity,
                             String displayType, String displayStartDate, String displayEndDate,
                             Integer minDonationAmount, Integer brandId, List<MultipartFile> images) {
        if (sellerId == null || !sellerRepository.existsById(sellerId)) {
            throw new GiftException("등록된 판매자를 선택해 주세요.");
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
        gift.setDisplayFlag(DISPLAY_ON);
        gift.setDataStatusCode(STATUS_APPROVED);
        gift.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
        gift.setDisplayType(resolvedDisplayType);
        gift.setDisplayStartDate(normalizeDate(displayStartDate));
        gift.setDisplayEndDate(normalizeDate(displayEndDate));
        gift.setMinDonationAmount(minDonationAmount);
        gift.setBrandId(brandId);
        gift.setItemLabel(LABEL_NONE);
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

    /** 관리자 직접 수정 - 셀러 소유권 검증 없이(edit()과 달리) 모든 답례품을 수정할 수 있고,
     *  판매자 재지정/재고수량 수정도 가능하다(둘 다 셀러 self-service edit()에는 없는 항목). */
    @Transactional
    public Gift adminEdit(Long itemId, Long sellerId, String itemName, String itemSummary, String detailContent,
                           String categoryCode, Integer salePrice, Integer stockQuantity, String displayType,
                           String displayStartDate, String displayEndDate, Integer minDonationAmount, Integer brandId) {
        Gift gift = detail(itemId);
        if (sellerId == null || !sellerRepository.existsById(sellerId)) {
            throw new GiftException("등록된 판매자를 선택해 주세요.");
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

        gift.setSellerId(sellerId);
        gift.setItemName(itemName);
        gift.setItemSummary(itemSummary);
        gift.setDetailContent(detailContent);
        gift.setCategoryCode(categoryCode);
        gift.setSalePrice(salePrice);
        gift.setStockQuantity(stockQuantity);
        gift.setSoldOut(stockQuantity <= 0 ? SOLD_OUT : IN_STOCK);
        gift.setDisplayType(resolvedDisplayType);
        gift.setDisplayStartDate(normalizeDate(displayStartDate));
        gift.setDisplayEndDate(normalizeDate(displayEndDate));
        gift.setMinDonationAmount(minDonationAmount);
        gift.setBrandId(brandId);
        Gift saved = giftRepository.save(gift);
        giftLifecyclePublisher.publish(saved);
        return saved;
    }

    /** 카테고리 일괄 배정 (AS-IS add-items-to-category). */
    @Transactional
    public int bulkAssignCategory(List<Long> itemIds, String categoryCode) {
        if (!codesOf("GIFT_CATEGORY").containsKey(categoryCode)) {
            throw new GiftException("올바른 카테고리를 선택해 주세요.");
        }
        List<Gift> gifts = giftRepository.findAllById(itemIds);
        gifts.forEach(g -> g.setCategoryCode(categoryCode));
        giftRepository.saveAll(gifts);
        return gifts.size();
    }

    /** 상품 복사 (AS-IS copy/{itemId}) - 이미지 포함 전체 필드를 복제한 새 답례품을 PENDING/
     *  비노출 초안으로 만든다(원본이 이미 승인돼 있어도 복사본은 관리자가 다시 검수하도록
     *  안전하게 시작). */
    @Transactional
    public Gift copy(Long itemId) {
        Gift source = detail(itemId);
        Gift copy = new Gift();
        copy.setSellerId(source.getSellerId());
        copy.setItemName(source.getItemName() + " (사본)");
        copy.setItemSummary(source.getItemSummary());
        copy.setDetailContent(source.getDetailContent());
        copy.setCategoryCode(source.getCategoryCode());
        copy.setLocgovCode(source.getLocgovCode());
        copy.setSalePrice(source.getSalePrice());
        copy.setStockQuantity(source.getStockQuantity());
        copy.setSoldOut(source.getStockQuantity() != null && source.getStockQuantity() <= 0 ? SOLD_OUT : IN_STOCK);
        copy.setDisplayFlag(DISPLAY_OFF);
        copy.setDataStatusCode(STATUS_PENDING);
        copy.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
        copy.setDisplayType(source.getDisplayType());
        copy.setDisplayStartDate(source.getDisplayStartDate());
        copy.setDisplayEndDate(source.getDisplayEndDate());
        copy.setMinDonationAmount(source.getMinDonationAmount());
        copy.setBrandId(source.getBrandId());
        copy.setItemLabel(source.getItemLabel() != null ? source.getItemLabel() : LABEL_NONE);
        Gift saved = giftRepository.save(copy);
        giftLifecyclePublisher.publish(saved);

        int ordering = 0;
        for (ItemImage img : imagesOf(itemId)) {
            ItemImage newImage = new ItemImage();
            newImage.setItemId(saved.getItemId());
            newImage.setImageName(img.getImageName());
            newImage.setOrdering(ordering++);
            newImage.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
            itemImageRepository.save(newImage);
        }
        return saved;
    }

    /** 관리자 상품관리 목록 - 검색조건(상품명/카테고리/판매자/상태) 조합. 시드 규모가 작아
     *  인메모리 필터링으로 처리한다(다른 admin 목록화면들과 동일한 관행). */
    public List<Gift> adminSearch(String itemName, String categoryCode, Long sellerId, String dataStatusCode) {
        return giftRepository.findAll().stream()
                .filter(g -> itemName == null || itemName.isBlank() || (g.getItemName() != null && g.getItemName().contains(itemName)))
                .filter(g -> categoryCode == null || categoryCode.isBlank() || categoryCode.equals(g.getCategoryCode()))
                .filter(g -> sellerId == null || sellerId.equals(g.getSellerId()))
                .filter(g -> dataStatusCode == null || dataStatusCode.isBlank() || dataStatusCode.equals(g.getDataStatusCode()))
                .sorted(Comparator.<Gift, Integer>comparing(g -> g.getAdminOrdering() == null ? 0 : g.getAdminOrdering())
                        .thenComparing(Gift::getItemId, Comparator.reverseOrder()))
                .toList();
    }

    /** 판매중지. 승인된 상품을 제공자/지자체가 임시로 노출에서 내릴 때 사용. */
    @Transactional
    public Gift stop(Long itemId, Long sellerId) {
        Gift gift = detail(itemId);
        // discontinue()와 동일한 소유권 검사 - 예전에는 이 검사가 없어 itemId만 바꾸면
        // 남의 답례품을 판매중지시킬 수 있었다.
        if (!gift.getSellerId().equals(sellerId)) {
            throw new GiftException("본인이 등록한 답례품만 판매중지할 수 있습니다.");
        }
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
    public Gift adjustStock(Long itemId, Long sellerId, int newQuantity) {
        if (newQuantity < 0) {
            throw new GiftException("재고 수량은 0 이상이어야 합니다.");
        }
        Gift gift = detail(itemId);
        // 소유권 검사 - 예전에는 없어 남의 답례품 재고를 임의로 바꿀 수 있었다.
        if (!gift.getSellerId().equals(sellerId)) {
            throw new GiftException("본인이 등록한 답례품만 재고를 조정할 수 있습니다.");
        }
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

    // ==================== 멀티아이템(출고 단위 SAGA) 재고 예약/복원 ====================

    /**
     * order.saga의 {@code SHIPMENT_CREATED}에 반응해 <b>출고 내 모든 품목</b> 재고를 예약한다
     * (all-or-nothing: 하나라도 부족하면 아무것도 예약하지 않고 실패). GIFT_SHIPMENT_STOCK에
     * (shipmentId,itemId)로 기록해 Kafka 재전달에도 중복 차감되지 않게 한다.
     *
     * @return true면 출고 전체 예약 성공(SHIPMENT_STOCK_RESERVED 발행), false면 재고 부족(FAILED 발행)
     */
    @Transactional
    public boolean reserveStockForShipment(Long shipmentId,
                                            List<com.ghlove.gift.event.ShipmentCreatedEvent.Line> lines) {
        if (giftShipmentStockRepository.existsByShipmentId(shipmentId)) {
            log.info("Shipment {} already processed for stock reservation - skipping duplicate event", shipmentId);
            return true;
        }
        if (lines == null || lines.isEmpty()) {
            return false;
        }

        // 1) 재고 선검증 - 옵션단위 재고(option_stock_flag='Y')는 옵션ID별, 그 외는 답례품ID별로
        //    합산 수요를 확인한다(같은 답례품/옵션이 여러 줄일 수 있음).
        java.util.Map<Long, Integer> demandByItem = new java.util.HashMap<>();
        java.util.Map<Long, Integer> demandByOption = new java.util.HashMap<>();
        for (var line : lines) {
            int need = line.quantity() != null ? line.quantity() : 0;
            if (isOptionStockTracked(line.itemOptionId())) {
                demandByOption.merge(line.itemOptionId(), need, Integer::sum);
            } else {
                demandByItem.merge(line.itemId(), need, Integer::sum);
            }
        }
        for (var e : demandByItem.entrySet()) {
            Gift gift = giftRepository.findById(e.getKey()).orElse(null);
            if (gift == null || gift.getStockQuantity() == null || gift.getStockQuantity() < e.getValue()) {
                return false;
            }
        }
        for (var e : demandByOption.entrySet()) {
            var option = giftOptionRepository.findById(e.getKey()).orElse(null);
            if (option == null || option.getOptionStockQuantity() == null
                    || option.getOptionStockQuantity() < e.getValue()) {
                return false;
            }
        }

        // 2) 차감 + 예약행 기록(품목=orderItemId 단위). 옵션단위면 옵션재고를, 아니면 답례품재고를 깎는다.
        LocalDateTime now = LocalDateTime.now();
        for (var line : lines) {
            int need = line.quantity() != null ? line.quantity() : 0;
            Long optionId = isOptionStockTracked(line.itemOptionId()) ? line.itemOptionId() : null;
            if (optionId != null) {
                var option = giftOptionRepository.findById(optionId).orElseThrow();
                option.setOptionStockQuantity(option.getOptionStockQuantity() - need);
                option.setOptionSoldOutFlag(option.getOptionStockQuantity() <= 0 ? "Y" : "N");
                giftOptionRepository.save(option);
            } else {
                Gift gift = giftRepository.findById(line.itemId()).orElseThrow();
                gift.setStockQuantity(gift.getStockQuantity() - need);
                gift.setSoldOut(gift.getStockQuantity() <= 0 ? SOLD_OUT : IN_STOCK);
                giftRepository.save(gift);
            }

            com.ghlove.gift.domain.GiftShipmentStock reservation = new com.ghlove.gift.domain.GiftShipmentStock();
            reservation.setOrderItemId(line.orderItemId());
            reservation.setShipmentId(shipmentId);
            reservation.setItemId(line.itemId());
            reservation.setItemOptionId(optionId);
            reservation.setQuantity(need);
            reservation.setStatus(RESERVATION_RESERVED);
            reservation.setCreatedDate(now);
            giftShipmentStockRepository.save(reservation);
        }
        return true;
    }

    /** 옵션이 존재하고 재고연동(option_stock_flag='Y')이면 옵션단위 재고로 관리한다. */
    private boolean isOptionStockTracked(Long itemOptionId) {
        if (itemOptionId == null || itemOptionId <= 0) {
            return false;
        }
        return giftOptionRepository.findById(itemOptionId)
                .map(o -> "Y".equals(o.getOptionStockFlag()))
                .orElse(false);
    }

    /** order.saga의 {@code SHIPMENT_CANCELLED}에 반응해, 이 서비스가 실제 예약한 출고 재고만 복원한다. */
    @Transactional
    public void restoreStockForShipment(Long shipmentId) {
        List<com.ghlove.gift.domain.GiftShipmentStock> reservations =
                giftShipmentStockRepository.findByShipmentId(shipmentId);
        if (reservations.isEmpty()) {
            log.info("Shipment {} was never reserved here - nothing to restore", shipmentId);
            return;
        }
        for (var reservation : reservations) {
            restoreReservation(reservation);
        }
    }

    /** order.saga의 {@code ITEM_CANCELLED}(품목 단위 부분취소/반품)에 반응해 그 품목 재고만 복원한다. */
    @Transactional
    public void restoreStockForItem(Long orderItemId) {
        com.ghlove.gift.domain.GiftShipmentStock reservation =
                giftShipmentStockRepository.findById(orderItemId).orElse(null);
        if (reservation == null) {
            log.info("OrderItem {} was never reserved here - nothing to restore", orderItemId);
            return;
        }
        restoreReservation(reservation);
    }

    private void restoreReservation(com.ghlove.gift.domain.GiftShipmentStock reservation) {
        if (RESERVATION_RESTORED.equals(reservation.getStatus())) {
            return;
        }
        Long optionId = reservation.getItemOptionId();
        if (optionId != null && optionId > 0) {
            // 옵션단위 예약분 복원 - 그 옵션 재고만 되돌린다.
            giftOptionRepository.findById(optionId).ifPresent(option -> {
                int restored = (option.getOptionStockQuantity() == null ? 0 : option.getOptionStockQuantity())
                        + reservation.getQuantity();
                option.setOptionStockQuantity(restored);
                option.setOptionSoldOutFlag(restored <= 0 ? "Y" : "N");
                giftOptionRepository.save(option);
            });
        } else {
            giftRepository.findById(reservation.getItemId()).ifPresent(gift -> {
                gift.setStockQuantity(gift.getStockQuantity() + reservation.getQuantity());
                gift.setSoldOut(IN_STOCK);
                giftRepository.save(gift);
            });
        }
        reservation.setStatus(RESERVATION_RESTORED);
        giftShipmentStockRepository.save(reservation);
    }

    // ---- 추가구성 자식 답례품 (AS-IS ItemServiceImpl: itemAdditionFlag='Y'면 추가상품명/가격으로
    //      item_data_type=2 자식 OP_ITEM을 생성해 OP_ITEM_ADDITION으로 연결) ----

    /**
     * 추가구성 자식 답례품(item_data_type=2)을 생성한다. 본품의 판매자/지자체/카테고리를 상속하고,
     * 일반 답례품 목록·검색에는 뜨지 않게 displayFlag='N', 주문은 가능하게 dataStatusCode='APPROVED'로 둔다.
     */
    @Transactional
    public Gift createAdditionChild(Long parentItemId, String name, Integer price, Integer stockQuantity) {
        Gift parent = giftRepository.findById(parentItemId)
                .orElseThrow(() -> new GiftException("본품 답례품을 찾을 수 없습니다."));
        if (name == null || name.isBlank()) {
            throw new GiftException("추가상품명을 입력해 주세요.");
        }
        if (name.trim().length() > 40) {
            throw new GiftException("추가상품명은 최대 40자입니다.");
        }
        if (price == null || price < 0) {
            throw new GiftException("추가상품가격은 0원 이상이어야 합니다.");
        }
        int qty = stockQuantity != null && stockQuantity > 0 ? stockQuantity : 0;

        Gift child = new Gift();
        child.setSellerId(parent.getSellerId());
        child.setItemName(name.trim());
        child.setCategoryCode(parent.getCategoryCode());
        child.setLocgovCode(parent.getLocgovCode());
        child.setSalePrice(price);
        child.setStockQuantity(qty);
        child.setSoldOut(qty <= 0 ? SOLD_OUT : IN_STOCK);
        child.setDisplayFlag(DISPLAY_OFF);        // 목록/검색 숨김
        child.setDataStatusCode(STATUS_APPROVED); // 주문 가능
        child.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
        child.setDisplayType(DISPLAY_TYPE_ALWAYS);
        child.setItemLabel(LABEL_NONE);
        child.setItemDataType("2");
        return giftRepository.save(child);
    }

    /** 추가구성 교체/해제 시 자식 답례품을 지운다 - item_data_type='2'인 것만 안전 삭제한다. */
    @Transactional
    public void deleteAdditionChild(Long childItemId) {
        giftRepository.findById(childItemId).ifPresent(child -> {
            if ("2".equals(child.getItemDataType())) {
                giftRepository.deleteById(childItemId);
            }
        });
    }

    // ---- 답례품 상품관리 2단계 (엑셀 대량처리/일괄작업 - admin-console-item-mgmt-round #2).
    // 셀러별 개별 등록/수정만 있던 1단계를 대량 작업으로 확장한다. ----

    private static final java.util.Set<String> VALID_ITEM_LABELS = java.util.Set.of("1", "2", "3", "4");

    /** 엑셀 대량등록 - POI 등 엑셀 라이브러리가 프로젝트에 없어 CSV로 실행한다(OrderAdminService
     *  #exportCsv와 동일한 관행, 엑셀에서 그대로 열리는 실질적으로 동등한 결과물). 헤더행 1줄 +
     *  데이터행(판매자ID,답례품명,요약설명,상세설명,카테고리코드,지자체코드,판매가격,재고수량
     *  [,노출유형,노출시작일,노출종료일,최소기부금액,브랜드ID]) - 단순 콤마 분리라 값 안에
     *  콤마/따옴표가 있는 경우는 지원하지 않는다(관리자 내부 대량등록 도구로 범위를 좁힘).
     *  행 단위로 실패해도 나머지 행은 계속 처리하고, 실패한 행 번호+사유를 모아서 돌려준다. */
    @Transactional
    public BulkUploadResult bulkUploadCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GiftException("업로드할 CSV 파일을 선택해 주세요.");
        }
        List<String> errors = new ArrayList<>();
        int created = 0;
        int lineNo = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (lineNo == 1 || line.isBlank()) {
                    continue; // 헤더행/빈행 건너뜀
                }
                String[] c = line.split(",", -1);
                try {
                    if (c.length < 8) {
                        throw new GiftException("컬럼 수가 부족합니다(최소 8개: 판매자ID,답례품명,요약설명,상세설명,카테고리코드,지자체코드,판매가격,재고수량).");
                    }
                    Long sellerId = parseLong(c[0], "판매자ID");
                    String itemName = c[1].trim();
                    String itemSummary = blankToNull(c[2]);
                    String detailContent = blankToNull(c[3]);
                    String categoryCode = c[4].trim();
                    String locgovCode = blankToNull(c[5]);
                    Integer salePrice = parseInt(c[6], "판매가격");
                    Integer stockQuantity = parseInt(c[7], "재고수량");
                    String displayType = c.length > 8 ? blankToNull(c[8]) : null;
                    String displayStartDate = c.length > 9 ? blankToNull(c[9]) : null;
                    String displayEndDate = c.length > 10 ? blankToNull(c[10]) : null;
                    Integer minDonationAmount = c.length > 11 ? parseIntOrNull(c[11]) : null;
                    Integer brandId = c.length > 12 ? parseIntOrNull(c[12]) : null;
                    adminCreate(sellerId, itemName, itemSummary, detailContent, categoryCode, locgovCode,
                            salePrice, stockQuantity, displayType, displayStartDate, displayEndDate,
                            minDonationAmount, brandId, null);
                    created++;
                } catch (GiftException e) {
                    errors.add(lineNo + "행: " + e.getMessage());
                } catch (RuntimeException e) {
                    errors.add(lineNo + "행: 처리 중 오류가 발생했습니다(" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            throw new GiftException("CSV 파일을 읽는 중 오류가 발생했습니다.");
        }
        return new BulkUploadResult(created, errors);
    }

    private Long parseLong(String s, String field) {
        try {
            return Long.parseLong(s.trim());
        } catch (RuntimeException e) {
            throw new GiftException(field + " 형식이 올바르지 않습니다: " + s);
        }
    }

    private Integer parseInt(String s, String field) {
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException e) {
            throw new GiftException(field + " 형식이 올바르지 않습니다: " + s);
        }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /** 엑셀 다운로드(CSV) - 현재 검색조건의 상품 목록을 그대로 내려받는다. */
    public String exportCsv(String itemName, String categoryCode, Long sellerId, String dataStatusCode) {
        List<Gift> items = adminSearch(itemName, categoryCode, sellerId, dataStatusCode);
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("ID,답례품명,카테고리,판매자ID,지자체코드,판매가격,재고수량,상태,노출,라벨,브랜드ID,등록일\n");
        for (Gift g : items) {
            sb.append(csvCell(String.valueOf(g.getItemId()))).append(',')
                    .append(csvCell(g.getItemName())).append(',')
                    .append(csvCell(g.getCategoryCode())).append(',')
                    .append(csvCell(String.valueOf(g.getSellerId()))).append(',')
                    .append(csvCell(g.getLocgovCode())).append(',')
                    .append(csvCell(String.valueOf(g.getSalePrice()))).append(',')
                    .append(csvCell(String.valueOf(g.getStockQuantity()))).append(',')
                    .append(csvCell(g.getDataStatusCode())).append(',')
                    .append(csvCell(g.getDisplayFlag())).append(',')
                    .append(csvCell(g.getItemLabel())).append(',')
                    .append(csvCell(g.getBrandId() == null ? "" : String.valueOf(g.getBrandId()))).append(',')
                    .append(csvCell(g.getCreatedDate()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /** 노출 on/off 일괄변경. */
    @Transactional
    public int bulkSetDisplay(List<Long> itemIds, String displayFlag) {
        if (!DISPLAY_ON.equals(displayFlag) && !DISPLAY_OFF.equals(displayFlag)) {
            throw new GiftException("노출값은 Y 또는 N이어야 합니다.");
        }
        List<Gift> gifts = giftRepository.findAllById(itemIds);
        gifts.forEach(g -> g.setDisplayFlag(displayFlag));
        giftRepository.saveAll(gifts);
        return gifts.size();
    }

    /** 상품라벨(1:없음,2:NEW,3:SALE,4:사기) 일괄변경 - AS-IS OP_ITEM.ITEM_LABEL. */
    @Transactional
    public int bulkSetLabel(List<Long> itemIds, String itemLabel) {
        if (!VALID_ITEM_LABELS.contains(itemLabel)) {
            throw new GiftException("올바른 라벨값이 아닙니다(1:없음,2:NEW,3:SALE,4:사기).");
        }
        List<Gift> gifts = giftRepository.findAllById(itemIds);
        gifts.forEach(g -> g.setItemLabel(itemLabel));
        giftRepository.saveAll(gifts);
        return gifts.size();
    }

    /** 일괄 삭제 - AS-IS의 물리적 DELETE 대신 폐지(DISCONTINUED) 처리한다. OP_ITEM을 참조하는
     *  레거시 FK 14개 테이블(OP_ITEM_INFO 등)과 이 서비스 자체의 OP_ITEM_IMAGE/GIFT_ORDER_STOCK이
     *  있어 물리적 삭제는 위험하고, 셀러 self-service의 discontinue()와 같은 "영구 종료" 의미로
     *  충분히 "삭제"를 대체한다(관리자 대량작업이라 소유권 검증은 하지 않는다). */
    @Transactional
    public int bulkDelete(List<Long> itemIds) {
        List<Gift> gifts = giftRepository.findAllById(itemIds);
        gifts.forEach(g -> {
            g.setDataStatusCode(STATUS_DISCONTINUED);
            g.setDisplayFlag(DISPLAY_OFF);
        });
        giftRepository.saveAll(gifts);
        gifts.forEach(giftLifecyclePublisher::publish);
        return gifts.size();
    }

    /** 순서변경 - 목록 노출순서(ADMIN_ORDERING, 값이 작을수록 상단)를 한 번에 여러 건 저장한다
     *  (up/down 스왑 대신 직접 값 입력 - 초기값이 전부 0이라 스왑만으로는 정렬을 벗어날 수
     *  없어 이 방식을 택함). */
    @Transactional
    public int bulkUpdateOrdering(Map<Long, Integer> orderingByItemId) {
        List<Gift> gifts = giftRepository.findAllById(orderingByItemId.keySet());
        gifts.forEach(g -> g.setAdminOrdering(orderingByItemId.get(g.getItemId())));
        giftRepository.saveAll(gifts);
        return gifts.size();
    }

    /** 판매정보(가격/재고) 일괄수정 - 선택한 여러 상품의 값을 한 번에 저장한다. */
    @Transactional
    public int bulkUpdateSales(List<SalesUpdate> updates) {
        int count = 0;
        for (SalesUpdate u : updates) {
            if (u.itemId() == null) {
                continue;
            }
            Gift gift = giftRepository.findById(u.itemId()).orElse(null);
            if (gift == null) {
                continue;
            }
            if (u.salePrice() != null) {
                if (u.salePrice() <= 0) {
                    throw new GiftException("가격은 0보다 커야 합니다(itemId=" + u.itemId() + ").");
                }
                gift.setSalePrice(u.salePrice());
            }
            if (u.stockQuantity() != null) {
                if (u.stockQuantity() < 0) {
                    throw new GiftException("재고 수량은 0 이상이어야 합니다(itemId=" + u.itemId() + ").");
                }
                gift.setStockQuantity(u.stockQuantity());
                gift.setSoldOut(u.stockQuantity() <= 0 ? SOLD_OUT : IN_STOCK);
            }
            giftRepository.save(gift);
            count++;
        }
        return count;
    }

    public record BulkUploadResult(int created, List<String> errors) {
    }

    public record SalesUpdate(Long itemId, Integer salePrice, Integer stockQuantity) {
    }
}
