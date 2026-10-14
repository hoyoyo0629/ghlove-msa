package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.repository.OffRprsRepository;
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
    private final OffRprsRepository offRprsRepository;

    @GetMapping
    public List<Gift> search(@RequestParam(required = false) String itemName,
                              @RequestParam(required = false) String categoryCode,
                              @RequestParam(required = false) Long sellerId,
                              @RequestParam(required = false) String dataStatusCode) {
        return giftService.adminSearch(itemName, categoryCode, sellerId, dataStatusCode);
    }

    /**
     * 기탁서 등록(admin 15102) 대표답례품 - AS-IS {@code OffgiveServiceImpl.selectOffRprs} 그대로.
     * 출처는 지자체 담당자가 답례품 목록 화면에서 지정하는 G_LCLGV_OFF_RPRS_GDS_MNG(지자체당 최대
     * 2개)다 - OP_ITEM의 대표상품 여부 컬럼이 아니다. 옵션 문자열(S: 4토막, S3: 8토막)로 옵션
     * 한 건을 찾아 붙이고(못 찾으면 changed_option=Y), 재고 1~9개인 건 뺀다(-1 무제한·0은 남긴다).
     * 남는 게 없으면 null(AS-IS와 같다).
     *
     * <p>TO-BE 번역 두 가지: ① {@code load_image}는 AS-IS ShopUtils.loadImage 경로 대신 이
     * 서비스의 대표이미지(/uploads/..) ② 승인상태는 화면이 AS-IS 코드("1")로 비교하므로
     * APPROVED를 "1"로 바꿔 내려준다.
     */
    @GetMapping("/off-rprs")
    public List<Map<String, Object>> offRprs(@RequestParam("lclgv_cd") String lclgvCd) {
        List<Map<String, Object>> result = offRprsRepository.selectOffRprs(lclgvCd);
        if (result == null || result.isEmpty()) {
            return null;
        }
        List<Long> itemIds = result.stream().map(r -> ((Number) r.get("gds_id")).longValue()).toList();
        Map<Long, String> thumbnails = giftService.thumbnailsOf(itemIds);

        Map<String, Object> s = new java.util.HashMap<>();
        List<Integer> removeIndex = new java.util.ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            Map<String, Object> row = result.get(i);
            String option = (String) row.get("options");
            String thumbnail = thumbnails.get(((Number) row.get("gds_id")).longValue());
            row.put("load_image", thumbnail != null ? "/uploads/" + thumbnail : "");
            if ("APPROVED".equals(row.get("data_status_code"))) {
                row.put("data_status_code", "1");
            }
            // AS-IS OP_ITEM.ITEM_OPTION_FLAG는 항상 Y/N인데 TO-BE는 옵션을 저장한 적 없는 상품이 NULL이다.
            // 화면 JS가 'N'일 때만 상품 재고로 최대주문수량을 그리므로(아니면 옵션 재고), NULL = 옵션 없음(N).
            if (row.get("item_option_flag") == null) {
                row.put("item_option_flag", "N");
            }

            if (option != null && !option.isEmpty()) {
                String[] sp = option.split("\\|\\|");
                if (sp.length == 4) {
                    s.put("gdsId", row.get("gds_id"));
                    s.put("optionType", sp[0]);
                    s.put("optionName2", sp[2]);
                    s.put("optionPrice", sp[3]);
                }
                if (sp.length == 8) {
                    s.put("gdsId", row.get("gds_id"));
                    s.put("optionType", sp[0]);
                    s.put("optionName1", sp[2]);
                    s.put("optionName2", sp[4]);
                    s.put("optionName3", sp[6]);
                    s.put("optionPrice", sp[7]);
                }
                Map<String, Object> sResult = s.isEmpty() ? null : offRprsRepository.selectOffRprsOption(s);
                if (sResult != null) {
                    if (sResult.get("option_stock_quantity") == null) {
                        sResult.put("option_stock_quantity", 0);
                    }
                    int checkStockQuantity = toInt(sResult.get("option_stock_quantity"));
                    if (checkStockQuantity > 0 && checkStockQuantity < 10) {
                        removeIndex.add(i);
                    }
                    row.put("item_option_id", sResult.get("item_option_id"));
                    row.put("option_price", sResult.get("option_price"));
                    row.put("option_stock_flag", sResult.get("option_stock_flag"));
                    row.put("option_stock_quantity", sResult.get("option_stock_quantity"));
                    row.put("total_price", toInt(row.get("sale_price")) + toInt(sResult.get("option_price")));
                    row.put("changed_option", "N");
                } else {
                    row.put("changed_option", "Y");
                }
            } else {
                if (row.get("stock_quantity") == null) {
                    row.put("stock_quantity", 0);
                }
                int checkStockQuantity = toInt(row.get("stock_quantity"));
                if (checkStockQuantity > 0 && checkStockQuantity < 10) {
                    removeIndex.add(i);
                }
                row.put("changed_option", "N");
                row.put("total_price", toInt(row.get("sale_price")));
            }
        }
        for (int j = removeIndex.size() - 1; j >= 0; j--) {
            result.remove((int) removeIndex.get(j));
        }
        return result.isEmpty() ? null : result;
    }

    private static int toInt(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
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
                        @RequestParam(required = false) List<MultipartFile> images,
                        @RequestParam(required = false) String adultItemYn) {
        try {
            Gift gift = giftService.adminCreate(sellerId, itemName, itemSummary, detailContent, categoryCode,
                    locgovCode, salePrice, stockQuantity, displayType, displayStartDate, displayEndDate,
                    minDonationAmount, brandId, images);
            gift = giftService.applyAdultItemYn(gift, adultItemYn);
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
                        @RequestParam(required = false) Integer brandId,
                        @RequestParam(required = false) String adultItemYn) {
        try {
            Gift gift = giftService.adminEdit(id, sellerId, itemName, itemSummary, detailContent, categoryCode,
                    salePrice, stockQuantity, displayType, displayStartDate, displayEndDate, minDonationAmount, brandId);
            return giftService.applyAdultItemYn(gift, adultItemYn);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 답례품의 제철식품관 지정 (AS-IS는 답례품 저장 트랜잭션 안에서 같이 처리한다 -
     * `ItemServiceImpl:1471~1479`). MSA는 admin이 gift를 HTTP로 부르는 구조라
     * 저장 파라미터에 섞지 않고 하위 리소스로 분리했다 - 답례품 본문 수정 없이
     * 제철 월만 바꾸는 것도 운영에서 흔한 일이라 그 편이 쓰기 좋다.
     */
    @GetMapping("/{id}/season-food-months")
    public List<Integer> seasonFoodMonths(@PathVariable Long id) {
        return giftService.seasonFoodMonthsOf(id);
    }

    @PutMapping("/{id}/season-food-months")
    public List<Integer> updateSeasonFoodMonths(@PathVariable Long id,
                                                 @RequestParam(required = false) List<Integer> months,
                                                 @RequestParam(required = false) Long managerId) {
        try {
            giftService.replaceSeasonFoodMonths(id, months, managerId);
            return giftService.seasonFoodMonthsOf(id);
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
