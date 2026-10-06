package com.ghlove.gift.web;

import com.ghlove.gift.domain.GiftOption;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftOptionService;
import com.ghlove.gift.service.GiftOptionService.OptionRow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** 답례품 옵션 관리 (SFR-005 재검토 라운드) - admin 콘솔(/admin/gift-items/{id}/edit)이
 *  이 cross-service API를 사용한다. AS-IS와 동일하게 S/S2/S3/T 조합저장 + 각인(필수추가정보)
 *  + 추가구성 벌크 편성을 지원한다. (Phase 2에서 gift 판매자 포털도 이 API를 소비하며,
 *  판매자 경로 노출 시 SellerApiController처럼 currentSellerId 소유권 검사를 얹는다.) */
@RestController
@RequestMapping("/api/admin/gift-items/{itemId}/options")
@RequiredArgsConstructor
public class GiftOptionAdminApiController {

    private final GiftOptionService giftOptionService;

    /** 조합형 옵션 저장 요청(옵션형태 + 옵션행 전체 교체). */
    public record OptionsRequest(String optionType, List<OptionRow> rows) {
    }

    /** 각인(필수 추가정보) 저장 요청. */
    public record TextOptionsRequest(boolean use, String title1, String title2, String title3) {
    }

    /** 추가구성 편성 요청 (추가상품명/가격/재고 → item_data_type=2 자식 답례품 생성). */
    public record AdditionsRequest(List<GiftOptionService.AdditionRow> rows) {
    }

    @GetMapping
    public List<GiftOption> list(@PathVariable Long itemId) {
        return giftOptionService.adminOptionsOf(itemId);
    }

    /** S/S2/S3/T 옵션 전체 저장(교체). rows가 비면 옵션 미사용으로 되돌린다. */
    @PutMapping("/bulk")
    public List<GiftOption> saveOptions(@PathVariable Long itemId, @RequestBody OptionsRequest req) {
        try {
            return giftOptionService.saveOptions(itemId, req.optionType(), req.rows());
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** 각인(필수 추가정보) 저장. */
    @PutMapping("/text")
    public void saveTextOptions(@PathVariable Long itemId, @RequestBody TextOptionsRequest req) {
        try {
            giftOptionService.saveTextOptions(itemId, req.use(), req.title1(), req.title2(), req.title3());
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** 추가구성 편성 저장. */
    @PutMapping("/additions")
    public void saveAdditions(@PathVariable Long itemId, @RequestBody AdditionsRequest req) {
        try {
            giftOptionService.saveAdditions(itemId, req.rows());
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping
    public GiftOption register(@PathVariable Long itemId, @RequestParam String optionName,
                                @RequestParam(required = false) Integer optionPrice,
                                @RequestParam(required = false, defaultValue = "false") boolean stockTracked,
                                @RequestParam(required = false) Integer stockQuantity) {
        try {
            return giftOptionService.register(itemId, optionName, optionPrice, stockTracked, stockQuantity);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{optionId}")
    public GiftOption edit(@PathVariable Long itemId, @PathVariable Long optionId, @RequestParam String optionName,
                            @RequestParam(required = false) Integer optionPrice,
                            @RequestParam(required = false, defaultValue = "false") boolean stockTracked,
                            @RequestParam(required = false) Integer stockQuantity) {
        try {
            return giftOptionService.edit(optionId, optionName, optionPrice, stockTracked, stockQuantity);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{optionId}/delete")
    public void delete(@PathVariable Long itemId, @PathVariable Long optionId) {
        giftOptionService.delete(optionId);
    }

    @PostMapping("/{optionId}/display")
    public GiftOption display(@PathVariable Long itemId, @PathVariable Long optionId, @RequestParam boolean display) {
        try {
            return giftOptionService.setDisplay(optionId, display);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
