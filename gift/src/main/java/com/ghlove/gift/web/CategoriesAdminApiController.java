package com.ghlove.gift.web;

import com.ghlove.gift.service.CategoriesAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 카테고리관리(AS-IS opmanager/categories) cross-service API - OP_CATEGORY 1~4차 트리.
 * admin 콘솔이 부른다(X-Internal-Secret 보호 경로 /api/admin/**).
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoriesAdminApiController {

    private final CategoriesAdminService service;

    @GetMapping("/tree")
    public List<Map<String, Object>> tree(@RequestParam(required = false) String groupId) {
        return service.treeLevel1(groupId);
    }

    @GetMapping("/tree/{nodeId}")
    public List<Map<String, Object>> children(@PathVariable String nodeId) {
        return service.treeChildren(nodeId);
    }

    @GetMapping("/by-code/{categoryCode}")
    public CategoriesAdminService.Category byCode(@PathVariable String categoryCode) {
        CategoriesAdminService.Category c = service.byCode(categoryCode);
        if (c == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.");
        }
        return c;
    }

    @GetMapping("/count")
    public Map<String, Long> count(@RequestParam(required = false) String categoryUrl,
                                   @RequestParam(required = false) String categoryCode) {
        return Map.of("count", service.countByUrlOrCode(categoryUrl, categoryCode));
    }

    @PostMapping
    public CategoriesAdminService.Category create(@RequestBody CategoriesAdminService.Form form) {
        return service.create(form);
    }

    @PostMapping("/sub")
    public CategoriesAdminService.Category createSub(@RequestBody CategoriesAdminService.Form form) {
        return service.createSub(form);
    }

    @PutMapping
    public void update(@RequestBody CategoriesAdminService.Form form) {
        service.update(form);
    }

    @PostMapping("/move")
    public void move(@RequestBody CategoriesAdminService.Move move) {
        service.move(move);
    }

    @PostMapping("/delete")
    public void delete(@RequestParam String categoryCode, @RequestParam String categoryLevel) {
        service.delete(categoryCode, categoryLevel);
    }
}
