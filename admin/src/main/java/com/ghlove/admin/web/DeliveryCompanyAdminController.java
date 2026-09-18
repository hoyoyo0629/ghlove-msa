package com.ghlove.admin.web;

import com.ghlove.admin.service.DeliveryReturnClient;
import com.ghlove.admin.service.DeliveryReturnClient.DeliveryCompanyForm;
import com.ghlove.admin.service.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 택배사관리 (AS-IS opmanager/delivery-company). order 서비스의 OP_DELIVERY_COMPANY를 cross-service CRUD. */
@Controller
@RequestMapping("/admin/delivery-companies")
@RequiredArgsConstructor
public class DeliveryCompanyAdminController {

    private final DeliveryReturnClient client;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("companies", client.deliveryCompanies());
        return "delivery-company-admin/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("company", null);
        return "delivery-company-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("company", client.deliveryCompany(id));
        return "delivery-company-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute DeliveryCompanyForm form, RedirectAttributes redirect) {
        try {
            client.createDeliveryCompany(form);
            redirect.addFlashAttribute("message", "택배사가 등록되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/delivery-companies";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute DeliveryCompanyForm form, RedirectAttributes redirect) {
        try {
            client.updateDeliveryCompany(id, form);
            redirect.addFlashAttribute("message", "택배사가 수정되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/delivery-companies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            client.deleteDeliveryCompany(id);
            redirect.addFlashAttribute("message", "택배사가 삭제되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/delivery-companies";
    }
}
