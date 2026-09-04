package com.ghlove.admin.web;

import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.RankingAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 답례품 판매 랭킹 관리 (AS-IS opmanager/ranking - RankingManagerController, B9). 카테고리
 *  (그룹)코드를 고르면(또는 새로 입력하면) 그 그룹의 큐레이션 순서 목록을 보여주고, 상품을
 *  검색해서 추가/순서변경/삭제할 수 있다. 공개 페이지는 gift 서비스의
 *  GET /api/ranking/{categoryGroupCode}가 별도로 제공한다(storefront 연동용). */
@Controller
@RequestMapping("/admin/ranking")
@RequiredArgsConstructor
public class RankingAdminController {

    private final RankingAdminClient client;

    @GetMapping
    public String list(@RequestParam(required = false) String categoryUrl,
                        @RequestParam(required = false) String keyword,
                        Model model) {
        model.addAttribute("groups", client.groups());
        model.addAttribute("categoryUrl", categoryUrl);
        model.addAttribute("keyword", keyword);
        model.addAttribute("rows", client.list(categoryUrl));
        model.addAttribute("searchResults", client.itemSearch(keyword));
        return "ranking/list";
    }

    @PostMapping("/add")
    public String add(@RequestParam String categoryUrl, @RequestParam Long itemId,
                       RedirectAttributes redirect) {
        try {
            client.add(categoryUrl, itemId);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ranking?categoryUrl=" + categoryUrl;
    }

    @PostMapping("/{rankingId}/delete")
    public String delete(@PathVariable Integer rankingId, @RequestParam String categoryUrl) {
        client.delete(rankingId);
        return "redirect:/admin/ranking?categoryUrl=" + categoryUrl;
    }

    @PostMapping("/{rankingId}/move")
    public String move(@PathVariable Integer rankingId, @RequestParam String direction,
                        @RequestParam String categoryUrl) {
        client.move(rankingId, direction);
        return "redirect:/admin/ranking?categoryUrl=" + categoryUrl;
    }
}
