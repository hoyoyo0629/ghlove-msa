package com.ghlove.admin.web;

import com.ghlove.admin.service.CategoryAdminClient;
import com.ghlove.admin.service.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 답례품 카테고리 관리 (AS-IS opmanager/categories - CategoriesManagerController). gift 서비스의
 *  GIFT_CATEGORY(대분류)/GIFT_SUBCATEGORY(중분류)/GIFT_SUBCATEGORY_ITEM(개별품목) 3단계 트리를
 *  cross-service로 CRUD한다 - 답례품몰 GNB "전체 카테고리" 메가메뉴가 실제로 읽는 구조라
 *  여기서 바꾸면 스토어프론트에 즉시 반영된다(초기 시드 이후 처음 생기는 편집 경로). */
@Controller
@RequestMapping("/admin/gift-categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryAdminClient client;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("majors", client.tree());
        return "gift-categories/list";
    }

    // ---------------------------------------------------------------- 대분류

    @GetMapping("/majors/new")
    public String newMajorForm(Model model) {
        model.addAttribute("major", new CategoryAdminClient.MajorDto(null, null, null, "Y", java.util.List.of()));
        return "gift-categories/major-form";
    }

    @GetMapping("/majors/{id}/edit")
    public String editMajorForm(@PathVariable String id, Model model) {
        model.addAttribute("major", client.major(id));
        return "gift-categories/major-form";
    }

    @PostMapping("/majors")
    public String createMajor(@RequestParam String id, @RequestParam String label,
                               @RequestParam(required = false) Integer ordering, Model model) {
        if (client.majorExists(id)) {
            model.addAttribute("major", new CategoryAdminClient.MajorDto(id, label, ordering, "Y", java.util.List.of()));
            model.addAttribute("errorMessage", "이미 사용 중인 카테고리코드입니다: " + id);
            return "gift-categories/major-form";
        }
        client.createMajor(new CategoryAdminClient.MajorForm(id, label, ordering, "Y"));
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/majors/{id}")
    public String updateMajor(@PathVariable String id, @RequestParam String label,
                               @RequestParam(required = false) Integer ordering,
                               @RequestParam(required = false) String useYn) {
        client.updateMajor(id, new CategoryAdminClient.MajorForm(id, label, ordering, useYn != null ? "Y" : "N"));
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/majors/{id}/delete")
    public String deleteMajor(@PathVariable String id, RedirectAttributes redirect) {
        try {
            client.deleteMajor(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/majors/{id}/move")
    public String moveMajor(@PathVariable String id, @RequestParam String direction) {
        client.moveMajor(id, direction);
        return "redirect:/admin/gift-categories";
    }

    // ---------------------------------------------------------------- 중분류

    @GetMapping("/subcategories/new")
    public String newSubcategoryForm(@RequestParam(required = false) String categoryCode, Model model) {
        model.addAttribute("subcategory", new CategoryAdminClient.SubcategoryDto(null, categoryCode, null, null,
                null, null, null, java.util.List.of()));
        model.addAttribute("majors", client.majors());
        return "gift-categories/subcategory-form";
    }

    @GetMapping("/subcategories/{id}/edit")
    public String editSubcategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("subcategory", client.subcategory(id));
        model.addAttribute("majors", client.majors());
        return "gift-categories/subcategory-form";
    }

    @PostMapping("/subcategories")
    public String createSubcategory(@RequestParam String categoryCode, @RequestParam String name,
                                     @RequestParam(required = false) String metaTitle,
                                     @RequestParam(required = false) String metaKeywords,
                                     @RequestParam(required = false) String metaDescription) {
        client.createSubcategory(new CategoryAdminClient.SubcategoryForm(categoryCode, name, metaTitle, metaKeywords, metaDescription));
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/subcategories/{id}")
    public String updateSubcategory(@PathVariable Long id, @RequestParam String categoryCode, @RequestParam String name,
                                     @RequestParam(required = false) String metaTitle,
                                     @RequestParam(required = false) String metaKeywords,
                                     @RequestParam(required = false) String metaDescription) {
        client.updateSubcategory(id, new CategoryAdminClient.SubcategoryForm(categoryCode, name, metaTitle, metaKeywords, metaDescription));
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/subcategories/{id}/delete")
    public String deleteSubcategory(@PathVariable Long id) {
        client.deleteSubcategory(id);
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/subcategories/{id}/move")
    public String moveSubcategory(@PathVariable Long id, @RequestParam String direction) {
        client.moveSubcategory(id, direction);
        return "redirect:/admin/gift-categories";
    }

    // ---------------------------------------------------------------- 개별품목

    @PostMapping("/items")
    public String createItem(@RequestParam Long subcategoryId, @RequestParam String name) {
        client.createItem(new CategoryAdminClient.ItemForm(subcategoryId, name));
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/items/{id}")
    public String updateItem(@PathVariable Long id, @RequestParam Long subcategoryId, @RequestParam String name) {
        client.updateItem(id, new CategoryAdminClient.ItemForm(subcategoryId, name));
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id) {
        client.deleteItem(id);
        return "redirect:/admin/gift-categories";
    }

    @PostMapping("/items/{id}/move")
    public String moveItem(@PathVariable Long id, @RequestParam String direction) {
        client.moveItem(id, direction);
        return "redirect:/admin/gift-categories";
    }
}
