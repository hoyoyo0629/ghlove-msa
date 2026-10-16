package com.ghlove.gift.web;

import com.ghlove.gift.service.CategoriesTeamGroupAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 카테고리 그룹 관리(AS-IS opmanager/categories-team-group) cross-service API - 대그룹/그룹(OP_CATEGORY_TEAM/GROUP).
 * admin 콘솔이 부른다(X-Internal-Secret 보호 경로 /api/admin/**).
 */
@RestController
@RequestMapping("/api/admin/categories-team-group")
@RequiredArgsConstructor
public class CategoriesTeamGroupAdminApiController {

    private final CategoriesTeamGroupAdminService service;

    @GetMapping("/team-groups")
    public List<CategoriesTeamGroupAdminService.TeamRow> teamGroups() {
        return service.teamGroupList();
    }

    @GetMapping("/teams")
    public List<CategoriesTeamGroupAdminService.TeamRow> teams() {
        return service.teamList();
    }

    @GetMapping("/groups")
    public List<CategoriesTeamGroupAdminService.GroupRow> groups() {
        return service.groupList();
    }

    @GetMapping("/groups/{id}")
    public CategoriesTeamGroupAdminService.GroupRow group(@PathVariable int id) {
        CategoriesTeamGroupAdminService.GroupRow g = service.group(id);
        if (g == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.");
        }
        return g;
    }

    @GetMapping("/groups/{id}/categories")
    public List<CategoriesTeamGroupAdminService.CategoryRef> categoriesOf(@PathVariable int id) {
        return service.categoriesOf(id);
    }

    @GetMapping("/categories-in-groups")
    public List<CategoriesTeamGroupAdminService.CategoriesInGroup> categoriesInGroups() {
        return service.categoriesInGroups();
    }

    @PostMapping("/groups")
    public Map<String, Integer> createGroup(@RequestBody CategoriesTeamGroupAdminService.GroupForm form) {
        return Map.of("categoryGroupId", service.createGroup(form));
    }

    @PutMapping("/groups")
    public void updateGroup(@RequestBody CategoriesTeamGroupAdminService.GroupForm form) {
        service.updateGroup(form);
    }

    @PostMapping("/groups/{id}/delete")
    public void deleteGroup(@PathVariable int id) {
        service.deleteGroup(id);
    }

    /** 순서변경 요청 본문 - ids[i]의 순서가 orderings[i]. */
    public record OrderingForm(List<Integer> ids, List<Integer> orderings) {
    }

    @PostMapping("/ordering/groups")
    public void groupOrdering(@RequestBody OrderingForm form) {
        service.updateGroupOrdering(form.ids(), form.orderings());
    }

    @PostMapping("/ordering/teams")
    public void teamOrdering(@RequestBody OrderingForm form) {
        service.updateTeamOrdering(form.ids(), form.orderings());
    }
}
