package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.PrivacyAccessLog;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.PrivacyAccessLogService;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.PrivacyLogParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 엑셀다운로드 사유 관리(메뉴 1411) - AS-IS saleson.shop.log.LogManagerController의
 * exceldownload-log 재현.
 *
 * <p>읽는 표는 AS-IS와 같은 <b>개인정보 접근로그</b>({@code OP_PRIVACY_ACCESS_LOG})이고 사유
 * 변경이력은 {@code OP_PRIVACY_ACCESS_LOG_HIST}다. 예전 TO-BE는 AS-IS에 없는
 * {@code op_excel_download_log}(admin) + order 서비스 로그를 합쳐 보여주는 별도 설계였다.
 *
 * <p>AS-IS 쿼리 규칙 그대로: <b>{@code TASK = '엑셀 다운로드'}인 행만</b> 보고, 등록일 기본값은
 * 오늘, 기본 목록수 20, <b>행안부/시스템 담당자(ROLE_ADMIN_1~4)가 아니면 본인(MANAGER_ID) 것만</b>
 * 보인다. 관리자ID 칸은 MANAGER_ID로 찾은 OP_MANAGER.LOGIN_ID이고, 검색구분 관리자ID는
 * <b>완전일치</b>(다운로드 메뉴·사유는 부분일치)다.
 */
@Controller
@RequiredArgsConstructor
public class ExcelDownloadLogAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PrivacyAccessLogService privacyAccessLogService;

    /** 목록 한 줄 - 사유 변경이력이 2건 이상이면 화면에 "이력보기"가 뜬다(AS-IS histCnt). */
    public record Row(PrivacyAccessLog log, long histCnt) {
    }

    @RequestMapping(value = "/admin/excel-download-logs", method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("privacyLogParam") PrivacyLogParam privacyLogParam,
                       HttpSession session, HttpServletRequest request, Model model) {
        // AS-IS: 등록일이 비어 있으면 오늘로 채운다
        String today = LocalDate.now().format(DAY);
        if (privacyLogParam.getSrchStartLogDate() == null || privacyLogParam.getSrchStartLogDate().isBlank()) {
            privacyLogParam.setSrchStartLogDate(today);
        }
        if (privacyLogParam.getSrchEndLogDate() == null || privacyLogParam.getSrchEndLogDate().isBlank()) {
            privacyLogParam.setSrchEndLogDate(today);
        }
        if (privacyLogParam.getItemsPerPageTemp() == 0) {
            privacyLogParam.setItemsPerPage(20);
        }

        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        boolean seeAll = viewer != null && MenuService.UNRESTRICTED_ROLES.contains(viewer.getAuthority());
        // AS-IS adminUserId 분기 - 전체권한이 아니면 MANAGER_ID로 본인 것만 본다
        Long onlyManagerId = (seeAll || viewer == null) ? null : viewer.getUserId();

        List<Row> all = privacyAccessLogService
                .excelDownloadLogs(privacyLogParam.getSrchStartLogDate(), privacyLogParam.getSrchEndLogDate(),
                        onlyManagerId)
                .stream()
                .filter(p -> privacyLogParam.matchesKeyword(p.getLoginId(), p.getName(), p.getReason()))
                .map(p -> new Row(p, privacyAccessLogService.histCountOf(p.getId())))
                .toList();

        Pagination pagination = Pagination.of(all.size(), privacyLogParam.getPage(), privacyLogParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "log/excel-download-log-list";
    }

    /** AS-IS exceldownload-log/popup/{id} - 사유 전문 팝업. */
    @GetMapping("/admin/excel-download-logs/popup/{id}")
    public String reasonPopup(@PathVariable Long id, Model model) {
        model.addAttribute("details", privacyAccessLogService.detail(id));
        return "log/excel-download-log-reason";
    }

    /** AS-IS 이력보기 팝업 - 사유 변경이력(최신순). */
    @GetMapping("/admin/excel-download-logs/hist/{id}")
    public String reasonHistPopup(@PathVariable Long id, Model model) {
        model.addAttribute("list", privacyAccessLogService.histOf(id));
        return "log/excel-download-log-hist";
    }
}
