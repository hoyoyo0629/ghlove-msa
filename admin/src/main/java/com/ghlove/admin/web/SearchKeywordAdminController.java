package com.ghlove.admin.web;

import com.ghlove.admin.domain.SearchKeyword;
import com.ghlove.admin.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 추천 검색어 관리 (AS-IS opmanager/search - SearchManagerController). */
@Controller
@RequestMapping("/admin/search-keywords")
@RequiredArgsConstructor
public class SearchKeywordAdminController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SearchKeywordRepository searchKeywordRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("keywords", searchKeywordRepository.findAllByOrderBySearchIdDesc());
        return "search-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("keyword", null);
        return "search-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("keyword", searchKeywordRepository.findById(id).orElseThrow());
        return "search-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute SearchKeyword form) {
        form.setSearchId(null);
        form.setSearchLinkTargetFlag(toYn(form.getSearchLinkTargetFlag()));
        form.setSearchMobileLinkTargetFlag(toYn(form.getSearchMobileLinkTargetFlag()));
        form.setCreatedDate(LocalDateTime.now().format(FMT));
        searchKeywordRepository.save(form);
        return "redirect:/admin/search-keywords";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute SearchKeyword form) {
        SearchKeyword keyword = searchKeywordRepository.findById(id).orElseThrow();
        keyword.setSearchContents(form.getSearchContents());
        keyword.setSearchLink(form.getSearchLink());
        keyword.setSearchMobileLink(form.getSearchMobileLink());
        keyword.setSearchLinkTargetFlag(toYn(form.getSearchLinkTargetFlag()));
        keyword.setSearchMobileLinkTargetFlag(toYn(form.getSearchMobileLinkTargetFlag()));
        keyword.setSearchStartDate(form.getSearchStartDate());
        keyword.setSearchEndDate(form.getSearchEndDate());
        searchKeywordRepository.save(keyword);
        return "redirect:/admin/search-keywords";
    }

    @PostMapping("/delete")
    public String deleteSelected(@RequestParam(required = false) List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            searchKeywordRepository.deleteAllById(ids);
        }
        return "redirect:/admin/search-keywords";
    }

    private static String toYn(String raw) {
        return "Y".equals(raw) || "on".equals(raw) ? "Y" : "N";
    }
}
