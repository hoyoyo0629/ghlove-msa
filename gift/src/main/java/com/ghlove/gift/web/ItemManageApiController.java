package com.ghlove.gift.web;

import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.ItemManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 답례품관리(AS-IS opmanager/item/list) cross-service API - 답례품 목록·일괄처리·삭제신청·변경사항 로그와
 * 대표 답례품 / 오프라인 대표 답례품. admin 콘솔이 부른다(X-Internal-Secret 보호 경로 /api/admin/**).
 * (기존 {@code /api/admin/items}는 옛 TO-BE 축소 화면(등록폼·승인·CSV)이 쓰는 채널이라 그대로 둔다.)
 */
@RestController
@RequestMapping("/api/admin/item-manage")
@RequiredArgsConstructor
public class ItemManageApiController {

    private final ItemManageService service;

    public record ListRequest(ItemManageService.Search search, int offset, int limit) {
    }

    public record IdsRequest(List<Long> ids, String flag, long managerId) {
    }

    public record ApprovalRequest(List<Long> ids, String message, long managerId) {
    }

    public record RprsRequest(String locgov, List<Long> itemIds, long userId) {
    }

    public record OffRprsRequest(String locgov, List<ItemManageService.OffRprsInput> items, long userId) {
    }

    /** 주문 상세가 품목 이미지를 그리려고 부른다 - 답례품ID별 대표 이미지 파일명(없으면 키가 빠진다). */
    @PostMapping("/thumbnails")
    public Map<Long, String> thumbnails(@RequestBody List<Long> itemIds) {
        return service.thumbnails(itemIds);
    }

    @PostMapping("/count")
    public Map<String, Integer> count(@RequestBody ItemManageService.Search search) {
        return Map.of("count", service.count(search));
    }

    @PostMapping("/list")
    public List<ItemManageService.ItemRow> list(@RequestBody ListRequest request) {
        return service.list(request.search(), request.offset(), request.limit());
    }

    @PostMapping("/update-display")
    public Map<String, Integer> updateDisplay(@RequestBody IdsRequest request) {
        try {
            return Map.of("updated", service.updateDisplay(request.ids(), request.flag(), request.managerId()));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/update-label")
    public Map<String, Integer> updateLabel(@RequestBody IdsRequest request) {
        try {
            return Map.of("updated", service.updateLabel(request.ids(), request.flag(), request.managerId()));
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Map<String, Integer> delete(@RequestBody IdsRequest request) {
        return Map.of("updated", service.delete(request.ids(), request.managerId()));
    }

    // ---- 답례품승인관리 ----

    /** AS-IS list/update(processType=approval) - 승인처리. 삭제신청(40)은 삭제 승인. */
    @PostMapping("/approval")
    public List<ItemManageService.ProcessedItem> approval(@RequestBody ApprovalRequest request) {
        try {
            return service.approve(request.ids(), request.managerId());
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** AS-IS list/update(processType=reject) - 등록보류. */
    @PostMapping("/reject")
    public List<ItemManageService.ProcessedItem> reject(@RequestBody ApprovalRequest request) {
        try {
            return service.reject(request.ids(), request.message(), request.managerId());
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** AS-IS item/seller/preview 데이터 - 화면에 보이는 상품코드로 찾는다. */
    @GetMapping("/preview")
    public ItemManageService.Preview preview(@RequestParam String itemUserCode) {
        try {
            return service.preview(itemUserCode);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{itemId}/log/count")
    public Map<String, Integer> logCount(@PathVariable long itemId) {
        return Map.of("count", service.logCount(itemId));
    }

    @GetMapping("/{itemId}/log")
    public List<ItemManageService.LogRow> logList(@PathVariable long itemId, @RequestParam(defaultValue = "0") int offset,
                                                  @RequestParam(defaultValue = "10") int limit) {
        return service.logList(itemId, offset, limit);
    }

    // ---- 대표 답례품 ----

    @GetMapping("/rprs")
    public List<ItemManageService.RprsRow> rprs(@RequestParam String locgov) {
        return service.rprsList(locgov);
    }

    @PostMapping("/rprs")
    public void saveRprs(@RequestBody RprsRequest request) {
        service.saveRprs(request.locgov(), request.itemIds(), request.userId());
    }

    @GetMapping("/off-rprs")
    public List<ItemManageService.OffRprsRow> offRprs(@RequestParam String locgov) {
        return service.offRprsList(locgov);
    }

    @PostMapping("/off-rprs")
    public void saveOffRprs(@RequestBody OffRprsRequest request) {
        service.saveOffRprs(request.locgov(), request.items(), request.userId());
    }

    @GetMapping("/option-list")
    public List<Map<String, Object>> optionList(@RequestParam long itemId) {
        return service.optionList(itemId);
    }

    @PostMapping("/combo-option-list")
    public List<Map<String, String>> comboOptionList(@RequestBody Map<String, Object> optionMap) {
        return service.comboOptionList(optionMap);
    }

    @GetMapping("/selected-option")
    public Map<String, String> selectedOption(@RequestParam long itemId) {
        String option = service.selectedOption(itemId);
        return option == null ? Map.of() : Map.of("option", option);
    }

    // ---- 카테고리 select 옵션 ----

    @GetMapping("/category-options")
    public List<ItemManageService.CategoryOption> categoryOptions(@RequestParam int groupId) {
        return service.categoriesByGroup(groupId);
    }

    @GetMapping("/category-children")
    public List<ItemManageService.CategoryOption> categoryChildren(@RequestParam String parent) {
        return service.categoryChildren(parent);
    }
}
