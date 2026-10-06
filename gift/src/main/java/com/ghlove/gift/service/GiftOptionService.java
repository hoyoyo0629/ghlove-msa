package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.GiftAddition;
import com.ghlove.gift.domain.GiftAdditionId;
import com.ghlove.gift.domain.GiftOption;
import com.ghlove.gift.repository.GiftAdditionRepository;
import com.ghlove.gift.repository.GiftOptionRepository;
import com.ghlove.gift.repository.GiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 답례품 옵션 카탈로그 관리 (SFR-005) - {@link com.ghlove.gift.domain.GiftOption} 참고.
 * AS-IS와 동일하게 선택형(S)/조합형(S2·S3)/텍스트형(T) + 각인(필수 추가정보) + 추가구성을
 * 전부 지원한다(docs/gift-option-redesign-plan.md). 판매자 화면에서 S2·T 숨김은 화면단 책임이고
 * 서비스는 전 형태를 처리한다.
 */
@Service
@RequiredArgsConstructor
public class GiftOptionService {

    private static final String STOCK_ON = "Y";
    private static final String STOCK_OFF = "N";
    private static final String DISPLAY_ON = "Y";
    private static final String DISPLAY_OFF = "N";
    private static final String USE_ON = "Y";
    private static final String USE_OFF = "N";
    private static final String OPTION_TYPE_SINGLE = "S";
    private static final String OPTION_TYPE_COMBO2 = "S2";
    private static final String OPTION_TYPE_COMBO3 = "S3";
    private static final String OPTION_TYPE_TEXT = "T";
    private static final List<String> OPTION_TYPES = List.of("S", "S2", "S3", "T");
    /** 필수 추가정보 제목 금지문자(AS-IS form.jsp): 콜론/수직선/홑화살괄호. */
    private static final String TEXT_TITLE_FORBIDDEN = ":|<>";
    private static final int TEXT_TITLE_MAX_LEN = 30;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final GiftOptionRepository giftOptionRepository;
    private final GiftRepository giftRepository;
    private final GiftAdditionRepository giftAdditionRepository;
    private final GiftService giftService;

    /** 조합형/텍스트형 옵션 한 줄. name2/3는 형태에 따라 null 가능. */
    public record OptionRow(String optionName1, String optionName2, String optionName3,
                            Integer optionPrice, boolean stockTracked, Integer stockQuantity,
                            boolean hidden) {
    }

    /** 추가구성 한 줄(AS-IS 추가상품명/가격/재고). item_data_type=2 자식 답례품으로 생성된다. */
    public record AdditionRow(String additionItemName, Integer additionSalePrice, Integer stockQuantity) {
    }

    /** 답례품 상세화면용 - 숨김 처리된 옵션은 제외. */
    public List<GiftOption> optionsOf(Long itemId) {
        return giftOptionRepository.findByItemIdAndOptionDisplayFlagOrderByItemOptionIdAsc(itemId, DISPLAY_ON);
    }

    /** admin 관리화면용 - 숨김 옵션도 포함해 전부 보여준다. */
    public List<GiftOption> adminOptionsOf(Long itemId) {
        return giftOptionRepository.findByItemIdOrderByItemOptionIdAsc(itemId);
    }

