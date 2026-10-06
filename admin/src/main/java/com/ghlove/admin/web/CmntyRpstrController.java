package com.ghlove.admin.web;

import com.ghlove.admin.domain.CmntyFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CmntyException;
import com.ghlove.admin.service.CmntyFileStorageService;
import com.ghlove.admin.service.CmntyRpstrAdminService;
import com.ghlove.admin.service.CmntyText;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.web.support.CmntyRpstrSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 자료실 (메뉴 11402) - AS-IS {@code LocGovDataBoardManagerController}
 * ({@code /opmanager/community/databoard/}) 이식.
 *
 * <p><b>AS-IS 그대로인 점</b>:
 * <ul>
 *   <li>목록은 GET·POST 둘 다 같은 코드이고 <b>진입에서도 조회</b>한다.</li>
 *   <li>등록·수정은 multipart 폼 전송이고 끝나면 목록으로 리다이렉트하며 안내문구를 띄운다
 *       ("등록되었습니다." / M00289 "수정되었습니다.").</li>
 *   <li><b>수정 화면이 등록 화면과 같은 템플릿</b>이다(AS-IS edit도 form.jsp를 돌려준다).</li>
 *   <li>삭제·첨부삭제는 ajax이고 삭제 응답에는 <b>메시지가 없다</b>(JsonViewUtils.success()).</li>
 *   <li>첨부 파라미터 이름이 {@code detailImageFiles[]}다(다른 게시판은 {@code detailImageFiles}).</li>
 *   <li>등록 화면의 role 판정에 <b>ROLE_ADMIN_10이 포함</b>된다(SR·FAQ는 1~6).</li>
 * </ul>
 *
 * <p><b>AS-IS 죽은 엔드포인트 2개는 이식하지 않았다</b>(기록만):
 * <ul>
 *   <li>{@code POST /deleteDataboard}(체크박스 일괄삭제) - 목록 화면에 <b>체크박스 컬럼이 없어</b>
 *       호출할 수 없는 죽은 UI이고, 더구나 이 엔드포인트는 {@code LocgovDataBoardService}를 거쳐
 *       <b>전혀 다른 표({@code op_community_databoard})</b>를 지운다. 화면이 보여주는
 *       {@code g_cmnty_rpstr}와 무관해서, 눌렀더라도 아무 행도 지워지지 않고 오류만 났을 것이다.</li>
 *   <li>{@code POST search-date} - 날짜만 계산해 모델에 담고 <b>JSON에는 아무 값도 싣지 않는다</b>.
 *       화면에서 호출하는 곳도 없다(list.jsp의 {@code search-date}는 CSS 클래스명이다).</li>
 * </ul>
 * 같은 이유로 list.jsp의 {@code deleteNotice}·{@code deleteCheckDataboard}·{@code checkedEventSet}
 * 함수도 옮기지 않았다 - 호출할 요소가 렌더링되지 않는다.
 *
 * <p><b>★ 메뉴 5107 고객센터 자료실({@code /admin/data-board})과 다른 화면이다</b> -
 * 표도 대상도 다르다({@link com.ghlove.admin.service.CmntyRpstrAdminService} 주석 참고).
 */
