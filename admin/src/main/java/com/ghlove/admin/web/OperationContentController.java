package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.ContentException;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OperationContentService;
import com.ghlove.admin.service.PopupImageStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OperationContentController {

    private final OperationContentService operationContentService;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;
    private final PopupImageStorageService popupImageStorageService;

    // ---- 공지사항 (공개 화면, AS-IS notice/list.html·detail.html) ----

    private static final int NOTICE_PAGE_SIZE_DEFAULT = 10;

    @GetMapping("/notices")
    public String notices(@RequestParam(required = false) String category,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false, defaultValue = "CREATED_DATE__DESC") String sort,
                           @RequestParam(required = false, defaultValue = "10") int size,
                           @RequestParam(required = false, defaultValue = "1") int page,
                           Model model) {
        var all = operationContentService.searchPublic(category, q, sort);
        int pageSize = size > 0 ? size : NOTICE_PAGE_SIZE_DEFAULT;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<Notice> pageItems = all.subList(from, to);

        Map<Integer, String> displayDates = new HashMap<>();
        for (Notice n : pageItems) {
            displayDates.put(n.getNoticeId(), formatDate(n.getCreatedDate()));
        }

        model.addAttribute("notices", pageItems);
        model.addAttribute("displayDates", displayDates);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("category", category);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageSize);
        return "content/notice-list";
    }

    @GetMapping("/notices/{id}")
    public String noticeDetail(@PathVariable Integer id, Model model) {
        operationContentService.addHit(id);
        var notice = operationContentService.notice(id);
        model.addAttribute("notice", notice);
        model.addAttribute("displayDate", formatDate(notice.getCreatedDate()));
        model.addAttribute("displayHits", notice.getHits() == null ? 0 : notice.getHits());
        return "content/notice-detail";
    }

    /** CREATED_DATE는 yyyyMMddHHmmss로 저장되므로 AS-IS와 동일하게 yyyy-MM-dd로 표시한다. */
    private static String formatDate(String createdDate) {
        if (createdDate == null || createdDate.length() < 8) {
            return "";
        }
        return createdDate.substring(0, 4) + "-" + createdDate.substring(4, 6) + "-" + createdDate.substring(6, 8);
    }

    /** donation 서비스(기금사업소개 "지자체공지사항" 탭)가 크로스서비스로 호출하는 읽기
     *  전용 API - 시딩 규모가 작아 페이징 없이 전체를 돌려주고, 호출 측에서 잘라 쓴다. */
    @GetMapping("/api/notices")
    @ResponseBody
    public List<NoticeDto> noticesByLocgov(@RequestParam String locgovCode) {
        return operationContentService.noticesByLocgov(locgovCode).stream()
                .map(n -> new NoticeDto(n.getNoticeId(), n.getSubject(), formatDate(n.getCreatedDate())))
                .toList();
    }

    // ---- 공지사항 관리 (내부 운영 콘솔) ----

    @GetMapping("/admin/notices")
    public String adminNotices(@RequestParam(required = false) String category, HttpSession session, Model model) {
        Manager viewer = manager(session);
        List<Notice> notices = operationContentService.notices(category);
        if (MenuService.isLocgovScoped(viewer)) {
            notices = notices.stream()
                    .filter(n -> viewer.getLocgovCode().equals(n.getLocgovCode()))
                    .toList();
        }
        Map<String, String> locgovNames = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovNames.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm()));
        model.addAttribute("notices", notices);
        model.addAttribute("locgovNames", locgovNames);
        model.addAttribute("categoryLabels", commonCodeService.labelsOf("NOTICE_CATEGORY"));
        model.addAttribute("selectedCategory", category);
        return "content/admin-notice-list";
    }

    @GetMapping("/admin/notices/new")
    public String noticeForm(Model model) {
        model.addAttribute("categories", commonCodeService.labelsOf("NOTICE_CATEGORY"));
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "content/notice-form";
    }

    @PostMapping("/admin/notices")
    public String createNotice(@RequestParam String subject, @RequestParam(required = false) String content,
                                @RequestParam(required = false) String categoryCode,
                                @RequestParam(required = false) String locgovCode, HttpSession session, Model model) {
        try {
            Manager viewer = manager(session);
            operationContentService.createNotice(subject, content, categoryCode, MenuService.effectiveLocgovCode(viewer, locgovCode));
            return "redirect:/admin/notices";
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", commonCodeService.labelsOf("NOTICE_CATEGORY"));
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            return "content/notice-form";
        }
    }

    @GetMapping("/admin/notices/{id}/edit")
    public String editNoticeForm(@PathVariable Integer id, HttpSession session,
                                  org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes, Model model) {
        Notice notice;
        try {
            notice = requireOwnLocgovOrThrow(id, session);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/notices";
        }
        model.addAttribute("notice", notice);
        model.addAttribute("categories", commonCodeService.labelsOf("NOTICE_CATEGORY"));
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "content/notice-form";
    }

    @PostMapping("/admin/notices/{id}/edit")
    public String updateNotice(@PathVariable Integer id, @RequestParam String subject,
                                @RequestParam(required = false) String content,
                                @RequestParam(required = false) String categoryCode,
                                @RequestParam(required = false) String locgovCode, HttpSession session, Model model) {
        try {
            Manager viewer = manager(session);
            requireOwnLocgovOrThrow(id, session);
            operationContentService.updateNotice(id, subject, content, categoryCode, MenuService.effectiveLocgovCode(viewer, locgovCode));
            return "redirect:/admin/notices";
        } catch (ContentException | ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("notice", operationContentService.notice(id));
            model.addAttribute("categories", commonCodeService.labelsOf("NOTICE_CATEGORY"));
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            return "content/notice-form";
        }
    }

    @PostMapping("/admin/notices/{id}/delete")
    public String deleteNotice(@PathVariable Integer id, HttpSession session,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            requireOwnLocgovOrThrow(id, session);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/notices";
        }
        operationContentService.deleteNotice(id);
        return "redirect:/admin/notices";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** 지자체담당자(ROLE_ADMIN_5/6)는 자기 지자체 공지만 수정/삭제할 수 있다 - AS-IS와 달리
     * 이 프로젝트는 그동안 이 스코핑이 빠져있어 다른 지자체 공지를 건드릴 수 있는 권한버그였다. */
    private Notice requireOwnLocgovOrThrow(Integer id, HttpSession session) {
        Manager viewer = manager(session);
        Notice notice = operationContentService.notice(id);
        if (MenuService.isLocgovScoped(viewer) && !viewer.getLocgovCode().equals(notice.getLocgovCode())) {
            throw new ManagerException("소속 지자체의 공지사항만 처리할 수 있습니다.");
        }
        return notice;
    }

    @PostMapping("/admin/notices/{id}/toggle")
    public String toggleNotice(@PathVariable Integer id) {
        operationContentService.toggleNotice(id);
        return "redirect:/admin/notices";
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }

    // ---- 배너 ----

    @GetMapping("/banners")
    public String banners(Model model) {
        model.addAttribute("banners", operationContentService.banners());
        return "content/banner-list";
    }

    @GetMapping("/banners/new")
    public String bannerForm() {
        return "content/banner-form";
    }

    @PostMapping("/banners")
    public String createBanner(@RequestParam String title, @RequestParam(required = false) String contents,
                                @RequestParam(required = false) String linkUrl,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) Integer displayOrder,
                                @RequestParam(required = false) String bannerType, Model model) {
        try {
            operationContentService.createBanner(title, contents, linkUrl, imageUrl, displayOrder, bannerType);
            return "redirect:/banners";
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "content/banner-form";
        }
    }

    @PostMapping("/banners/{id}/toggle")
    public String toggleBanner(@PathVariable Integer id) {
        operationContentService.toggleBanner(id);
        return "redirect:/banners";
    }

    @GetMapping("/banners/{id}/edit")
    public String bannerEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("banner", operationContentService.banner(id));
        return "content/banner-form";
    }

    @PostMapping("/banners/{id}")
    public String updateBanner(@PathVariable Integer id, @RequestParam String title,
                                @RequestParam(required = false) String contents,
                                @RequestParam(required = false) String linkUrl,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) Integer displayOrder,
                                @RequestParam(required = false) String bannerType, Model model) {
        try {
            operationContentService.updateBanner(id, title, contents, linkUrl, imageUrl, displayOrder, bannerType);
            return "redirect:/banners";
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("banner", operationContentService.banner(id));
            return "content/banner-form";
        }
    }

    // ---- 팝업 ----

    @GetMapping("/popups")
    public String popups(Model model) {
        model.addAttribute("popups", operationContentService.popups());
        model.addAttribute("typeLabels", commonCodeService.labelsOf("POPUP_TYPE"));
        return "content/popup-list";
    }

    @GetMapping("/popups/new")
    public String popupForm(Model model) {
        model.addAttribute("popup", null);
        model.addAttribute("types", commonCodeService.labelsOf("POPUP_TYPE"));
        return "content/popup-form";
    }

    @GetMapping("/popups/{id}/edit")
    public String popupEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("popup", operationContentService.popup(id));
        model.addAttribute("types", commonCodeService.labelsOf("POPUP_TYPE"));
        return "content/popup-form";
    }

    @PostMapping("/popups")
    public String createPopup(@RequestParam String subject, @RequestParam(required = false) String content,
                               @RequestParam(required = false) String popupType,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(required = false) String popupStyle,
                               @RequestParam(required = false) String popupClose,
                               @RequestParam(required = false) MultipartFile image, Model model) {
        try {
            String imageUrl = popupImageStorageService.store(image);
            operationContentService.createPopup(subject, content, popupType, startDate, endDate, popupStyle, popupClose, imageUrl);
            return "redirect:/popups";
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", commonCodeService.labelsOf("POPUP_TYPE"));
            return "content/popup-form";
        }
    }

    @PostMapping("/popups/{id}")
    public String updatePopup(@PathVariable Integer id, @RequestParam String subject, @RequestParam(required = false) String content,
                               @RequestParam(required = false) String popupType,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(required = false) String popupStyle,
                               @RequestParam(required = false) String popupClose,
                               @RequestParam(required = false) MultipartFile image, Model model) {
        try {
            String imageUrl = popupImageStorageService.store(image);
            operationContentService.updatePopup(id, subject, content, popupType, startDate, endDate, popupStyle, popupClose, imageUrl);
            return "redirect:/popups";
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("popup", operationContentService.popup(id));
            model.addAttribute("types", commonCodeService.labelsOf("POPUP_TYPE"));
            return "content/popup-form";
        }
    }

    @PostMapping("/popups/{id}/delete")
    public String deletePopup(@PathVariable Integer id) {
        operationContentService.deletePopup(id);
        return "redirect:/popups";
    }

    @PostMapping("/popups/{id}/toggle")
    public String togglePopup(@PathVariable Integer id) {
        operationContentService.togglePopup(id);
        return "redirect:/popups";
    }
}
