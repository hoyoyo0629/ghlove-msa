package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.ThumbnailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 답례품 상품 관리자 직접 CRUD (AS-IS opmanager/item - ItemManagerController 1~2단계 대응)
 * cross-service API - admin 콘솔(/admin/gift-items)이 사용한다. 셀러 self-service
 * (GiftController#register/edit)와 달리 관리자가 셀러를 대신 지정해 직접 생성/수정하고,
 * 카테고리 일괄 배정/상품 복사(1단계)에 이어 엑셀 대량등록/다운로드, 노출·라벨·순서·판매정보
 * 일괄변경, 일괄삭제(2단계, admin-console-item-mgmt-round #2)를 지원한다.
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

    // ---------------------------------------------------------------- 2단계: 엑셀 대량처리/일괄작업

    @PostMapping("/bulk-upload")
    public GiftService.BulkUploadResult bulkUpload(@RequestParam MultipartFile file) {
        try {
            return giftService.bulkUploadCsv(file);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String itemName,
                                          @RequestParam(required = false) String categoryCode,
                                          @RequestParam(required = false) Long sellerId,
                                          @RequestParam(required = false) String dataStatusCode) {
        String csv = giftService.exportCsv(itemName, categoryCode, sellerId, dataStatusCode);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "gift-items.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @PostMapping("/bulk-display")
    public Map<String, Integer> bulkDisplay(@RequestParam List<Long> itemIds, @RequestParam String displayFlag) {
        try {
            return Map.of("updated", giftService.bulkSetDisplay(itemIds, displayFlag));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/bulk-label")
    public Map<String, Integer> bulkLabel(@RequestParam List<Long> itemIds, @RequestParam String itemLabel) {
        try {
            return Map.of("updated", giftService.bulkSetLabel(itemIds, itemLabel));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/bulk-delete")
    public Map<String, Integer> bulkDelete(@RequestParam List<Long> itemIds) {
        return Map.of("updated", giftService.bulkDelete(itemIds));
    }

    @PostMapping("/bulk-ordering")
    public Map<String, Integer> bulkOrdering(@RequestParam List<Long> itemIds, @RequestParam List<Integer> orderings) {
        if (itemIds.size() != orderings.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemIds와 orderings 개수가 일치하지 않습니다.");
        }
        Map<Long, Integer> orderingByItemId = new java.util.LinkedHashMap<>();
        for (int i = 0; i < itemIds.size(); i++) {
            orderingByItemId.put(itemIds.get(i), orderings.get(i));
        }
        return Map.of("updated", giftService.bulkUpdateOrdering(orderingByItemId));
    }

    @PostMapping("/bulk-sales")
    public Map<String, Integer> bulkSales(@RequestBody List<GiftService.SalesUpdate> updates) {
        try {
            return Map.of("updated", giftService.bulkUpdateSales(updates));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** 배송비/택배사 설정 (SFR-005 재검토 라운드). */
    @PostMapping("/{id}/shipping")
    public Gift updateShipping(@PathVariable Long id, @RequestParam(required = false) String deliveryCompanyName,
                                @RequestParam String shippingType, @RequestParam(required = false) Integer shipping,
                                @RequestParam(required = false) Integer shippingFreeAmount,
                                @RequestParam(required = false) Integer shippingExtraCharge1,
                                @RequestParam(required = false) Integer shippingExtraCharge2,
                                @RequestParam(required = false, defaultValue = "true") boolean itemReturnAllowed) {
        try {
            return giftService.updateShippingPolicy(id, deliveryCompanyName, shippingType, shipping,
                    shippingFreeAmount, shippingExtraCharge1, shippingExtraCharge2, itemReturnAllowed);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 지자체 승인/반려, 대표상품관리
    // (SFR-005 재검토 라운드 - 원래 gift 자체에 무인증 화면(/admin, /gifts/{id}/approve,
    // /representative/*)으로 임시 노출돼 있던 것을 admin 콘솔 전용 API로 이관)

    @GetMapping("/pending")
    public List<Gift> pending() {
        return giftService.pendingGifts();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(giftService.approve(id));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(giftService.reject(id));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/representative")
    public List<Gift> representative() {
        return giftService.representativeGifts();
    }

    @PostMapping("/{id}/representative/register")
    public ResponseEntity<?> registerRepresentative(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(giftService.registerRepresentative(id));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/representative/delete")
    public ResponseEntity<?> unregisterRepresentative(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(giftService.unregisterRepresentative(id));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