@Controller
@RequestMapping("/community/databoard")
@RequiredArgsConstructor
public class CmntyRpstrController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String LIST_URL = "/community/databoard/list";

    private final CmntyRpstrAdminService rpstrService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final CmntyFileStorageService fileStorageService;
    private final CommonCodeService commonCodeService;
    private final CommonMessageService commonMessageService;
    private final LocgovClient locgovClient;

    /** op_menu 11402의 등록 URL이 {@code /community/databoard}라서 목록으로 보낸다. */
    @GetMapping
    public String root() {
        return "redirect:" + LIST_URL;
    }

    /** AS-IS GET/POST databoard/list. */
    @RequestMapping("/list")
    public String list(@ModelAttribute("searchParam") CmntyRpstrSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        searchParam.applyDefaults();

        int count = rpstrService.count(searchParam.getLocgovCode(), searchParam.getStartDt(),
                searchParam.getEndDt(), searchParam.getSearchRole(), searchParam.getWhere(),
                searchParam.getQuery());
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<CmntyRpstrAdminService.RpstrRow> list = rpstrService.list(searchParam.getLocgovCode(),
                searchParam.getStartDt(), searchParam.getEndDt(), searchParam.getSearchRole(),
                searchParam.getWhere(), searchParam.getQuery(), pagination.getStartRow(),
                pagination.getItemsPerPage());

        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
        model.addAttribute("wdr", wdrOptions());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        return "community/databoard/list";
    }

    /** AS-IS GET databoard/create. */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        model.addAttribute("role", rpstrService.screenRole(viewer(session)));
        return "community/databoard/form";
    }

    /** AS-IS POST databoard/create - multipart 폼 전송. */
    @PostMapping("/create")
    public String create(@RequestParam String rpstrTtl,
                         @RequestParam String rpstrCn,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(value = "detailImageFiles[]", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            rpstrService.insert(rpstrTtl, rpstrCn, noticeYn, detailImageFiles, viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        return redirectToList("등록되었습니다.");
    }

    /** AS-IS GET databoard/detail/{rpstrId} - 조회수를 먼저 올린다. */
    @GetMapping("/detail/{rpstrId}")
    public String detail(@PathVariable long rpstrId, HttpSession session, Model model) {
        rpstrService.increaseInqCnt(rpstrId);

        CmntyRpstrAdminService.RpstrDetail detail = rpstrService.detail(rpstrId);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        Manager viewer = viewer(session);
        model.addAttribute("detail", detail);
        model.addAttribute("rpstrCnHtml", CmntyText.nl2br(detail.rpstrCn()));
        model.addAttribute("fileList", rpstrService.fileList(rpstrId));
        model.addAttribute("role", rpstrService.screenRole(viewer));
        model.addAttribute("userId", viewer == null ? null : viewer.getUserId());
        return "community/databoard/detail";
    }

    /** AS-IS GET databoard/edit/{rpstrId} - <b>등록 화면과 같은 템플릿</b>을 쓴다. */
    @GetMapping("/edit/{rpstrId}")
    public String editForm(@PathVariable long rpstrId, HttpSession session, Model model) {
        CmntyRpstrAdminService.RpstrDetail detail = rpstrService.detail(rpstrId);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        model.addAttribute("detail", detail);
        model.addAttribute("fileList", rpstrService.fileList(rpstrId));
        model.addAttribute("role", rpstrService.screenRole(viewer(session)));
        return "community/databoard/form";
    }

    /** AS-IS POST databoard/edit/{rpstrId} - 끝나면 M00289("수정되었습니다.")로 목록으로. */
    @PostMapping("/edit/{rpstrId}")
    public String update(@PathVariable long rpstrId,
                         @RequestParam String rpstrTtl,
                         @RequestParam String rpstrCn,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(value = "detailImageFiles[]", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            rpstrService.update(rpstrId, rpstrTtl, rpstrCn, noticeYn, detailImageFiles, viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        // AS-IS: MessageUtils.getMessage("M00289") = "수정되었습니다."
        return redirectToList(commonMessageService.get("M00289"));
    }

    /** AS-IS POST databoard/delete/{rpstrId} - 응답에 메시지가 없다(AS-IS 그대로). */
    @PostMapping("/delete/{rpstrId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable long rpstrId, HttpSession session) {
        try {
            rpstrService.delete(rpstrId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS POST databoard/delete-item-image. */
    @PostMapping("/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteFile(@RequestParam long fileId, HttpSession session) {
        try {
            rpstrService.deleteFile(fileId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS GET databoard/file-download/{fileId}.
     *
     * <p>AS-IS는 파일이 없으면 {@code ApiResponseEntity.error(SYSTEM_ERROR)}를 돌려준다
     * (SR·오프라인SR·FAQ는 alert 스크립트를 돌려준다 - 화면마다 다르다). 같은 성격으로
     * 500 + 오류 본문을 돌려준다.
     */
    @GetMapping("/file-download/{fileId}")
    public ResponseEntity<?> fileDownload(@PathVariable long fileId) throws IOException {
        CmntyFile file = rpstrService.file(fileId);
        Resource resource = new UrlResource(
                fileStorageService.resolve(file.getAtchFileNm(), CmntyRpstrAdminService.SUBDIR).toUri());
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("isSuccess", false, "errorMessage", "시스템 오류가 발생했습니다."));
        }
        String downloadName = file.getOrgnlAtchFileNm() != null
                ? file.getOrgnlAtchFileNm() : file.getAtchFileNm();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /** 공통코드 WDR을 AS-IS JSP가 쓰는 모양(id·label)으로 내려준다. */
    private List<Map<String, String>> wdrOptions() {
        List<Map<String, String>> options = new ArrayList<>();
        commonCodeService.labelsOf("WDR").forEach((id, label) -> {
            Map<String, String> option = new LinkedHashMap<>();
            option.put("id", id);
            option.put("label", label);
            options.add(option);
        });
        return options;
    }

    private String redirectToList(String message) {
        return flashRedirect.to(LIST_URL, message);
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static Map<String, Object> failure(String message) {
        return Map.of("isSuccess", false, "errorMessage", message == null ? "" : message);
    }
}
