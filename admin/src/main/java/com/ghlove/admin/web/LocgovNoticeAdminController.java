package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.ContentException;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.LocgovNoticeAdminService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.LocgovNoticeSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 지자체공지사항 (메뉴 5110) - AS-IS {@code LocgovNoticeManagerController}
 * ({@code /opmanager/locgov-notice/**}) 이식. 5111 공지사항({@link OperationContentController})과
 * 같은 {@code OP_NOTICE}를 쓰지만 AS-IS처럼 <b>컨트롤러·화면이 별도</b>다 - 여기는 항상
 * {@code LOCGOV_CODE <> '00000'}인 행만 보고, 작성자 칸에 사람 이름 대신 <b>지자체명</b>을 보여준다.
 *
 * <p><b>AS-IS 역할 분기</b>: 지자체 담당자(LOC)로 로그인하면 자기 지자체 공지만 보이고
 * 등록·선택삭제 버튼도 LOC일 때만 보인다. 시스템·행안부는 지자체 조건 없이 전체(= 전체공지만
 * 빼고 모든 지자체)를 본다 - AS-IS {@code getLoginUserAdminRoleCheck()}가 "LOC"를 돌려줄 때만
 * 검색조건에 locgovCode를 채우는 느슨한 분기 그대로다.
 *
 * <p><b>수정화면은 역할 검사를 하지 않는다</b>(AS-IS edit.jsp에 role 참조가 전혀 없다) - 다만
 * 이 프로젝트는 5111에서 같은 표에 대해 "다른 지자체 공지를 건드릴 수 있는 권한버그"를 먼저
 * 찾아 고친 전례가 있어({@link OperationContentController#requireOwnLocgovOrThrow}) 같은
 * 가드를 여기도 적용한다(LOC 담당자가 타 지자체 공지를 수정·삭제하지 못하게).
 */
@Controller
@RequiredArgsConstructor
public class LocgovNoticeAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String BASE_URL = "/admin/locgov-notices";

    private final LocgovNoticeAdminService locgovNoticeAdminService;
    private final LocgovClient locgovClient;
    private final CommonMessageService commonMessageService;
    private final FlashRedirect flashRedirect;

    /** AS-IS GET list - 진입에서는 조회하지 않는다(빈 목록). 날짜 검색조건은 오늘로 채워 보여준다. */
    @GetMapping(BASE_URL)
    public String list(@ModelAttribute("searchParam") LocgovNoticeSearchParam searchParam,
                       HttpSession session, HttpServletRequest request, Model model) {
        fillList(searchParam, session, request, model, false);
        return "locgov-notice/list";
    }

    /** AS-IS POST list - 검색. */
    @PostMapping(BASE_URL)
    public String searchList(@ModelAttribute("searchParam") LocgovNoticeSearchParam searchParam,
                             HttpSession session, HttpServletRequest request, Model model) {
        fillList(searchParam, session, request, model, true);
        return "locgov-notice/list";
    }

    private void fillList(LocgovNoticeSearchParam searchParam, HttpSession session,
                          HttpServletRequest request, Model model, boolean doSearch) {
        searchParam.applyDefaults();
        Manager viewer = manager(session);
        boolean locScoped = MenuService.isLocgovScoped(viewer);
        String locgovCode = locScoped ? viewer.getLocgovCode() : null;

        int count = 0;
        List<Notice> list = List.of();
        if (doSearch) {
            count = locgovNoticeAdminService.count(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate(), locgovCode);
        }
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        if (doSearch) {
            list = locgovNoticeAdminService.list(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate(), locgovCode,
                    pagination.getStartRow(), pagination.getItemsPerPage());
        }

        Map<String, String> locgovNames = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovNames.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm()));

        model.addAttribute("list", list);
        model.addAttribute("locgovNames", locgovNames);
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", count);
        model.addAttribute("role", locScoped ? "LOC" : "");
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
    }

    /** AS-IS GET create. */
    @GetMapping(BASE_URL + "/create")
    public String createForm() {
        return "locgov-notice/form";
    }

    /** AS-IS POST create - LOC 담당자일 때만 자기 지자체 코드를 채운다(그 외는 null). */
    @PostMapping(BASE_URL + "/create")
    public String create(@RequestParam String subject, @RequestParam(required = false) String content,
                         @RequestParam(required = false) String noticeFlag,
                         @RequestParam String useYn, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String locgovCode = MenuService.isLocgovScoped(viewer) ? viewer.getLocgovCode() : null;
        try {
            locgovNoticeAdminService.create(subject, content, noticeFlag, useYn, locgovCode);
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "locgov-notice/form";
        }
        // AS-IS: ViewUtils.redirect(..., "등록되었습니다.") - 메시지 코드가 아니라 하드코딩 문자열이다.
        return flashRedirect.to(BASE_URL, "등록되었습니다.");
    }

    /** AS-IS GET edit/{noticeId} - role 검사 없이 누구나 열 수 있다(AS-IS 그대로). */
    @GetMapping(BASE_URL + "/edit/{noticeId}")
    public String editForm(@PathVariable Integer noticeId, Model model) {
        model.addAttribute("notice", locgovNoticeAdminService.notice(noticeId));
        return "locgov-notice/edit";
    }

    /** AS-IS POST edit/{noticeId}. */
    @PostMapping(BASE_URL + "/edit/{noticeId}")
    public String edit(@PathVariable Integer noticeId, @RequestParam String subject,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) String noticeFlag,
                       @RequestParam String useYn, HttpSession session, Model model) {
        try {
            requireOwnLocgovOrThrow(noticeId, session);
            locgovNoticeAdminService.update(noticeId, subject, content, noticeFlag, useYn);
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("notice", locgovNoticeAdminService.notice(noticeId));
            return "locgov-notice/edit";
        } catch (ManagerException e) {
            return flashRedirect.to(BASE_URL, e.getMessage());
        }
        return flashRedirect.to(BASE_URL, commonMessageService.get("M00289"));
    }

    /** AS-IS POST delete/{noticeId}(ajax) - 단건 삭제. */
    @PostMapping(BASE_URL + "/delete/{noticeId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Integer noticeId, HttpSession session) {
        try {
            requireOwnLocgovOrThrow(noticeId, session);
            locgovNoticeAdminService.delete(noticeId);
        } catch (RuntimeException e) {
            return Map.of("isSuccess", false, "errorMessage", "실패했습니다.");
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS POST deleteNotice - 목록 체크박스 일괄삭제(파라미터명 locgovNoticeList). */
    @PostMapping(BASE_URL + "/deleteNotice")
    @ResponseBody
    public Map<String, Object> deleteList(
            @RequestParam(value = "locgovNoticeList", required = false) List<Integer> locgovNoticeList) {
        String code = locgovNoticeAdminService.deleteList(locgovNoticeList);
        return Map.of("isSuccess", true, "data", code);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** LOC 담당자는 자기 지자체 공지만 수정·삭제할 수 있다 - 5111에서 먼저 찾은 권한버그와 같은 가드. */
    private void requireOwnLocgovOrThrow(Integer noticeId, HttpSession session) {
        Manager viewer = manager(session);
        if (!MenuService.isLocgovScoped(viewer)) {
            return;
        }
        Notice notice = locgovNoticeAdminService.notice(noticeId);
        if (!viewer.getLocgovCode().equals(notice.getLocgovCode())) {
            throw new ManagerException("소속 지자체의 공지사항만 처리할 수 있습니다.");
        }
    }
}