    /**
     * 표시 옵션이 <b>전부 품절</b>인 답례품 ID 집합 (AS-IS op_item_option_soldout 요약이 하던
     * "아이템 단위 옵션 품절" 판정을 조회시점에 직접 계산). 목록 카드 품절뱃지에 쓴다 -
     * 옵션 재고는 소진됐지만 아이템 재고(op_item.stock_quantity)는 남은 경우를 잡는다.
     */
    public java.util.Set<Long> optionSoldOutItemIds(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return java.util.Set.of();
        }
        java.util.Map<Long, List<GiftOption>> byItem = giftOptionRepository
                .findByItemIdInAndOptionDisplayFlag(itemIds, DISPLAY_ON).stream()
                .collect(java.util.stream.Collectors.groupingBy(GiftOption::getItemId));
        java.util.Set<Long> result = new java.util.HashSet<>();
        for (var e : byItem.entrySet()) {
            List<GiftOption> opts = e.getValue();
            if (!opts.isEmpty() && opts.stream().allMatch(o -> "Y".equals(o.getOptionSoldOutFlag()))) {
                result.add(e.getKey());
            }
        }
        return result;
    }

    /** 추가구성으로 편성된 답례품 목록(존재하는 것만). 구매자 상세/장바구니 담기에서 사용. */
    public List<Gift> additionsOf(Long itemId) {
        List<Gift> result = new ArrayList<>();
        for (GiftAddition addition : giftAdditionRepository.findByItemId(itemId)) {
            giftRepository.findById(addition.getAdditionItemId()).ifPresent(result::add);
        }
        return result;
    }

    @Transactional
    public GiftOption register(Long itemId, String optionName, Integer optionPrice, boolean stockTracked,
                                Integer stockQuantity) {
        if (!giftRepository.existsById(itemId)) {
            throw new GiftException("답례품을 찾을 수 없습니다.");
        }
        if (optionName == null || optionName.isBlank()) {
            throw new GiftException("옵션명을 입력해 주세요.");
        }
        if (optionPrice != null && optionPrice < 0) {
            throw new GiftException("추가금액은 0원 이상이어야 합니다.");
        }

        GiftOption option = new GiftOption();
        option.setItemId(itemId);
        option.setOptionType(OPTION_TYPE_SINGLE);
        option.setOptionName1(optionName);
        option.setOptionPrice(optionPrice != null ? optionPrice : 0);
        applyStock(option, stockTracked, stockQuantity);
        option.setOptionDisplayFlag(DISPLAY_ON);
        option.setCreatedUserId(0L);
        option.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        return giftOptionRepository.save(option);
    }

    @Transactional
    public GiftOption edit(Long itemOptionId, String optionName, Integer optionPrice, boolean stockTracked,
                            Integer stockQuantity) {
        GiftOption option = getOrThrow(itemOptionId);
        if (optionName == null || optionName.isBlank()) {
            throw new GiftException("옵션명을 입력해 주세요.");
        }
        if (optionPrice != null && optionPrice < 0) {
            throw new GiftException("추가금액은 0원 이상이어야 합니다.");
        }
        option.setOptionName1(optionName);
        option.setOptionPrice(optionPrice != null ? optionPrice : 0);
        applyStock(option, stockTracked, stockQuantity);
        return giftOptionRepository.save(option);
    }

    @Transactional
    public void delete(Long itemOptionId) {
        giftOptionRepository.deleteById(itemOptionId);
    }

    @Transactional
    public GiftOption setDisplay(Long itemOptionId, boolean display) {
        GiftOption option = getOrThrow(itemOptionId);
        option.setOptionDisplayFlag(display ? DISPLAY_ON : DISPLAY_OFF);
        return giftOptionRepository.save(option);
    }

    /**
     * 옵션형태(S/S2/S3/T)와 옵션행 전체를 받아 해당 답례품의 옵션을 통째로 교체한다
     * (AS-IS: deleteItemOptionByItemId 후 insertItemOption 반복). rows가 비면 옵션 미사용(N)으로 되돌린다.
     */
    @Transactional
    public List<GiftOption> saveOptions(Long itemId, String optionType, List<OptionRow> rows) {
        Gift gift = giftRepository.findById(itemId)
                .orElseThrow(() -> new GiftException("답례품을 찾을 수 없습니다."));

        giftOptionRepository.deleteByItemId(itemId);

        if (rows == null || rows.isEmpty()) {
            gift.setItemOptionFlag(USE_OFF);
            gift.setItemOptionType(null);
            giftRepository.save(gift);
            return List.of();
        }
        if (optionType == null || !OPTION_TYPES.contains(optionType)) {
            throw new GiftException("옵션형태가 올바르지 않습니다.");
        }

        String now = DATE_FORMAT.format(LocalDateTime.now());
        List<GiftOption> saved = new ArrayList<>();
        for (OptionRow row : rows) {
            validateRow(optionType, row);
            GiftOption option = new GiftOption();
            option.setItemId(itemId);
            option.setOptionType(optionType);
            option.setOptionName1(trimToEmpty(row.optionName1()));
            option.setOptionName2(OPTION_TYPE_SINGLE.equals(optionType) || OPTION_TYPE_TEXT.equals(optionType)
                    ? null : trimToNull(row.optionName2()));
            option.setOptionName3(OPTION_TYPE_COMBO3.equals(optionType) ? trimToNull(row.optionName3()) : null);
            option.setOptionPrice(nonNegative(row.optionPrice()));
            applyStock(option, row.stockTracked(), row.stockQuantity());
            option.setOptionHideFlag(row.hidden() ? USE_ON : USE_OFF);
            option.setOptionDisplayFlag(row.hidden() ? DISPLAY_OFF : DISPLAY_ON);
            option.setCreatedUserId(0L);
            option.setCreatedDate(now);
            saved.add(giftOptionRepository.save(option));
        }

        gift.setItemOptionFlag(USE_ON);
        gift.setItemOptionType(optionType);
        giftRepository.save(gift);
        return saved;
    }

    /**
     * 각인(필수 추가정보, itemTextOptionFlag/Title1~3) 저장. AS-IS와 동일하게 옵션형태(S/S2/S3/T)와
     * 독립적으로 답례품에 붙는다. 제목은 최대 3개, 각 30자 이하, `: | < >` 금지.
     */
    @Transactional
    public Gift saveTextOptions(Long itemId, boolean use, String title1, String title2, String title3) {
        Gift gift = giftRepository.findById(itemId)
                .orElseThrow(() -> new GiftException("답례품을 찾을 수 없습니다."));
        if (!use) {
            gift.setItemTextOptionFlag(USE_OFF);
            gift.setItemTextOptionTitle1(null);
            gift.setItemTextOptionTitle2(null);
            gift.setItemTextOptionTitle3(null);
            return giftRepository.save(gift);
        }
        String t1 = validateTextTitle(title1);
        String t2 = validateTextTitle(title2);
        String t3 = validateTextTitle(title3);
        if (t1 == null) {
            throw new GiftException("필수 추가정보 제목을 1개 이상 입력해 주세요.");
        }
        gift.setItemTextOptionFlag(USE_ON);
        gift.setItemTextOptionTitle1(t1);
        gift.setItemTextOptionTitle2(t2);
        gift.setItemTextOptionTitle3(t3);
        return giftRepository.save(gift);
    }

    /**
     * 추가구성 편성 (AS-IS 동일). 추가상품명/가격/재고로 <b>item_data_type=2 자식 답례품을 생성</b>하고
     * op_item_addition으로 본품에 연결한다. 저장 시 기존 추가구성(링크+자식 답례품)을 통째로 교체한다.
     */
    @Transactional
    public void saveAdditions(Long itemId, List<AdditionRow> rows) {
        Gift gift = giftRepository.findById(itemId)
                .orElseThrow(() -> new GiftException("답례품을 찾을 수 없습니다."));

        // 기존 추가구성 링크 + 자식 답례품 제거(교체)
        List<GiftAddition> existing = giftAdditionRepository.findByItemId(itemId);
        giftAdditionRepository.deleteByItemId(itemId);
        for (GiftAddition ga : existing) {
            giftService.deleteAdditionChild(ga.getAdditionItemId());
        }

        if (rows == null || rows.isEmpty()) {
            gift.setItemAdditionFlag(USE_OFF);
            giftRepository.save(gift);
            return;
        }
        for (AdditionRow row : rows) {
            Gift child = giftService.createAdditionChild(itemId, row.additionItemName(),
                    row.additionSalePrice(), row.stockQuantity());
            GiftAddition addition = new GiftAddition();
            addition.setItemId(itemId);
            addition.setAdditionItemId(child.getItemId());
            giftAdditionRepository.save(addition);
        }
        gift.setItemAdditionFlag(USE_ON);
        giftRepository.save(gift);
    }

    private void validateRow(String optionType, OptionRow row) {
        if (row.optionName1() == null || row.optionName1().isBlank()) {
            throw new GiftException("옵션값을 입력해 주세요.");
        }
        if ((OPTION_TYPE_COMBO2.equals(optionType) || OPTION_TYPE_COMBO3.equals(optionType))
                && (row.optionName2() == null || row.optionName2().isBlank())) {
            throw new GiftException("조합형 2단계 옵션값을 입력해 주세요.");
        }
        if (OPTION_TYPE_COMBO3.equals(optionType)
                && (row.optionName3() == null || row.optionName3().isBlank())) {
            throw new GiftException("3조합형 3단계 옵션값을 입력해 주세요.");
        }
        if (row.optionPrice() != null && row.optionPrice() < 0) {
            throw new GiftException("추가금액은 0원 이상이어야 합니다.");
        }
    }

    private String validateTextTitle(String title) {
        String t = trimToNull(title);
        if (t == null) {
            return null;
        }
        if (t.length() > TEXT_TITLE_MAX_LEN) {
            throw new GiftException("필수 추가정보 제목은 최대 " + TEXT_TITLE_MAX_LEN + "자입니다.");
        }
        for (int i = 0; i < TEXT_TITLE_FORBIDDEN.length(); i++) {
            if (t.indexOf(TEXT_TITLE_FORBIDDEN.charAt(i)) >= 0) {
                throw new GiftException("필수 추가정보 제목에 : | < > 는 사용할 수 없습니다.");
            }
        }
        return t;
    }

    private int nonNegative(Integer value) {
        return value != null && value > 0 ? value : 0;
    }

    private String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void applyStock(GiftOption option, boolean stockTracked, Integer stockQuantity) {
        option.setOptionStockFlag(stockTracked ? STOCK_ON : STOCK_OFF);
        int quantity = stockTracked && stockQuantity != null ? stockQuantity : 0;
        option.setOptionStockQuantity(quantity);
        option.setOptionSoldOutFlag(stockTracked && quantity <= 0 ? "Y" : "N");
    }

    /** order 서비스가 장바구니 담기 시 옵션명·추가금액을 신뢰성 있게 스냅샷하려고 조회한다. */
    public GiftOption optionById(Long itemOptionId) {
        return getOrThrow(itemOptionId);
    }

    private GiftOption getOrThrow(Long itemOptionId) {
        return giftOptionRepository.findById(itemOptionId)
                .orElseThrow(() -> new GiftException("옵션을 찾을 수 없습니다."));
    }
}
