package com.ghlove.gift.web;

import com.ghlove.gift.domain.GiftOption;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** 답례품 옵션 관리 (SFR-005 재검토 라운드) - admin 콘솔(/admin/gift-items/{id}/edit)이
 *  이 cross-service API를 사용한다. */
@RestController
@RequestMapping("/api/admin/gift-items/{itemId}/options")
@RequiredArgsConstructor
public class GiftOptionAdminApiController {

    private final GiftOptionService giftOptionService;

    @GetMapping
    public List<GiftOption> list(@PathVariable Long itemId) {
        return giftOptionService.adminOptionsOf(itemId);
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
