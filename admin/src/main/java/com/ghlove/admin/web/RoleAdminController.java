package com.ghlove.admin.web;

import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.RoleAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * D10 역할·메뉴권한 관리 (AS-IS opmanager/user-group - UserGroupController, 이름과 달리
 * 회원그룹이 아니라 관리자 권한관리다). 역할(OP_ROLE) CRUD + 역할별 메뉴트리×권한
 * 체크박스 매트릭스(OP_MENU_RIGHT) 편집.
 */
@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleAdminService roleAdminService;

    @GetMapping
    public String list(@RequestParam(required = false) String errorMessage, Model model) {
        model.addAttribute("roles", roleAdminService.list());
        model.addAttribute("errorMessage", errorMessage);
        return "role-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("role", null);
        return "role-admin/form";
    }

    @PostMapping
    public String create(@RequestParam String authority, @RequestParam String roleName,
                          @RequestParam(required = false) String roleDesc,
                          @RequestParam(required = false) Integer roleSeq, Model model) {
        try {
            roleAdminService.create(authority, roleName, roleDesc, roleSeq);
            return "redirect:/admin/roles";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "role-admin/form";
        }
    }

    @GetMapping("/{authority}/edit")
    public String editForm(@PathVariable String authority, Model model) {
        model.addAttribute("role", roleAdminService.get(authority));
        return "role-admin/form";
    }

    @PostMapping("/{authority}")
    public String update(@PathVariable String authority, @RequestParam String roleName,
                          @RequestParam(required = false) String roleDesc,
                          @RequestParam(required = false) Integer roleSeq, Model model) {
        try {
            roleAdminService.update(authority, roleName, roleDesc, roleSeq);
            return "redirect:/admin/roles";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("role", roleAdminService.get(authority));
            return "role-admin/form";
        }
    }

    @PostMapping("/{authority}/delete")
    public String delete(@PathVariable String authority, RedirectAttributes redirectAttributes) {
        try {
            roleAdminService.delete(authority);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/{authority}/matrix")
    public String matrix(@PathVariable String authority, Model model) {
        model.addAttribute("role", roleAdminService.get(authority));
        model.addAttribute("matrix", roleAdminService.matrixFor(authority));
        return "role-admin/matrix";
    }

    @PostMapping("/{authority}/matrix")
    public String saveMatrix(@PathVariable String authority,
                              @RequestParam(required = false) List<Integer> menuIds) {
        roleAdminService.saveMatrix(authority, menuIds);
        return "redirect:/admin/roles/" + authority + "/matrix";
    }
}
