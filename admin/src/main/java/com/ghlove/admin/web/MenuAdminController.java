package com.ghlove.admin.web;

import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 메뉴관리 (AS-IS opmanager/menu - MenuManagerController). OP_MENU/OP_MENU_RIGHT를
 * 화면에서 직접 CRUD한다 - 지금까지 이 프로젝트의 모든 라운드가 새 화면을 추가할 때마다
 * SQL INSERT로 해오던 작업을 대체. */
@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class MenuAdminController {

    private final MenuAdminService menuAdminService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("groups", menuAdminService.allGrouped());
        return "menu-admin/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Integer parentId, Model model) {
        model.addAttribute("menu", null);
        model.addAttribute("selectedParentId", parentId);
        model.addAttribute("topLevelMenus", menuAdminService.topLevelMenus());
        model.addAttribute("rights", java.util.List.of());
        return "menu-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("menu", menuAdminService.findOrThrow(id));
        model.addAttribute("topLevelMenus", menuAdminService.topLevelMenus());
        model.addAttribute("rights", menuAdminService.rightsOf(id));
        return "menu-admin/form";
    }

    @PostMapping
    public String create(@RequestParam(required = false) Integer parentId,
                          @RequestParam String menuName,
                          @RequestParam(required = false) String menuUrl,
                          @RequestParam(required = false) boolean grantLocgovMain,
                          @RequestParam(required = false) boolean grantLocgovSub,
                          Model model) {
        try {
            menuAdminService.create(parentId, menuName, menuUrl, grantLocgovMain, grantLocgovSub);
            return "redirect:/admin/menus";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("topLevelMenus", menuAdminService.topLevelMenus());
            model.addAttribute("rights", java.util.List.of());
            return "menu-admin/form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                          @RequestParam String menuName,
                          @RequestParam(required = false) String menuUrl,
                          @RequestParam(required = false) Integer menuSeq,
                          @RequestParam(required = false) boolean grantLocgovMain,
                          @RequestParam(required = false) boolean grantLocgovSub,
                          Model model) {
        try {
            menuAdminService.update(id, menuName, menuUrl, menuSeq, grantLocgovMain, grantLocgovSub);
            return "redirect:/admin/menus";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("menu", menuAdminService.findOrThrow(id));
            model.addAttribute("topLevelMenus", menuAdminService.topLevelMenus());
            model.addAttribute("rights", menuAdminService.rightsOf(id));
            return "menu-admin/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            menuAdminService.delete(id);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/menus";
    }
}
