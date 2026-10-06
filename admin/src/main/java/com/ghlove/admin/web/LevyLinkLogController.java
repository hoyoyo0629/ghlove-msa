package com.ghlove.admin.web;

import com.ghlove.admin.service.LevyLogClient;
import com.ghlove.admin.web.support.LevyLogParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 연계 로그 조회 4화면 (메뉴 1413~1416) - AS-IS
 * {@code saleson.shop.log.LogManagerController}의 gif-seoul-buga / gif-seoul-sunap /
 * gif-stnd-buga / gif-stnd-sunap 재현.
 *
 * <p><b>예전 TO-BE는 이 4개를 {@code /log/levy} 한 화면으로 합쳐 뒀다</b>(컬럼 11/9/34/37로 서로
 * 다른 별개 화면인데 제목도 "연계 로그 관리 (국세청 부과·수납)"으로 하나였다). AS-IS대로 4개로
 * 분리했고, 메뉴 1413~1416의 menu_url도 각각으로 바로잡았다
 * ({@code migration-admin-menu-1413-1416-levy-split.sql}).
 *
 * <p>원천 표 네 개는 모두 donation 소유라 조회 전용 API를 거친다
 * ({@code /api/admin/levy-logs/*}). 실제 연계(쓰기)는 납부게이트웨이 이식 라운드 소관이고,
 * 개발DB 검증용 시드데이터는 {@code seed-donation-levy-link-logs.sql}에 있다.
 *
 * <p>AS-IS 공통 동작: 날짜가 비면 <b>오늘</b>로 채우고, 화면출력을 바꾸지 않았으면(itemsPerPageTemp=0)
 * 목록수는 <b>20</b>이다.
 */
@Controller
@RequiredArgsConstructor
public class LevyLinkLogController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** AS-IS 기본 목록수. */
    private static final int DEFAULT_ITEMS_PER_PAGE = 20;

    private final LevyLogClient levyLogClient;

    /** 1413 서울세외 부과연계 로그. */
    @RequestMapping(value = "/log/levy/seoul-buga", method = { RequestMethod.GET, RequestMethod.POST })
    public String seoulBuga(@ModelAttribute("levyLogParam") LevyLogParam param,
                            HttpServletRequest request, Model model) {
        applyDefaults(param);
        List<LevyLogClient.SeoulBugaRow> all = levyLogClient.seoulBuga(param.getSrchTxt(),
                param.getSrchErrorCd(), param.getSrchStartLogDate(), param.getSrchEndLogDate());
        page(model, all, param, request);
        return "log/levy-seoul-buga-list";
    }

    /** 1414 서울 수납연계 로그. */
    @RequestMapping(value = "/log/levy/seoul-sunap", method = { RequestMethod.GET, RequestMethod.POST })
    public String seoulSunap(@ModelAttribute("levyLogParam") LevyLogParam param,
                             HttpServletRequest request, Model model) {
        applyDefaults(param);
        List<LevyLogClient.SeoulSunapRow> all = levyLogClient.seoulSunap(param.getSrchTxt(),
                param.getSrchRstCd(), param.getSrchStartLogDate(), param.getSrchEndLogDate());
        page(model, all, param, request);
        return "log/levy-seoul-sunap-list";
    }

    /** 1415 지방세외 부과연계 로그. */
    @RequestMapping(value = "/log/levy/stnd-buga", method = { RequestMethod.GET, RequestMethod.POST })
    public String stndBuga(@ModelAttribute("levyLogParam") LevyLogParam param,
                           HttpServletRequest request, Model model) {
        applyDefaults(param);
        List<LevyLogClient.StndBugaRow> all = levyLogClient.stndBuga(param.getSrchStartLogDate(),
                param.getSrchEndLogDate(), param.getBugaStatusCd(), param.getLinkRstCd(),
                param.getEpayNo(), param.getSrchlinkRstYn(), param.getSrchpyrNm());
        page(model, all, param, request);
        return "log/levy-stnd-buga-list";
    }

    /** 1416 지방세외 수납연계 로그. */
    @RequestMapping(value = "/log/levy/stnd-sunap", method = { RequestMethod.GET, RequestMethod.POST })
    public String stndSunap(@ModelAttribute("levyLogParam") LevyLogParam param,
                            HttpServletRequest request, Model model) {
        applyDefaults(param);
        List<LevyLogClient.StndSunapRow> all = levyLogClient.stndSunap(param.getSrchStartLogDate(),
                param.getSrchEndLogDate(), param.getEpayNo());
        page(model, all, param, request);
        return "log/levy-stnd-sunap-list";
    }

    /** AS-IS 네 컨트롤러가 똑같이 하는 두 가지 - 날짜 기본값(오늘)과 목록수 기본값(20). */
    private static void applyDefaults(LevyLogParam param) {
        String today = LocalDate.now().format(DAY);
        if (param.getSrchStartLogDate() == null || param.getSrchStartLogDate().isBlank()) {
            param.setSrchStartLogDate(today);
        }
        if (param.getSrchEndLogDate() == null || param.getSrchEndLogDate().isBlank()) {
            param.setSrchEndLogDate(today);
        }
        if (param.getItemsPerPageTemp() == 0) {
            param.setItemsPerPage(DEFAULT_ITEMS_PER_PAGE);
        }
    }

    private static void page(Model model, List<?> all, LevyLogParam param, HttpServletRequest request) {
        Pagination pagination = Pagination.of(all.size(), param.getPage(), param.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
    }
}
