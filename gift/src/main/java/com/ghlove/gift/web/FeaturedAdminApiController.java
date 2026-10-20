package com.ghlove.gift.web;

import com.ghlove.gift.service.FeaturedManageService;
import com.ghlove.gift.service.GiftException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 이벤트 관리(AS-IS opmanager/featured) cross-service API - admin 콘솔({@code /featured/*})이 부른다.
 * 조회·저장 규칙은 {@link FeaturedManageService}(AS-IS featured-mapper)에 있다.
 */
@RestController
@RequestMapping("/api/admin/featured")
@RequiredArgsConstructor
public class FeaturedAdminApiController {

    private final FeaturedManageService service;

    public record ListRequest(FeaturedManageService.Search search, int offset, int limit) {
    }

    public record IdsRequest(List<Integer> ids) {
    }

    @PostMapping("/count")
    public int count(@RequestBody FeaturedManageService.Search search) {
        return service.count(search);
    }

    @PostMapping("/list")
    public List<FeaturedManageService.Row> list(@RequestBody ListRequest request) {
        return service.list(request.search(), request.offset(), request.limit());
    }

    @GetMapping("/{featuredId}")
    public FeaturedManageService.Detail detail(@PathVariable int featuredId) {
        FeaturedManageService.Detail detail = service.detail(featuredId);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return detail;
    }

    /** 등록 - 폼 칸 이름 그대로 + 첨부 3개(featuredFile 상단 PC·featuredFileMobile 상단 mobile·featuredListFile 목록). */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestParam Map<String, String> params,
                                                      @RequestParam(required = false) MultipartFile featuredFile,
                                                      @RequestParam(required = false) MultipartFile featuredFileMobile,
                                                      @RequestParam(required = false) MultipartFile featuredListFile) {
        try {
            int id = service.create(form(params), params.get("locgovCode"), featuredFile, featuredFileMobile, featuredListFile);
            return ResponseEntity.ok(Map.of("featuredId", id));
        } catch (GiftException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{featuredId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable int featuredId, @RequestParam Map<String, String> params,
                                                      @RequestParam(required = false) MultipartFile featuredFile,
                                                      @RequestParam(required = false) MultipartFile featuredFileMobile,
                                                      @RequestParam(required = false) MultipartFile featuredListFile) {
        try {
            service.update(featuredId, form(params), featuredFile, featuredFileMobile, featuredListFile);
            return ResponseEntity.ok(Map.of("featuredId", featuredId));
        } catch (GiftException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{featuredId}/delete-image")
    public Map<String, Object> deleteImage(@PathVariable int featuredId, @RequestParam int fileType) {
        return Map.of("isSuccess", service.deleteImage(featuredId, fileType));
    }

    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody IdsRequest request) {
        return Map.of("deleted", service.delete(request.ids()));
    }

    private static FeaturedManageService.Form form(Map<String, String> p) {
        return new FeaturedManageService.Form(p.get("featuredType"), p.get("featuredName"), p.get("startDate"), p.get("startTime"),
                p.get("endDate"), p.get("endTime"), p.get("featuredPhoneNo1"), p.get("featuredPhoneNo2"), p.get("featuredPhoneNo3"),
                p.get("featuredHost"), p.get("link"), p.get("featuredContent"), p.get("prodState"), p.get("displayListFlag"),
                p.get("featuredFlag"), p.get("prodString"));
    }
}
