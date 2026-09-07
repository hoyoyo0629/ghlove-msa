package com.ghlove.admin.web;

import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MobileCategoryEditAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 모바일 카테고리 편집 관리 (AS-IS opmanager/mobile-category-edit -
 *  MobileCategoriesEditManagerController). gift 서비스의 OP_MOBILE_CATEGORY_EDIT을
 *  cross-service로 CRUD한다 - 모바일 메인/카테고리 화면의 코드(화면)별 위치에 HTML블록
 *  또는 배너이미지+링크를 배치한다. */
@Controller
@RequestMapping("/admin/mobile-category-edit")
@RequiredArgsConstructor
public class MobileCategoryEditAdminController {

    private final MobileCategoryEditAdminClient client;

    @GetMapping
    public String list(@RequestParam(required = false) String code, Model model) {
        model.addAttribute("rows", client.list(code));
        model.addAttribute("code", code);
        model.addAttribute("giftBaseUrl", client.giftServiceBaseUrl());
        return "mobile-category-edit-admin/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) String code, Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("row", new MobileCategoryEditAdminClient.EditRow(null, code, "1", null, null, null, null, null, null));
        return "mobile-category-edit-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@org.springframework.web.bind.annotation.PathVariable Integer id, Model model) {
        model.addAttribute("mode", "edit");
        model.addAttribute("row", client.get(id));
        model.addAttribute("giftBaseUrl", client.giftServiceBaseUrl());
        return "mobile-category-edit-admin/form";
    }

    @PostMapping
    public String create(@RequestParam String code, @RequestParam String editKind, @RequestParam String editPosition,
                          @RequestParam(required = false) String editContent, @RequestParam(required = false) String editUrl,
                          @RequestParam(required = false) MultipartFile editImage, RedirectAttributes redirect) {
        try {
            client.create(code, editKind, editPosition, editContent, editUrl, editImage);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/mobile-category-edit?code=" + code;
    }

    @PostMapping("/{id}")
    public String update(@org.springframework.web.bind.annotation.PathVariable Integer id,
                          @RequestParam String code, @RequestParam String editKind, @RequestParam String editPosition,
                          @RequestParam(required = false) String editContent, @RequestParam(required = false) String editUrl,
                          @RequestParam(required = false) MultipartFile editImage, RedirectAttributes redirect) {
        try {
            client.update(id, code, editKind, editPosition, editContent, editUrl, editImage);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/mobile-category-edit?code=" + code;
    }

    @PostMapping("/{id}/delete")
    public String delete(@org.springframework.web.bind.annotation.PathVariable Integer id,
                          @RequestParam(required = false) String code, RedirectAttributes redirect) {
        try {
            client.delete(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/mobile-category-edit?code=" + code;
    }
}
