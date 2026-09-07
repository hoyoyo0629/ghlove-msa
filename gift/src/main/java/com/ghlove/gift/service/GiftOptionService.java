package com.ghlove.gift.service;

import com.ghlove.gift.domain.GiftOption;
import com.ghlove.gift.repository.GiftOptionRepository;
import com.ghlove.gift.repository.GiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 답례품 옵션 카탈로그 관리 (SFR-005) - {@link com.ghlove.gift.domain.GiftOption} 참고,
 * 등록/수정/재고표시만 다루고 실제 구매(장바구니/주문)에는 아직 반영되지 않는 의도적
 * 스코프 축소.
 */
@Service
@RequiredArgsConstructor
public class GiftOptionService {

    private static final String STOCK_ON = "Y";
    private static final String STOCK_OFF = "N";
    private static final String DISPLAY_ON = "Y";
    private static final String DISPLAY_OFF = "N";
    private static final String OPTION_TYPE_SINGLE = "S";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final GiftOptionRepository giftOptionRepository;
    private final GiftRepository giftRepository;

    /** 답례품 상세화면용 - 숨김 처리된 옵션은 제외. */
    public List<GiftOption> optionsOf(Long itemId) {
        return giftOptionRepository.findByItemIdAndOptionDisplayFlagOrderByItemOptionIdAsc(itemId, DISPLAY_ON);
    }

    /** admin 관리화면용 - 숨김 옵션도 포함해 전부 보여준다. */
    public List<GiftOption> adminOptionsOf(Long itemId) {
        return giftOptionRepository.findByItemIdOrderByItemOptionIdAsc(itemId);
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

    private void applyStock(GiftOption option, boolean stockTracked, Integer stockQuantity) {
        option.setOptionStockFlag(stockTracked ? STOCK_ON : STOCK_OFF);
        int quantity = stockTracked && stockQuantity != null ? stockQuantity : 0;
        option.setOptionStockQuantity(quantity);
        option.setOptionSoldOutFlag(stockTracked && quantity <= 0 ? "Y" : "N");
    }

    private GiftOption getOrThrow(Long itemOptionId) {
        return giftOptionRepository.findById(itemOptionId)
                .orElseThrow(() -> new GiftException("옵션을 찾을 수 없습니다."));
    }
}
