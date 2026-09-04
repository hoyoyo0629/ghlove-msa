package com.ghlove.admin.web;

import com.ghlove.admin.service.StyleBookAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 스타일북(답례품 큐레이션) 관리 (AS-IS opmanager/style-book - StyleBookManagerController). */
@Controller
@RequestMapping("/admin/style-books")
@RequiredArgsConstructor
public class StyleBookAdminController {

    private final StyleBookAdminClient client;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("styleBooks", client.list());
        return "style-book-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("styleBook", null);
        return "style-book-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("styleBook", client.get(id));
        model.addAttribute("items", client.items(id));
        return "style-book-admin/form";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam(required = false) String content,
                          @RequestParam(required = false) String image, @RequestParam(required = false) Integer ordering) {
        client.create(title, content, image, ordering);
        return "redirect:/admin/style-books";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String title, @RequestParam(required = false) String content,
                          @RequestParam(required = false) String image, @RequestParam(required = false) Integer ordering) {
        client.update(id, title, content, image, ordering);
        return "redirect:/admin/style-books/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        client.delete(id);
        return "redirect:/admin/style-books";
    }

    @PostMapping("/{id}/items")
    public String addItem(@PathVariable Long id, @RequestParam Long itemId) {
        client.addItem(id, itemId);
        return "redirect:/admin/style-books/" + id + "/edit";
    }

    @PostMapping("/{id}/items/{itemId}/delete")
    public String removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        client.removeItem(id, itemId);
        return "redirect:/admin/style-books/" + id + "/edit";
    }
}
