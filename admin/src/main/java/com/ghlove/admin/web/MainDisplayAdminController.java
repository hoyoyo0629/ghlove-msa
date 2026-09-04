package com.ghlove.admin.web;

import com.ghlove.admin.service.MainDisplayAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 메인 진열상품 관리 (AS-IS opmanager/main-display - MainDisplayItemManagerController).
 * TEMPLATE_ID("{뷰타입}_{카테고리팀코드}" 조합, 예: md_best) 단위로 노출상품을 전체교체한다. */
@Controller
@RequestMapping("/admin/main-display")
@RequiredArgsConstructor
public class MainDisplayAdminController {

    private final MainDisplayAdminClient client;

    @GetMapping
    public String form(@RequestParam(required = false, defaultValue = "md") String templateId, Model model) {
        List<MainDisplayAdminClient.ItemRow> items = client.get(templateId);
        model.addAttribute("templateId", templateId);
        model.addAttribute("items", items);
        model.addAttribute("itemIdsCsv", items.stream().map(i -> String.valueOf(i.itemId())).reduce((a, b) -> a + ", " + b).orElse(""));
        return "main-display-admin/form";
    }

    @PostMapping
    public String save(@RequestParam String templateId, @RequestParam(required = false) String itemIds) {
        List<Long> ids = (itemIds == null || itemIds.isBlank())
                ? List.of()
                : java.util.Arrays.stream(itemIds.split("[,\\s]+"))
                        .filter(s -> !s.isBlank())
                        .map(Long::parseLong)
                        .toList();
        client.replace(templateId, ids);
        return "redirect:/admin/main-display?templateId=" + templateId;
    }
}
