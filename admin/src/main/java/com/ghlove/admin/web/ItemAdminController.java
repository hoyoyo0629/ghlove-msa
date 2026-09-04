package com.ghlove.admin.web;

import com.ghlove.admin.service.CategoryAdminClient;
import com.ghlove.admin.service.GiftClient;
import com.ghlove.admin.service.ItemAdminClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** 답례품 상품 관리자 직접 CRUD (AS-IS opmanager/item - ItemManagerController 1단계 대응).
 *  gift 서비스의 OP_ITEM을 관리자가 셀러를 대신 지정해 직접 생성/수정하고, 카테고리 일괄
 *  배정/상품 복사를 수행한다. 엑셀 대량등록/리뷰 관리자 대응은 이번 라운드 범위 밖. */
@Controller
@RequestMapping("/admin/gift-items")
@RequiredArgsConstructor
public class ItemAdminController {

    private final ItemAdminClient itemClient;
    private final GiftClient giftClient;
    private final CategoryAdminClient categoryClient;
    private final LocgovClient locgovClient;

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
                null, null, null, null, null, null, "ALWAYS", null, null, null, null, null));
        return "gift-items/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        populateFormModel(model);
        model.addAttribute("item", itemClient.get(id));
        return "gift-items/form";
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
                    displayType, displayStartDate, displayEndDate, minDonationAmount, null, brandId));
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
                    displayStartDate, displayEndDate, minDonationAmount, null, brandId));
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

    private void populateFormModel(Model model) {
        model.addAttribute("sellers", giftClient.sellers());
        model.addAttribute("categories", categoryClient.majors());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        model.addAttribute("brands", giftClient.brands());
    }
}
