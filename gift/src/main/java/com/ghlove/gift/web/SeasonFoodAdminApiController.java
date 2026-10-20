package com.ghlove.gift.web;

import com.ghlove.gift.service.SeasonFoodAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 관리자 > 제철식품관 관리(AS-IS opmanager/seasonal-food) cross-service API - admin 콘솔이 부른다(X-Internal-Secret 보호 경로 /api/admin/**). */
@RestController
@RequestMapping("/api/admin/seasonal-food")
@RequiredArgsConstructor
public class SeasonFoodAdminApiController {

    private final SeasonFoodAdminService service;

    public record SaveRequest(List<SeasonFoodAdminService.MonthKeywords> seasonalFoods, long userId) {
    }

    @GetMapping
    public List<SeasonFoodAdminService.MonthKeywords> list() {
        return service.list();
    }

    @PostMapping("/save")
    public List<SeasonFoodAdminService.MonthKeywords> save(@RequestBody SaveRequest request) {
        return service.save(request.seasonalFoods(), request.userId());
    }
}
