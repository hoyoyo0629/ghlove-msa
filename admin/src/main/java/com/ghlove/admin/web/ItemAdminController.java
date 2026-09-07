package com.ghlove.admin.web;

import com.ghlove.admin.service.CategoryAdminClient;
import com.ghlove.admin.service.GiftClient;
import com.ghlove.admin.service.ItemAdminClient;
import com.ghlove.admin.service.ItemOptionAdminClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** 답례품 상품 관리자 직접 CRUD (AS-IS opmanager/item - ItemManagerController 1~2단계 대응).
 *  gift 서비스의 OP_ITEM을 관리자가 셀러를 대신 지정해 직접 생성/수정하고, 카테고리 일괄
 *  배정/상품 복사(1단계)에 이어 엑셀 대량등록/다운로드, 노출·라벨·순서·판매정보 일괄변경,
 *  일괄삭제(2단계, admin-console-item-mgmt-round #2)를 수행한다. */
@Controller
@RequestMapping("/admin/gift-items")
@RequiredArgsConstructor
public class ItemAdminController {

    private final ItemAdminClient itemClient;
    private final GiftClient giftClient;
    private final CategoryAdminClient categoryClient;
    private final LocgovClient locgovClient;
    private final ItemOptionAdminClient itemOptionClient;

    @GetMapping
    public String list(@RequestParam(required = false) String itemName,
                        @RequestParam(required = false) String categoryCode,
                        @RequestParam(required = false) Long sellerId,
                        @RequestParam(required = false) String dataStatusCode,
                        Model model) {
        model.addAttribute("items", itemClient.search(itemName, categoryCode, sellerId, dataStatusCode));
        model.addAttribute("sellers", giftClient.sellers());
        model.addAttribute("categories", categoryClient.majors());
        model.addAttribute("itemName", itemName);
        model.addAttribute("categoryCode", categoryCode);
        model.addAttribute("sellerId", sellerId);
        model.addAttribute("dataStatusCode", dataStatusCode);
        return "gift-items/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        populateFormModel(model);
        model.addAttribute("item", new ItemAdminClient.ItemDto(null, null, null, null, null, null, null,
                null, null, null, null, null, null, "ALWAYS", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null));
        return "gift-items/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        populateFormModel(model);
        model.addAttribute("item", itemClient.get(id));
        model.addAttribute("options", itemOptionClient.list(id));
        return "gift-items/form";
    }

    /** 배송비/택배사 설정 (SFR-005 재검토 라운드 - 원래 코드가 전혀 없던 gap). */
    @PostMapping("/{id}/shipping")
    public String updateShipping(@PathVariable Long id, @RequestParam(required = false) String deliveryCompanyName,
                                  @RequestParam String shippingType, @RequestParam(required = false) Integer shipping,
                                  @RequestParam(required = false) Integer shippingFreeAmount,
                                  @RequestParam(required = false) Integer shippingExtraCharge1,
                                  @RequestParam(required = false) Integer shippingExtraCharge2,
                                  @RequestParam(required = false, defaultValue = "false") boolean itemReturnAllowed,
                                  RedirectAttributes redirect) {
        try {
            itemClient.updateShipping(id, deliveryCompanyName, shippingType, shipping, shippingFreeAmount,
                    shippingExtraCharge1, shippingExtraCharge2, itemReturnAllowed);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/" + id + "/edit";
    }

    /** 답례품 옵션 관리 (SFR-005 재검토 라운드 - 원래 코드가 전혀 없던 gap). */
    @PostMapping("/{id}/options")
    public String registerOption(@PathVariable Long id, @RequestParam String optionName,
                                  @RequestParam(required = false) Integer optionPrice,
                                  @RequestParam(required = false, defaultValue = "false") boolean stockTracked,
                                  @RequestParam(required = false) Integer stockQuantity,
                                  RedirectAttributes redirect) {
        try {
            itemOptionClient.register(id, optionName, optionPrice, stockTracked, stockQuantity);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/" + id + "/edit";
    }

    @PostMapping("/{id}/options/{optionId}")
    public String editOption(@PathVariable Long id, @PathVariable Long optionId, @RequestParam String optionName,
                              @RequestParam(required = false) Integer optionPrice,
                              @RequestParam(required = false, defaultValue = "false") boolean stockTracked,
                              @RequestParam(required = false) Integer stockQuantity,
                              RedirectAttributes redirect) {
        try {
            itemOptionClient.edit(id, optionId, optionName, optionPrice, stockTracked, stockQuantity);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/" + id + "/edit";
    }

    @PostMapping("/{id}/options/{optionId}/delete")
    public String deleteOption(@PathVariable Long id, @PathVariable Long optionId, RedirectAttributes redirect) {
        try {
            itemOptionClient.delete(id, optionId);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/" + id + "/edit";
    }

    @PostMapping("/{id}/options/{optionId}/display")
    public String displayOption(@PathVariable Long id, @PathVariable Long optionId, @RequestParam boolean display,
                                 RedirectAttributes redirect) {
        try {
            itemOptionClient.setDisplay(id, optionId, display);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/" + id + "/edit";
    }

    @PostMapping
    public String create(@RequestParam Long sellerId, @RequestParam String itemName,
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
                          Model model) {
        try {
            itemClient.create(sellerId, itemName, itemSummary, detailContent, categoryCode, locgovCode,
                    salePrice, stockQuantity, displayType, displayStartDate, displayEndDate,
                    minDonationAmount, brandId, images);
            return "redirect:/admin/gift-items";
        } catch (ManagerException e) {
            populateFormModel(model);
            model.addAttribute("item", new ItemAdminClient.ItemDto(null, sellerId, itemName, itemSummary, detailContent,
                    categoryCode, locgovCode, salePrice, stockQuantity, null, null, null, null,
                    displayType, displayStartDate, displayEndDate, minDonationAmount, null, brandId, null, null,
                    null, null, null, null, null, null, null));
            model.addAttribute("errorMessage", e.getMessage());
            return "gift-items/form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam Long sellerId, @RequestParam String itemName,
                          @RequestParam(required = false) String itemSummary,
                          @RequestParam(required = false) String detailContent,
                          @RequestParam String categoryCode, @RequestParam Integer salePrice,
                          @RequestParam Integer stockQuantity,
                          @RequestParam(required = false) String displayType,
                          @RequestParam(required = false) String displayStartDate,
                          @RequestParam(required = false) String displayEndDate,
                          @RequestParam(required = false) Integer minDonationAmount,
                          @RequestParam(required = false) Integer brandId,
                          Model model) {
        try {
            itemClient.update(id, sellerId, itemName, itemSummary, detailContent, categoryCode, salePrice,
                    stockQuantity, displayType, displayStartDate, displayEndDate, minDonationAmount, brandId);
            return "redirect:/admin/gift-items";
        } catch (ManagerException e) {
            populateFormModel(model);
            model.addAttribute("item", new ItemAdminClient.ItemDto(id, sellerId, itemName, itemSummary, detailContent,
                    categoryCode, null, salePrice, stockQuantity, null, null, null, null, displayType,
                    displayStartDate, displayEndDate, minDonationAmount, null, brandId, null, null,
                    null, null, null, null, null, null, null));
            model.addAttribute("options", itemOptionClient.list(id));
            model.addAttribute("errorMessage", e.getMessage());
            return "gift-items/form";
        }
    }

    @PostMapping("/{id}/copy")
    public String copy(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            itemClient.copy(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items";
    }

    /** 지자체 승인/반려 (SFR-005 재검토 라운드 - 원래 gift 자체 무인증 임시화면이었던 것을 이관). */
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            itemClient.approve(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items?dataStatusCode=PENDING";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            itemClient.reject(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items?dataStatusCode=PENDING";
    }

    /** 대표상품관리 (AS-IS opmanager/item/representative-item - 원래 gift 자체 무인증
     *  임시화면이었던 것을 이관). */
    @GetMapping("/representative")
    public String representative(Model model) {
        model.addAttribute("gifts", itemClient.representativeItems());
        model.addAttribute("candidates", itemClient.search(null, null, null, "APPROVED"));
        return "gift-items/representative";
    }

    @PostMapping("/{id}/representative/register")
    public String registerRepresentative(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            itemClient.registerRepresentative(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/representative";
    }

    @PostMapping("/{id}/representative/delete")
    public String unregisterRepresentative(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            itemClient.unregisterRepresentative(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/representative";
    }

    @PostMapping("/bulk-category")
    public String bulkCategory(@RequestParam(required = false) List<Long> itemIds,
                                @RequestParam String categoryCode, RedirectAttributes redirect) {
        if (itemIds == null || itemIds.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "카테고리를 배정할 답례품을 선택해 주세요.");
            return "redirect:/admin/gift-items";
        }
        try {
            itemClient.bulkAssignCategory(itemIds, categoryCode);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items";
    }

    // ==================== 2단계: 엑셀 대량처리/일괄작업 ====================

    @GetMapping("/excel")
    public String excelScreen(Model model) {
        return "gift-items/excel";
    }

    @PostMapping("/excel/upload")
    public String excelUpload(@RequestParam MultipartFile file, RedirectAttributes redirect) {
        try {
            ItemAdminClient.BulkUploadResult result = itemClient.bulkUpload(file);
            redirect.addFlashAttribute("uploadCreated", result.created());
            redirect.addFlashAttribute("uploadErrors", result.errors());
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items/excel";
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) String itemName,
                        @RequestParam(required = false) String categoryCode,
                        @RequestParam(required = false) Long sellerId,
                        @RequestParam(required = false) String dataStatusCode,
                        HttpServletResponse response) throws IOException {
        byte[] csv = itemClient.export(itemName, categoryCode, sellerId, dataStatusCode);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"gift-items.csv\"; filename*=UTF-8''" + URLEncoder.encode("답례품목록.csv", StandardCharsets.UTF_8));
        response.getOutputStream().write(csv != null ? csv : new byte[0]);
    }

    @PostMapping("/bulk-display")
    public String bulkDisplay(@RequestParam(required = false) List<Long> itemIds,
                               @RequestParam String displayFlag, RedirectAttributes redirect) {
        if (itemIds == null || itemIds.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "노출 상태를 변경할 답례품을 선택해 주세요.");
            return "redirect:/admin/gift-items";
        }
        try {
            itemClient.bulkDisplay(itemIds, displayFlag);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items";
    }

    @PostMapping("/bulk-label")
    public String bulkLabel(@RequestParam(required = false) List<Long> itemIds,
                             @RequestParam String itemLabel, RedirectAttributes redirect) {
        if (itemIds == null || itemIds.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "라벨을 변경할 답례품을 선택해 주세요.");
            return "redirect:/admin/gift-items";
        }
        try {
            itemClient.bulkLabel(itemIds, itemLabel);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Long> itemIds, RedirectAttributes redirect) {
        if (itemIds == null || itemIds.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "삭제할 답례품을 선택해 주세요.");
            return "redirect:/admin/gift-items";
        }
        try {
            itemClient.bulkDelete(itemIds);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items";
    }

    @PostMapping("/bulk-ordering")
    public String bulkOrdering(@RequestParam(required = false) List<Long> itemIds,
                                @RequestParam(required = false) List<Integer> orderings, RedirectAttributes redirect) {
        if (itemIds != null && orderings != null && !itemIds.isEmpty()) {
            try {
                itemClient.bulkOrdering(itemIds, orderings);
            } catch (ManagerException e) {
                redirect.addFlashAttribute("errorMessage", e.getMessage());
            }
        }
        return "redirect:/admin/gift-items";
    }

    @GetMapping("/bulk-sales")
    public String bulkSalesForm(@RequestParam(required = false) List<Long> itemIds, Model model,
                                 RedirectAttributes redirect) {
        if (itemIds == null || itemIds.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "판매정보를 수정할 답례품을 선택해 주세요.");
            return "redirect:/admin/gift-items";
        }
        List<ItemAdminClient.ItemDto> items = new ArrayList<>();
        for (Long id : itemIds) {
            try {
                items.add(itemClient.get(id));
            } catch (ManagerException ignored) {
                // 조회 실패한 항목은 조용히 건너뜀 - 나머지 선택 항목은 계속 수정 가능해야 함
            }
        }
        model.addAttribute("items", items);
        return "gift-items/bulk-sales";
    }

    @PostMapping("/bulk-sales")
    public String bulkSalesApply(@RequestParam List<Long> itemIds,
                                  @RequestParam List<Integer> salePrices,
                                  @RequestParam List<Integer> stockQuantities,
                                  RedirectAttributes redirect) {
        List<ItemAdminClient.SalesUpdate> updates = new ArrayList<>();
        for (int i = 0; i < itemIds.size(); i++) {
            updates.add(new ItemAdminClient.SalesUpdate(itemIds.get(i), salePrices.get(i), stockQuantities.get(i)));
        }
        try {
            itemClient.bulkSales(updates);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-items";
    }

    private void populateFormModel(Model model) {
        model.addAttribute("sellers", giftClient.sellers());
        model.addAttribute("categories", categoryClient.majors());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        model.addAttribute("brands", giftClient.brands());
    }
}
