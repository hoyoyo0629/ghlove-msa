package com.ghlove.admin.web;

import com.ghlove.admin.service.CategoryTeamAdminClient;
import com.ghlove.admin.service.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * 카테고리 "팀"(대분류 상위그룹)+"그룹"(팀 하위, 프로모션묶음) 관리 (AS-IS
 * saleson.shop.categoriesteamgroup - 답례품 상품관리 2단계 #5). gift DB에 이미 이관돼 있던
 * OP_CATEGORY_TEAM/OP_CATEGORY_GROUP/OP_CATEGORY_GROUP_BANNER를 처음으로 CRUD 대상으로
 * 활성화한다. 기존 카테고리관리(/admin/gift-categories, GNB 트리)와는 별개의 프로모션
 * 기획전 성격 트리라 별도 화면으로 둔다.
 */
@Controller
@RequestMapping("/admin/category-teams")
@RequiredArgsConstructor
public class CategoryTeamAdminController {

    private final CategoryTeamAdminClient client;

    @GetMapping
    public String list(Model model) {
        List<CategoryTeamAdminClient.TeamDto> teams = client.list();
        java.util.Map<Integer, List<CategoryTeamAdminClient.TeamItemDto>> teamItems = new java.util.LinkedHashMap<>();
        teams.forEach(t -> teamItems.put(t.id(), client.teamItems(t.id())));
        model.addAttribute("teams", teams);
        model.addAttribute("teamItems", teamItems);
        return "category-teams/list";
    }

    // ---------------------------------------------------------------- 팀

    @GetMapping("/new")
    public String newTeamForm(Model model) {
        model.addAttribute("team", new CategoryTeamAdminClient.TeamDto(null, null, null, "Y", null, List.of()));
        return "category-teams/team-form";
    }

    @GetMapping("/{id}/edit")
    public String editTeamForm(@PathVariable Integer id, Model model) {
        model.addAttribute("team", client.team(id));
        return "category-teams/team-form";
    }

    @PostMapping
    public String createTeam(@RequestParam String name, @RequestParam String code,
                              @RequestParam(required = false) Integer ordering, Model model,
                              RedirectAttributes redirect) {
        try {
            client.createTeam(new CategoryTeamAdminClient.TeamForm(name, code, ordering, "Y"));
            return "redirect:/admin/category-teams";
        } catch (ManagerException e) {
            model.addAttribute("team", new CategoryTeamAdminClient.TeamDto(null, name, code, "Y", ordering, List.of()));
            model.addAttribute("errorMessage", e.getMessage());
            return "category-teams/team-form";
        }
    }

    @PostMapping("/{id}")
    public String updateTeam(@PathVariable Integer id, @RequestParam String name,
                              @RequestParam(required = false) Integer ordering,
                              @RequestParam(required = false) String flag) {
        client.updateTeam(id, new CategoryTeamAdminClient.TeamForm(name, null, ordering, flag != null ? "Y" : "N"));
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/{id}/delete")
    public String deleteTeam(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            client.deleteTeam(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/{id}/move")
    public String moveTeam(@PathVariable Integer id, @RequestParam String direction) {
        client.moveTeam(id, direction);
        return "redirect:/admin/category-teams";
    }

    // ---------------------------------------------------------------- 팀에 답례품 배정

    @PostMapping("/{id}/items")
    public String addItems(@PathVariable Integer id, @RequestParam String itemIds, RedirectAttributes redirect) {
        try {
            List<Long> ids = Arrays.stream(itemIds.split("[,\\s]+"))
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .toList();
            if (ids.isEmpty()) {
                redirect.addFlashAttribute("errorMessage", "배정할 답례품ID를 입력해 주세요.");
            } else {
                client.addTeamItems(id, ids);
            }
        } catch (NumberFormatException e) {
            redirect.addFlashAttribute("errorMessage", "답례품ID는 숫자로, 콤마 또는 공백으로 구분해 입력해 주세요.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/{id}/items/{itemId}/delete")
    public String removeItem(@PathVariable Integer id, @PathVariable Long itemId) {
        client.removeTeamItem(id, itemId);
        return "redirect:/admin/category-teams";
    }

    // ---------------------------------------------------------------- 그룹

    @GetMapping("/groups/new")
    public String newGroupForm(@RequestParam Integer teamId, Model model) {
        model.addAttribute("group", new CategoryTeamAdminClient.GroupDto(null, teamId, null, null, "Y", null, List.of()));
        return "category-teams/group-form";
    }

    @GetMapping("/groups/{id}/edit")
    public String editGroupForm(@PathVariable Integer id, Model model) {
        model.addAttribute("group", client.group(id));
        return "category-teams/group-form";
    }

    @PostMapping("/groups")
    public String createGroup(@RequestParam Integer categoryTeamId, @RequestParam String name,
                               @RequestParam(required = false) String code, Model model) {
        try {
            client.createGroup(new CategoryTeamAdminClient.GroupForm(categoryTeamId, name, code, null, "Y"));
        } catch (ManagerException e) {
            model.addAttribute("group", new CategoryTeamAdminClient.GroupDto(null, categoryTeamId, name, code, "Y", null, List.of()));
            model.addAttribute("errorMessage", e.getMessage());
            return "category-teams/group-form";
        }
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/groups/{id}")
    public String updateGroup(@PathVariable Integer id, @RequestParam Integer categoryTeamId,
                               @RequestParam String name, @RequestParam(required = false) String code) {
        client.updateGroup(id, new CategoryTeamAdminClient.GroupForm(categoryTeamId, name, code, null, "Y"));
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/groups/{id}/delete")
    public String deleteGroup(@PathVariable Integer id) {
        client.deleteGroup(id);
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/groups/{id}/move")
    public String moveGroup(@PathVariable Integer id, @RequestParam String direction) {
        client.moveGroup(id, direction);
        return "redirect:/admin/category-teams";
    }

    // ---------------------------------------------------------------- 그룹 배너

    @PostMapping("/groups/{id}/banners")
    public String addBanner(@PathVariable Integer id, @RequestParam String title,
                             @RequestParam(required = false) String linkUrl,
                             @RequestParam MultipartFile image, RedirectAttributes redirect) {
        try {
            client.addBanner(id, title, linkUrl, image);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/banners/{id}/delete")
    public String deleteBanner(@PathVariable Integer id) {
        client.deleteBanner(id);
        return "redirect:/admin/category-teams";
    }

    @PostMapping("/banners/{id}/move")
    public String moveBanner(@PathVariable Integer id, @RequestParam String direction) {
        client.moveBanner(id, direction);
        return "redirect:/admin/category-teams";
    }
}
