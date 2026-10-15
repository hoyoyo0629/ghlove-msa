package com.ghlove.gift.web;

import com.ghlove.gift.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 관리자 메인화면의 답례품 집계 - admin이 cross-service로 호출(/api/admin/** = X-Internal-Secret 보호). */
@RestController
@RequiredArgsConstructor
public class AdminDashboardApiController {

    private final AdminDashboardService service;

    /** date = 기부일자(yyyyMMdd). */
    @GetMapping("/api/admin/gifts/dashboard/item-count")
    public long itemCount(@RequestParam String date) {
        return service.registeredItemCount(date);
    }

    @GetMapping("/api/admin/gifts/dashboard/item-locgov-codes")
    public List<String> itemLocgovCodes() {
        return service.itemLocgovCodes();
    }
}
