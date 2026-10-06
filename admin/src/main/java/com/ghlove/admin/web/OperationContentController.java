package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.domain.Popup;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.ContentException;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OperationContentService;
import com.ghlove.admin.service.PopupImageStorageService;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.PopupSearchParam;
import jakarta.servlet.http.HttpServletRequest;
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
    private final CommonMessageService commonMessageService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final com.ghlove.admin.service.MainBannerImageStorageService mainBannerImageStorageService;

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

    /**
     * 공지사항 (메뉴 5111) - AS-IS {@code NoticeManagerController}
     * ({@code /opmanager/notice/list}). <b>전체공지만</b> 본다.
     *
     * <p>AS-IS는 검색조건의 지자체코드를 {@code "00000"}으로 <b>고정</b>해서 전체공지만 조회한다.
     * TO-BE 데이터는 전체공지의 {@code locgov_code}를 비워 두었으므로(실측: 빈 값 14행)
     * <b>NULL·빈 문자열·'00000'을 모두 전체공지로 본다</b> - 담당자 사용여부 9/2를
     * ACTIVE/LOCKED로 바꾼 것과 같은 경계에서의 코드체계 번역이다.
     */
    @GetMapping("/admin/notices")
    public String adminNotices(@RequestParam(required = false) String category, HttpSession session, Model model) {
        return fillNoticeList(category, true, session, model);
    }

    /**
     * 지자체공지사항 (메뉴 5110) - AS-IS {@code LocgovNoticeManagerController}
     * ({@code /opmanager/locgov-notice/list}).
     *
     * <p>AS-IS는 <b>지자체 담당자(LOC)일 때만</b> 자기 지자체로 스코프하고, 시스템·행안부는
     * 지자체 조건을 걸지 않아 <b>전체공지까지 포함해 모든 행</b>을 본다 - 그 느슨한 동작까지 그대로 둔다
     * (AS-IS {@code getLocgovNoticeList}에 locgov_code 조건이 {@code locgovCode}가 있을 때만 붙는다).
     *
     * <p>5111 공지사항과 <b>같은 표({@code op_notice})를 쓰고 조회 범위만 다르다</b> - AS-IS도
     * 컨트롤러·매퍼만 둘이고 표는 하나다. 그래서 등록·수정·삭제·게시토글은
     * {@code /admin/notices/...} 한 경로를 공유한다.
     */
    @GetMapping("/admin/locgov-notices")
    public String adminLocgovNotices(@RequestParam(required = false) String category,
                                      HttpSession session, Model model) {
        return fillNoticeList(category, false, session, model);
    }

    private String fillNoticeList(String category, boolean wholeNoticeOnly, HttpSession session,
                                   Model model) {
        Manager viewer = manager(session);
        List<Notice> notices = operationContentService.notices(category);
        if (wholeNoticeOnly) {
            notices = notices.stream().filter(OperationContentController::isWholeNotice).toList();
        } else if (MenuService.isLocgovScoped(viewer)) {
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
        model.addAttribute("screenTitle", wholeNoticeOnly ? "공지사항" : "지자체공지사항");
        model.addAttribute("listUrl", wholeNoticeOnly ? "/admin/notices" : "/admin/locgov-notices");
        return "content/admin-notice-list";
    }

    /** AS-IS는 전체공지를 {@code locgov_code='00000'}으로 쓴다 - TO-BE 데이터는 비워 두었다. */
    private static boolean isWholeNotice(Notice notice) {
        String locgovCode = notice.getLocgovCode();
        return locgovCode == null || locgovCode.isBlank() || "00000".equals(locgovCode);
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

    /**
     * AS-IS mainBanner(/opmanager/user-login-banner/index → banner/main/list.jsp) 재현.
     * 5컬럼(배너명/내용/사용여부/관리/순서) 표이고 등록된 배너 뒤는 **10행까지 빈 행**으로 채워
     * 그 자리에 등록 버튼을 둔다. 순서는 행마다 셀렉트로 고르고 하단 저장으로 한 번에 반영한다.
     */
    @GetMapping("/banners")
    public String banners(Model model) {
        model.addAttribute("list", operationContentService.banners());
        return "content/banner-list";
    }

    /** AS-IS mainBannerCreate(GET create) - 등록화면(pageValidFlag 없음). */
    @GetMapping("/banners/create")
    public String bannerCreateForm(Model model) {
        model.addAttribute("details", null);
        model.addAttribute("pageValidFlag", "");
        return "content/banner-form";
    }

    /** AS-IS mainBannerCreate(GET edit/{bannerId}) - 없는 배너면 pageValidFlag='N'(화면이 "잘못된 접근"). */
    @GetMapping("/banners/edit/{bannerId}")
    public String bannerEditForm(@PathVariable Integer bannerId, Model model) {
        var details = operationContentService.bannerOrNull(bannerId);
        model.addAttribute("details", details);
        model.addAttribute("pageValidFlag", details == null ? "N" : "Y");
        return "content/banner-form";
    }

    /** AS-IS mainBannerCreateProcess(POST create) - FormData(ajax). */
    @PostMapping("/banners/create")
    @ResponseBody
    public Map<String, Object> createBanner(@RequestParam String title,
                                            @RequestParam(required = false) String contents,
                                            @RequestParam(required = false) String linkUrl,
                                            @RequestParam(required = false) String displayFlag,
                                            @RequestParam(value = "pcFile", required = false) MultipartFile pcFile,
                                            @RequestParam(value = "mFile", required = false) MultipartFile mFile) {
        operationContentService.createMainBanner(title, contents, linkUrl, displayFlag,
                mainBannerImageStorageService.store(pcFile), originalNameOf(pcFile),
                mainBannerImageStorageService.store(mFile), originalNameOf(mFile));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        return result;
    }

    /** AS-IS mainBannerEditProcess(POST edit) - FormData(ajax). 이미지는 새로 올린 쪽만 바뀐다. */
    @PostMapping("/banners/edit")
    @ResponseBody
    public Map<String, Object> updateBanner(@RequestParam Integer bannerId,
                                            @RequestParam String title,
                                            @RequestParam(required = false) String contents,
                                            @RequestParam(required = false) String linkUrl,
                                            @RequestParam(required = false) String displayFlag,
                                            @RequestParam(value = "pcFile", required = false) MultipartFile pcFile,
                                            @RequestParam(value = "mFile", required = false) MultipartFile mFile) {
        operationContentService.updateMainBanner(bannerId, title, contents, linkUrl, displayFlag,
                mainBannerImageStorageService.store(pcFile), originalNameOf(pcFile),
                mainBannerImageStorageService.store(mFile), originalNameOf(mFile));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        return result;
    }

    /** AS-IS changeDisplayOrder - "bannerId|순서" 배열을 받아 한 번에 반영한다. */
    @PostMapping("/banners/change-display-order")
    @ResponseBody
    public Map<String, Object> changeBannerDisplayOrder(
            @RequestParam(value = "displayOrderList", required = false) List<String> displayOrderList) {
        operationContentService.changeBannerDisplayOrder(displayOrderList);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        result.put("data", true);   // 화면 JS가 response.data 까지 확인한다
        return result;
    }

    /** AS-IS mainBanner/pc|m/{bannerId} - 저장한 배너 이미지를 스트리밍한다. */
    @GetMapping("/banners/mainBanner/{type}/{bannerId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> bannerImage(@PathVariable String type,
                                                                       @PathVariable Integer bannerId) {
        var banner = operationContentService.bannerOrNull(bannerId);
        if (banner == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        String stored = "pc".equalsIgnoreCase(type) ? banner.getPcFileName() : banner.getMFileName();
        byte[] bytes = mainBannerImageStorageService.read(stored);
        if (bytes == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Type", mainBannerImageStorageService.contentTypeOf(stored))
                .body(bytes);
    }

    private static String originalNameOf(MultipartFile file) {
        return (file == null || file.isEmpty()) ? null : file.getOriginalFilename();
    }

    @PostMapping("/banners/{id}/toggle")
    public String toggleBanner(@PathVariable Integer id) {
        operationContentService.toggleBanner(id);
        return "redirect:/banners";
    }

    // AS-IS에 없던 폼 저장 경로(POST /banners/{id}, imageUrl 텍스트 입력 기반)는 제거했다.
    // AS-IS는 /banners/edit 로 FormData(ajax) 저장하고 이미지는 PC/모바일 파일 업로드다.

    // ---- 팝업 ----

    /**
     * AS-IS PopupManagerController.popupList(/opmanager/popup/list, GET·POST 양쪽) 재현.
     * 목록에 뿌리는 팝업상태·팝업형태·팝업타입은 AS-IS가 컨트롤러에서 코드값을 문구로 치환해
     * 내려주므로(OP_COMMON_CODE가 아니라 OP_COMMON_MESSAGE를 쓴다) 그 분기를 그대로 옮겼다.
     * 원본 코드값이 화면 수정링크 등에 필요하진 않으므로 AS-IS처럼 치환한 값만 담는다.
     */
    @RequestMapping(value = "/popups", method = { RequestMethod.GET, RequestMethod.POST })
    public String popups(@ModelAttribute("popupSearchParam") PopupSearchParam popupSearchParam,
                         HttpServletRequest request, Model model) {
        List<Popup> found = operationContentService.popupSearch(popupSearchParam);
        int popupCount = found.size();

        Pagination pagination = Pagination.of(popupCount, popupSearchParam.getPage()).withLinkFrom(request);
        List<Popup> popupList = found.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList();

        for (Popup popup : popupList) {
            popup.setPopupClose(popupCloseLabel(popup.getPopupClose()));
            popup.setPopupStyle(popupStyleLabel(popup.getPopupStyle()));
            popup.setPopupType("1".equals(popup.getPopupType())
                    ? commonMessageService.get("M00747")    // 윈도우
                    : commonMessageService.get("M00748"));  // 레이어
        }

        model.addAttribute("popupCount", popupCount);
        model.addAttribute("popupList", popupList);
        model.addAttribute("pagination", pagination);
        return "content/popup-list";
    }

    /** AS-IS: case 1 사용 / case 2 일시 정지 / default 종료. */
    private String popupCloseLabel(String popupClose) {
        return switch (asInt(popupClose)) {
            case 1 -> commonMessageService.get("M00083");
            case 2 -> commonMessageService.get("M00732");
            default -> commonMessageService.get("M00733");
        };
    }

    /** AS-IS: case 1 텍스트입력 / case 2 텍스트입력-테두리없음 / default 이미지등록. */
    private String popupStyleLabel(String popupStyle) {
        return switch (asInt(popupStyle)) {
            case 1 -> commonMessageService.get("M00736");
            case 2 -> commonMessageService.get("M00736") + "-" + commonMessageService.get("M00737");
            default -> commonMessageService.get("M00738");
        };
    }

    private static int asInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;   // AS-IS는 parseInt가 터지지만, 값이 비어 있어도 화면은 떠야 한다
        }
    }

    /** AS-IS popupWrite(/opmanager/popup/write) - 신규 폼. hours는 00~23 시간 셀렉트. */
    @GetMapping("/popups/new")
    public String popupForm(Model model) {
        model.addAttribute("popup", new Popup());
        model.addAttribute("hours", hours());
        return "content/popup-form";
    }

    @GetMapping("/popups/{id}/edit")
    public String popupEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("popup", operationContentService.popup(id));
        model.addAttribute("hours", hours());
        return "content/popup-form";
    }

    /** AS-IS ShopUtils.getHours() - "00"~"23" 제로패딩. */
    private List<String> hours() {
        List<String> hours = new java.util.ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        return hours;
    }

    /** AS-IS는 저장 URL이 폼 URL과 같다({@code POST /opmanager/popup/write}). 목록 검색이
     *  {@code POST list}로 분리돼 있는데 TO-BE가 저장을 목록 URL(/popups)에 얹어
     *  "Ambiguous handler methods mapped for '/popups'"로 <b>검색·페이징이 전부 500</b>이었다. */
    @PostMapping("/popups/new")
    public String createPopup(@ModelAttribute Popup popup,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Model model) {
        try {
            operationContentService.createPopup(popup, storePopupImage(popup, imageFile));
            // AS-IS ViewUtils.redirect("/opmanager/popup/list", M00288) - 목록으로 돌아가며 안내를 띄운다.
            // (AS-IS도 검색조건·페이지를 들고 가지 않고 목록 1페이지로 간다 - 2026-10-06 확인)
            return redirectToPopupList("M00288");
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("hours", hours());
            return "content/popup-form";
        }
    }

    /** AS-IS {@code POST /opmanager/popup/edit/{popupId}} - 폼 URL과 같은 주소로 저장한다. */
    @PostMapping("/popups/{id}/edit")
    public String updatePopup(@PathVariable Integer id, @ModelAttribute Popup popup,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Model model) {
        try {
            operationContentService.updatePopup(id, popup, storePopupImage(popup, imageFile));
            return redirectToPopupList("M00289");   // AS-IS: 수정되었습니다.
        } catch (ContentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("popup", operationContentService.popup(id));
            model.addAttribute("hours", hours());
            return "content/popup-form";
        }
    }

    /** AS-IS insert/updatePopup은 <b>팝업형태가 '3'(이미지등록)일 때만</b> 올라온 파일을 저장한다
     *  ({@code popup.getPopupImageFile() != null && "3".equals(popup.getPopupStyle())}).
     *  텍스트입력으로 저장하면서 파일을 끼워 보내도 AS-IS는 그 파일을 쓰지 않는다. */
    private String storePopupImage(Popup popup, MultipartFile imageFile) {
        if (!"3".equals(popup.getPopupStyle())) {
            return null;
        }
        return popupImageStorageService.store(imageFile);
    }

    /** AS-IS deletePopup은 GET /opmanager/popup/delete/{id}로 들어온다(목록 JS가 location.replace). */
    @RequestMapping(value = "/popups/{id}/delete", method = { RequestMethod.GET, RequestMethod.POST })
    public String deletePopup(@PathVariable Integer id) {
        operationContentService.deletePopup(id);
        return redirectToPopupList("M00205");   // AS-IS: 삭제 되었습니다.
    }

    /** AS-IS {@code ViewUtils.redirect("/opmanager/popup/list", 문구)} - 메시지는 flash로
     *  넘어가고 <b>URL에는 안 붙는다</b>(AS-IS 바이트코드 확인: FlashMapUtils.setMessage). */
    private String redirectToPopupList(String messageCode) {
        return flashRedirect.to("/popups", commonMessageService.get(messageCode));
    }

    /** AS-IS deleteListData(/opmanager/popup/delete-list) - 목록 선택삭제. op.common.js의
     *  Common.updateListData가 체크박스 name="id"를 모아 ajax POST하고 {isSuccess}를 본다. */
    @PostMapping("/popups/delete-list")
    @ResponseBody
    public Map<String, Object> deletePopupList(@RequestParam(value = "id", required = false) List<Integer> ids) {
        operationContentService.deletePopups(ids);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        return result;
    }

    /** AS-IS deletePopupImage(/opmanager/popup/delete-item-image) - 등록화면의 이미지 삭제 아이콘. */
    @PostMapping("/popups/delete-item-image")
    @ResponseBody
    public Map<String, Object> deletePopupImage(@RequestParam("popupId") Integer popupId) {
        operationContentService.deletePopupImage(popupId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        return result;
    }
}
