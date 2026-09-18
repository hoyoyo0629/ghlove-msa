package com.ghlove.admin.web;

import com.ghlove.admin.service.DeliveryReturnClient;
import com.ghlove.admin.service.DeliveryReturnClient.ShipmentReturnForm;
import com.ghlove.admin.service.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 반품지관리 (AS-IS opmanager/shipment-return). order 서비스의 OP_SHIPMENT_RETURN을 판매자별 CRUD. */
@Controller
@RequestMapping("/admin/shipment-returns")
@RequiredArgsConstructor
public class ShipmentReturnAdminController {

    private final DeliveryReturnClient client;

    @GetMapping
    public String list(@RequestParam(required = false) Long sellerId, Model model) {
        model.addAttribute("returns", client.shipmentReturns(sellerId));
        model.addAttribute("sellerId", sellerId);
        return "shipment-return-admin/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long sellerId, Model model) {
        model.addAttribute("shipmentReturn", null);
        model.addAttribute("sellerId", sellerId);
        return "shipment-return-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("shipmentReturn", client.shipmentReturn(id));
        return "shipment-return-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute ShipmentReturnForm form, RedirectAttributes redirect) {
        try {
            client.createShipmentReturn(form);
            redirect.addFlashAttribute("message", "반품지가 등록되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipment-returns" + (form.sellerId() != null ? "?sellerId=" + form.sellerId() : "");
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute ShipmentReturnForm form, RedirectAttributes redirect) {
        try {
            client.updateShipmentReturn(id, form);
            redirect.addFlashAttribute("message", "반품지가 수정되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipment-returns" + (form.sellerId() != null ? "?sellerId=" + form.sellerId() : "");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, @RequestParam(required = false) Long sellerId, RedirectAttributes redirect) {
        try {
            client.deleteShipmentReturn(id);
            redirect.addFlashAttribute("message", "반품지가 삭제되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipment-returns" + (sellerId != null ? "?sellerId=" + sellerId : "");
    }
}
