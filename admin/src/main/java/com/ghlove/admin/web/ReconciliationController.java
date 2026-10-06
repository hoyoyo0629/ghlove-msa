package com.ghlove.admin.web;

import com.ghlove.admin.service.PointCheckService;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.PointCheckParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 포인트사용 정합성검증 (메뉴 7210) - AS-IS saleson.shop.pointcheck.PointCheckManagerController
 * ({@code /opmanager/point-check/list}) 재현.
 *
 * <p>주문번호 검색 · 정합여부(전체/금액일치/금액불일치) · 주문일자 범위 · 출력수(10/50/100/200/500)
 * 와 11컬럼(주문번호/지자체명/주문상태/결제금액/취소금액/입금대기금액/정산금액/사용완료 포인트/
 * 정합여부/미정주문건수/주문일자), 그리고 맨 위의 합계 행이다.
 *
 * <p>예전 TO-BE는 AS-IS와 다른 대사(주문 point_amount ↔ 포인트 원장 PT_POINT_LEDGER)를 만들어
 * 검색·컬럼 없이 보여주고 있었다. AS-IS는 <b>기부포인트 사용이력(G_CNTR_USE_POINT)</b>과 대조하므로
 * 그쪽으로 되돌렸고, 세 표의 소유 서비스가 달라 order·point에 조회 전용 집계 API를 새로 추가했다
 * (사용자 승인 후 진행, 2026-10-03).
 */
@Controller
@RequiredArgsConstructor
public class ReconciliationController {

    private final PointCheckService pointCheckService;

    @RequestMapping(value = { "/reconciliation/order-point", "/reconciliation/order-point/list" },
            method = { RequestMethod.GET, RequestMethod.POST })
    public String orderPoint(@ModelAttribute("pointCheckParam") PointCheckParam param,
                             HttpServletRequest request, Model model) {
        if (param.getItemsPerPage() <= 0) {
            param.setItemsPerPage(Pagination.DEFAULT_ITEMS_PER_PAGE);
        }
        List<PointCheckService.PointCheckRow> all = pointCheckService.getPointCheckList(param);

        Pagination pagination = Pagination.of(all.size(), param.getPage(), param.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("pointCheckList", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("totalCount", all.size());
        model.addAttribute("pagination", pagination);
        return "reconciliation/order-point";
    }
}
