package com.ghlove.point.web;

import com.ghlove.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 포인트 소멸 배치 실행 화면(운영자 수동 트리거) - 실시간 스케줄러가 없어 서버렌더로 남긴다.
 *
 * <p>마이페이지 "기부포인트 조회/현황 상세"(my.html/detail.html)와 "포인트 예약"(reservations.html)
 * 등 대민 서버렌더 흐름은 storefront Vue3 SPA(MyPointsView.vue/PointReservationsView.vue) +
 * {@code /api/my/points*}·{@code /api/my/reservations*}로 이관되어 제거됐다(2026-09-17 Thymeleaf 폐기).
 */
@Controller
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /** 실시간 스케줄러가 없어 운영자가 수동으로 트리거하는 소멸 배치 실행 화면. */
    @GetMapping("/batch/expire")
    public String expireBatchForm(Model model) {
        return "expire-batch";
    }

    @PostMapping("/batch/expire")
    public String runExpireBatch(Model model) {
        int count = pointService.runExpirationBatch();
        model.addAttribute("resultCount", count);
        return "expire-batch";
    }
}
