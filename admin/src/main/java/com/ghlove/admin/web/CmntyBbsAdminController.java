package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CmntyBbsAdminService;
import com.ghlove.admin.service.CmntyException;
import com.ghlove.admin.service.CmntyText;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.web.support.CmntyBbsSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 소통방 (메뉴 11401) - AS-IS {@code CommunityManagerController}의
 * {@code bbs/*}·{@code cmnt/*} 11개 엔드포인트 재현
 * ({@code /opmanager/community/bbs/list} 외).
 *
 * <p>AS-IS 경로 모양을 그대로 쓴다 - 게시글은 {@code /community/bbs/*}, 댓글은
 * {@code /community/cmnt/*}다(AS-IS도 댓글을 게시판 하위에 두지 않았다. 소통방 전용이라
 * 다른 게시판 댓글은 각자 {@code srBbs/cmnt/*} 식으로 따로 있다).
 *
 * <p><b>AS-IS 그대로인 점</b>:
 * <ul>
 *   <li>진입(GET)에서도 조회한다 - 검색해야 보이는 다른 운영화면들과 달리 소통방은
 *       들어가면 바로 목록이 나온다(AS-IS {@code list}와 {@code searchList}가 같은 코드다).</li>
 *   <li>등록·수정·삭제·댓글은 전부 ajax(JSON)다 - 화면은 {@code /content/modules/cmnty/bbs.js},
 *       {@code cmnt.js}가 움직이고 응답은 {@code isSuccess}/{@code data}를 본다.</li>
 *   <li>글이 없거나 비밀글에 막히면 <b>목록으로 돌려보내며 안내문구를 띄운다</b> -
 *       문구 3종(존재하지 않음 / 비공개 처리된 글 / 비밀글)을 AS-IS에서 그대로 가져왔다.</li>
 *   <li>상단공지 체크박스는 시스템·행안부 관리자(1~4)에게만 보인다({@code role} 모델값).</li>
 * </ul>
 *
 * <p>검색칸의 시·도 목록은 AS-IS와 같이 공통코드 {@code WDR}을 쓰고, 시·군·구는 AS-IS가
 * ajax로 받던 것을 지자체 목록을 화면에 실어 보내 JS가 걸러 쓰는 방식으로 바꿨다
 * (4401·19101과 같은 처리, 보이는 동작은 같다).
 */
@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CmntyBbsAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String LIST_URL = "/community/bbs/list";

    private final CmntyBbsAdminService cmntyBbsAdminService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;

    /** op_menu 11401의 등록 URL이 {@code /community/bbs}라서 목록으로 보낸다. */
    @GetMapping("/bbs")
    public String bbsRoot() {
        return "redirect:" + LIST_URL;
    }

    /** AS-IS GET bbs/list - 진입에서도 조회한다. */
    @GetMapping("/bbs/list")
    public String list(@ModelAttribute("searchParam") CmntyBbsSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model);
        return "community/freeboard/list";
    }

    /** AS-IS POST bbs/list - GET과 같은 코드다. */
    @PostMapping("/bbs/list")
    public String searchList(@ModelAttribute("searchParam") CmntyBbsSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model);
        return "community/freeboard/list";
    }

    /** AS-IS GET bbs/form. */
    @GetMapping("/bbs/form")
    public String createForm(HttpSession session, Model model) {
        model.addAttribute("role", cmntyBbsAdminService.screenRole(viewer(session)));
        return "community/freeboard/form";
    }

    /** AS-IS POST bbs/add. */
    @PostMapping("/bbs/add")
    @ResponseBody
    public Map<String, Object> add(@RequestParam String bbsTtl,
                                   @RequestParam String bbsCn,
                                   @RequestParam(defaultValue = "N") String noticeYn,
                                   @RequestParam(defaultValue = "N") String isSecret,
                                   HttpSession session) {
        try {
            cmntyBbsAdminService.addBbs(bbsTtl, bbsCn, noticeYn, isSecret, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("등록에 성공하였습니다.");
    }

    /** AS-IS GET bbs/detail/{bbsId} - 조회수를 올리고 댓글까지 같이 내려준다. */
    @GetMapping("/bbs/detail/{bbsId}")
    public String detail(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        CmntyBbsAdminService.BbsDetail detail = cmntyBbsAdminService.detailBbs(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비공개 처리된 글은 작성자만 확인 가능 합니다.");
        }

        List<CmntyBbsAdminService.CmntRow> comments = cmntyBbsAdminService.bbsCmntList(bbsId);
        model.addAttribute("detail", detail);
        // AS-IS op:nl2br - 본문은 에디터 HTML이라 그대로 렌더링하고 줄바꿈만 <br>로 바꾼다
        model.addAttribute("bbsCnHtml", CmntyText.nl2br(detail.bbsCn()));
        model.addAttribute("bbsCmntList", comments);
        model.addAttribute("bbsCmntListCnt", comments.size());
        model.addAttribute("role", cmntyBbsAdminService.screenRole(viewer));
        model.addAttribute("userId", viewer == null ? null : viewer.getUserId());
        return "community/freeboard/detail";
    }

    /** AS-IS GET bbs/edit/{bbsId} - 상세와 같은 조회를 쓰므로 <b>조회수가 또 올라간다</b>. */
    @GetMapping("/bbs/edit/{bbsId}")
    public String editForm(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        CmntyBbsAdminService.BbsDetail detail = cmntyBbsAdminService.detailBbs(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비밀글은 본인만 확인 가능 합니다.");
        }

        model.addAttribute("detail", detail);
        model.addAttribute("role", cmntyBbsAdminService.screenRole(viewer));
        model.addAttribute("userId", viewer == null ? null : viewer.getUserId());
        return "community/freeboard/edit";
    }

    /** AS-IS POST bbs/update. */
    @PostMapping("/bbs/update")
    @ResponseBody
    public Map<String, Object> update(@RequestParam long bbsId,
                                      @RequestParam String bbsTtl,
                                      @RequestParam String bbsCn,
                                      @RequestParam(defaultValue = "N") String noticeYn,
                                      @RequestParam(defaultValue = "N") String isSecret,
                                      HttpSession session) {
        try {
            cmntyBbsAdminService.updateBbs(bbsId, bbsTtl, bbsCn, noticeYn, isSecret, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("수정에 성공하였습니다.");
    }

    /** AS-IS POST bbs/delete/{bbsId}. */
    @PostMapping("/bbs/delete/{bbsId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable long bbsId, HttpSession session) {
        try {
            cmntyBbsAdminService.deleteBbs(bbsId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("삭제에 성공하였습니다.");
    }

    /** AS-IS POST cmnt/add. */
    @PostMapping("/cmnt/add")
    @ResponseBody
    public Map<String, Object> addCmnt(@RequestParam long bbsId, @RequestParam String cmntCn,
                                       HttpSession session) {
        try {
            cmntyBbsAdminService.addCmnt(bbsId, cmntCn, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("등록에 성공하였습니다.");
    }

    /** AS-IS POST cmnt/update. */
    @PostMapping("/cmnt/update")
    @ResponseBody
    public Map<String, Object> updateCmnt(@RequestParam long cmntId, @RequestParam String cmntCn,
                                          HttpSession session) {
        try {
            cmntyBbsAdminService.updateCmnt(cmntId, cmntCn, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("수정에 성공하였습니다.");
    }

    /** AS-IS POST cmnt/delete/{cmntId}. */
    @PostMapping("/cmnt/delete/{cmntId}")
    @ResponseBody
    public Map<String, Object> deleteCmnt(@PathVariable long cmntId, HttpSession session) {
        try {
            cmntyBbsAdminService.deleteCmnt(cmntId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("삭제에 성공하였습니다.");
    }

    private void fillList(CmntyBbsSearchParam searchParam, HttpServletRequest request, Model model) {
        searchParam.applyDefaults();

        int count = cmntyBbsAdminService.countBbs(searchParam.getLocgovCode(), searchParam.getStartDt(),
                searchParam.getEndDt(), searchParam.getSearchRole(), searchParam.getWhere(),
                searchParam.getQuery());
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<CmntyBbsAdminService.BbsRow> list = cmntyBbsAdminService.listBbs(searchParam.getLocgovCode(),
                searchParam.getStartDt(), searchParam.getEndDt(), searchParam.getSearchRole(),
                searchParam.getWhere(), searchParam.getQuery(), pagination.getStartRow(),
                pagination.getItemsPerPage());

        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        // AS-IS가 화면에 같이 내려주는 날짜 버튼 기준값들
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
        // AS-IS CodeUtils.getCodeList("WDR") - 시·도 목록
        model.addAttribute("wdr", wdrOptions());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
    }

    /** 공통코드 WDR을 AS-IS JSP가 쓰는 모양(id·label)으로 내려준다. */
    private List<Map<String, String>> wdrOptions() {
        List<Map<String, String>> options = new java.util.ArrayList<>();
        commonCodeService.labelsOf("WDR").forEach((id, label) -> {
            Map<String, String> option = new LinkedHashMap<>();
            option.put("id", id);
            option.put("label", label);
            options.add(option);
        });
        return options;
    }

    /** AS-IS ViewUtils.redirect(url, message) - 안내문구를 띄우고 목록으로 보낸다. */
    private String redirectToList(String message) {
        return flashRedirect.to(LIST_URL, message);
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS JsonViewUtils.success(msg) - 화면 JS가 {@code response.data}를 alert한다. */
    private static Map<String, Object> success(String message) {
        return Map.of("isSuccess", true, "data", message);
    }

    /** AS-IS JsonViewUtils.failure(msg) - Common.responseHandler가 errorMessage를 alert한다. */
    private static Map<String, Object> failure(String message) {
        return Map.of("isSuccess", false, "errorMessage", message == null ? "" : message);
    }
}
