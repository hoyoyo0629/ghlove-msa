package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.ContentException;
import com.ghlove.admin.service.NoticeAdminService;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.NoticeSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 공지사항(전체공지) 관리 (메뉴 5111) - AS-IS {@code NoticeManagerController}
 * ({@code /opmanager/notice/**}) 이식. 지자체공지사항(5110, {@link LocgovNoticeAdminController})과
 * 같은 {@code OP_NOTICE}를 쓰지만 AS-IS도 컨트롤러·화면이 별도고 조회범위만 다르다 -
 * 여기는 지자체코드를 {@code '00000'}(전체공지)으로 고정해서 본다.
 *
 * <p><b>5110과 다른 점(전부 AS-IS 그대로)</b>:
 * <ul>
 *   <li>역할 분기가 <b>없다</b> - 등록·선택삭제 버튼이 권한과 무관하게 항상 보인다.</li>
 *   <li>등록일 검색조건을 <b>오늘로 채우지 않는다</b>({@link NoticeSearchParam} 주석).</li>
 *   <li>작성자 칸이 지자체명이 아니라 <b>사람 이름</b>({@code USER_NAME})이다.</li>
 *   <li>등록·수정이 <b>같은 화면</b>(AS-IS {@code form.jsp})이다 - 5110만 {@code edit.jsp}가 따로 있다.</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
public class NoticeAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String BASE_URL = "/admin/notices";

    private final NoticeAdminService noticeAdminService;
    private final CommonMessageService commonMessageService;
    private final FlashRedirect flashRedirect;

    /** AS-IS GET list - 진입에서는 조회하지 않는다(빈 목록). */
    @GetMapping(BASE_URL)
    public String list(@ModelAttribute("searchParam") NoticeSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, false);
        return "notice-admin/list";
    }

    /** AS-IS POST list - 검색. */
    @PostMapping(BASE_URL)
    public String searchList(@ModelAttribute("searchParam") NoticeSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, true);
        return "notice-admin/list";
    }

    private void fillList(NoticeSearchParam searchParam, HttpServletRequest request,
                          Model model, boolean doSearch) {
        int count = 0;
        List<Notice> list = List.of();
        if (doSearch) {
            count = noticeAdminService.count(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate());
        }
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        if (doSearch) {
            list = noticeAdminService.list(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate(),
                    pagination.getStartRow(), pagination.getItemsPerPage());
        }

        model.addAttribute("list", list);
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", count);
        // AS-IS가 같이 내려주는 날짜 기준값들 - 이 화면은 today/week/month1/month2다
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
    }

    /** AS-IS GET create - 등록·수정이 같은 화면이라 notice 없이 들어가면 등록 모드다. */
    @GetMapping(BASE_URL + "/create")
    public String createForm() {
        return "notice-admin/form";
    }

    /** AS-IS POST create. */
    @PostMapping(BASE_URL + "/create")
    public String create(@RequestParam String subject, @RequestParam(required = false) String content,
                         @RequestParam(required = false) String noticeFlag,
                         @RequestParam String useYn, HttpSession session, Model model) {
        try {
            noticeAdminService.create(subject, content, noticeFlag, useYn, managerName(session));
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "notice-admin/form";
        }
        // AS-IS: ViewUtils.redirect(..., "등록되었습니다.") - 메시지 코드가 아니라 하드코딩 문자열이다.
        return flashRedirect.to(BASE_URL, "등록되었습니다.");
    }

    /** AS-IS GET edit/{noticeId} - 등록과 같은 form.jsp를 noticeId가 있는 상태로 그린다. */
    @GetMapping(BASE_URL + "/edit/{noticeId}")
    public String editForm(@PathVariable Integer noticeId, Model model) {
        model.addAttribute("notice", noticeAdminService.notice(noticeId));
        return "notice-admin/form";
    }

    /** AS-IS POST edit/{noticeId}. */
    @PostMapping(BASE_URL + "/edit/{noticeId}")
    public String edit(@PathVariable Integer noticeId, @RequestParam String subject,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) String noticeFlag,
                       @RequestParam String useYn, HttpSession session, Model model) {
        try {
            noticeAdminService.update(noticeId, subject, content, noticeFlag, useYn, managerName(session));
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("notice", noticeAdminService.notice(noticeId));
            return "notice-admin/form";
        }
        return flashRedirect.to(BASE_URL, commonMessageService.get("M00289"));
    }

    /** AS-IS POST delete/{noticeId}(ajax) - 수정화면의 [삭제] 버튼. */
    @PostMapping(BASE_URL + "/delete/{noticeId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Integer noticeId) {
        try {
            noticeAdminService.delete(noticeId);
        } catch (RuntimeException e) {
            return Map.of("isSuccess", false, "errorMessage", "실패했습니다.");
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS POST deleteNotice - 목록 체크박스 일괄삭제(파라미터명 noticeList). */
    @PostMapping(BASE_URL + "/deleteNotice")
    @ResponseBody
    public Map<String, Object> deleteList(
            @RequestParam(value = "noticeList", required = false) List<Integer> noticeList) {
        return Map.of("isSuccess", true, "data", noticeAdminService.deleteList(noticeList));
    }

    /** AS-IS는 {@code SecurityUtils.getCurrentUser().getUserName()}을 작성자로 저장한다. */
    private static String managerName(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        return manager == null ? null : manager.getUserName();
    }
}
