package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.ThumbnailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 답례품 상품 관리자 직접 CRUD (AS-IS opmanager/item - ItemManagerController 1단계 대응)
 * cross-service API - admin 콘솔(/admin/gift-items)이 사용한다. 셀러 self-service
 * (GiftController#register/edit)와 달리 관리자가 셀러를 대신 지정해 직접 생성/수정하고,
 * 카테고리 일괄 배정/상품 복사를 지원한다. 엑셀 대량등록·리뷰 관리자 대응은 이번 라운드
 * 범위 밖(as-is-admin-gap-deep-audit-part1.md #60 참고, 다음 라운드로 명시적으로 미룸).
 */
@RestController
@RequestMapping("/api/admin/gift-items")
@RequiredArgsConstructor
public class ItemAdminApiController {

    private final GiftService giftService;
    private final ThumbnailService thumbnailService;

    @GetMapping
    public List<Gift> search(@RequestParam(required = false) String itemName,
                              @RequestParam(required = false) String categoryCode,
                              @RequestParam(required = false) Long sellerId,
                              @RequestParam(required = false) String dataStatusCode) {
        return giftService.adminSearch(itemName, categoryCode, sellerId, dataStatusCode);
    }

    @GetMapping("/{id}")
    public Gift get(@PathVariable Long id) {
        try {
            return giftService.detail(id);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public Gift create(@RequestParam Long sellerId, @RequestParam String itemName,
                        @RequestParam(required = false) String itemSummary,
                        @RequestParam(required = false) String detailContent,
                        @RequestParam String categoryCode, @RequestParam(required = false) String locgovCode,
                        @RequestParam Integer salePrice, @RequestParam Integer stockQuantity,
                        @RequestParam(required = false) String displayType,
                        @RequestParam(required = false) String displayStartDate,
                        @RequestParam(required = false) String displayEndDate,
                        @RequestParam(required = false) Integer minDonationAmount,
                        @RequestParam(required = false) Integer brandId,
                        @RequestParam(required = false) List<MultipartFile> images) {
        try {
            Gift gift = giftService.adminCreate(sellerId, itemName, itemSummary, detailContent, categoryCode,
                    locgovCode, salePrice, stockQuantity, displayType, displayStartDate, displayEndDate,
                    minDonationAmount, brandId, images);
            giftService.imagesOf(gift.getItemId())
                    .forEach(img -> thumbnailService.generateThumbnails(img.getItemImageId(), img.getImageName()));
            return gift;
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Gift update(@PathVariable Long id, @RequestParam Long sellerId, @RequestParam String itemName,
                        @RequestParam(required = false) String itemSummary,
                        @RequestParam(required = false) String detailContent,
                        @RequestParam String categoryCode, @RequestParam Integer salePrice,
                        @RequestParam Integer stockQuantity,
                        @RequestParam(required = false) String displayType,
                        @RequestParam(required = false) String displayStartDate,
                        @RequestParam(required = false) String displayEndDate,
                        @RequestParam(required = false) Integer minDonationAmount,
                        @RequestParam(required = false) Integer brandId) {
        try {
            return giftService.adminEdit(id, sellerId, itemName, itemSummary, detailContent, categoryCode,
                    salePrice, stockQuantity, displayType, displayStartDate, displayEndDate, minDonationAmount, brandId);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/bulk-category")
    public Map<String, Integer> bulkCategory(@RequestParam List<Long> itemIds, @RequestParam String categoryCode) {
        try {
            return Map.of("updated", giftService.bulkAssignCategory(itemIds, categoryCode));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/copy")
    public Gift copy(@PathVariable Long id) {
        try {
            return giftService.copy(id);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
