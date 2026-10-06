package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.HonorUserAdminClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.web.support.HonorUserSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 기부혜택증 관리 (메뉴 19101 설정관리 / 19102 설정 / 19103 열람현황) - AS-IS
 * {@code saleson.shop.lclgvHnrUser.LclgvHnrUserManagerController}
 * ({@code /opmanager/lclgvHnrUser}) 재현.
 *
 * <p>기부혜택증(AS-IS 명칭 "명예시도민증")은 일정 금액 이상 기부자에게 지자체가 발급하는 전자
 * 증서다. 설정·이미지설명·열람이력이 모두 donation 소유라 화면만 admin에 두고 데이터는
 * {@link HonorUserAdminClient}로 다룬다(지자체관리 4401과 같은 구성).
 *
 * <p><b>구조갭 해소</b>: TO-BE는 19101·19102 두 메뉴가 같은 URL({@code /admin/honor-users})을
 * 가리켰다. AS-IS는 19101이 <b>목록</b>({@code /lclgvHnrUserMng/list}), 19102가 <b>설정 폼</b>
 * ({@code /lclgvHnrUserMng/form})인 별개 화면이다 - 4402·4501과 같은 유형이라 분리했다.
 *
 * <p><b>권한 분기</b>(AS-IS): 시스템·행안부(ROLE_ADMIN_1~4)는 목록을 보고, <b>지자체 담당자
 * (5·6)는 목록 대신 자기 지자체 설정 폼으로 리다이렉트</b>된다. 열람현황도 지자체 담당자는
 * 자기 지자체로 강제 스코프되고 <b>지자체 검색칸 자체가 숨는다</b>.
 *
 * <p>AS-IS 동작 그대로: 목록·열람현황은 <b>진입(GET)에서 조회하지 않는다</b>(검색해야 조회).
 * 열람현황의 기간은 비면 오늘로 채운다.
 *
 * <p><b>발견한 결함 1건 - 고쳤다</b>: 예전 TO-BE 화면은 admin 스키마의 빈
 * {@code op_honor_view_hist}를 읽어 열람현황이 <b>항상 0건</b>이었다. 실제 열람이력은
 * storefront 기부혜택증 열람 시 donation이 {@code donation.op_honor_view_hist}에 쌓는다
 * (실측 5건). donation API로 바꿨다.
 */
@Controller
@RequestMapping("/admin/honor-users")
@RequiredArgsConstructor
public class LclgvHnrUserAdminController {

    /** 발급기준 구분 공통코드 - 기준1 전년도 / 기준2 최근1년 / 기준3 회계연도. */
    private static final String CODE_TYPE_SLCTN_SE = "HNR_USER_SLCTN_SE_CD";

    private final HonorUserAdminClient honorUserAdminClient;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final MemberAdminClient memberAdminClient;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;

    /** 목록 한 행 - AS-IS 6컬럼(골드·실버 금액 컬럼은 AS-IS에서 주석처리). */
    public record Row(int rowNumber, String lclgvCd, String upperLocgovNm, String lclgvCdNm,
                      Integer brnzGrdDntnAmt, String hnrUserSlctnSeCdNm, String useYn) {

        public String getBrnzGrdDntnAmtText() {
            return brnzGrdDntnAmt == null ? "" : String.format("%,d", brnzGrdDntnAmt);
        }
    }

    /** 열람현황 한 행 - AS-IS는 (열람일자, 회원, 지자체) 묶음의 열람횟수다. */
    public record ViewHistRow(int rowNumber, String viewYm, String upperLocgovNm, String lclgvCdNm,
                              String userName, long viewCnt) {
    }

    /* ==================== 19101 설정 목록 ==================== */

    /** AS-IS GET /lclgvHnrUserMng/list - 조회하지 않고 빈 목록. 지자체 담당자는 설정 폼으로. */
    @GetMapping
    public String list(@ModelAttribute("searchParam") HonorUserSearchParam searchParam,
                       HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String redirect = listGate(viewer);
        if (redirect != null) {
            return redirect;
        }
        searchParam.applyListDefaults();
        fillList(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request));
        return "honor-user-admin/list";
    }

    /** AS-IS POST /lclgvHnrUserMng/list - 실제 검색. */
    @PostMapping
    public String search(@ModelAttribute("searchParam") HonorUserSearchParam searchParam,
                         HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String redirect = listGate(viewer);
        if (redirect != null) {
            return redirect;
        }
        searchParam.applyListDefaults();

        Map<String, String> slctnLabels = commonCodeService.labelsOf(CODE_TYPE_SLCTN_SE);
        List<HonorUserAdminClient.SettingRow> all =
                honorUserAdminClient.list(searchParam.getUpperLocgovCode(), searchParam.getLclgvCd());

        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);

        // AS-IS는 ROW_NUMBER() OVER(ORDER BY LCLGV_CD)로 1부터 올라가는 번호를 쓴다
        int start = pagination.getStartRow();
        int end = Math.min(start + pagination.getItemsPerPage(), all.size());
        List<Row> numbered = new java.util.ArrayList<>();
        for (int i = start; i < end; i++) {
            HonorUserAdminClient.SettingRow s = all.get(i);
            numbered.add(new Row(i + 1, s.lclgvCd(), s.upperLocgovNm(), s.lclgvCdNm(),
                    s.brnzGrdDntnAmt(), slctnLabels.get(s.hnrUserSlctnSeCd()), s.useYn()));
        }

        fillList(model, numbered, all.size(), pagination);
        return "honor-user-admin/list";
    }

    /* ==================== 19102 설정 폼 ==================== */

    /**
     * AS-IS GET /lclgvHnrUserMng/form/{lclgvCd} (+ 코드 없는 /form).
     * 코드가 없으면 지자체 담당자는 자기 지자체로, 그 외 권한은 홈으로 돌려보낸다(AS-IS 동일).
     */
    @GetMapping({"/form", "/form/{lclgvCd}"})
    public String form(@PathVariable(required = false) String lclgvCd, HttpSession session, Model model) {
        Manager viewer = manager(session);
        if (lclgvCd == null || lclgvCd.isBlank()) {
            if (isLocgovManager(viewer) && viewer.getLocgovCode() != null) {
                lclgvCd = viewer.getLocgovCode();
            } else {
                return "redirect:/admin";
            }
        }
        // 지자체 담당자가 남의 지자체를 열면 자기 것으로 되돌린다
        if (isLocgovManager(viewer) && viewer.getLocgovCode() != null
                && !lclgvCd.equals(viewer.getLocgovCode())) {
            return "redirect:/admin/honor-users/form/" + viewer.getLocgovCode();
        }

        HonorUserAdminClient.Detail detail = honorUserAdminClient.detail(lclgvCd);
        model.addAttribute("lclgvCd", lclgvCd);
        model.addAttribute("lclgvCdNm", locgovFullName(lclgvCd));
        model.addAttribute("setting", detail.setting());
        model.addAttribute("imageExplains", detail.imageExplains());
        model.addAttribute("slctnSeCodes", commonCodeService.listByType(CODE_TYPE_SLCTN_SE));
        model.addAttribute("listVisible", !isLocgovManager(viewer));
        return "honor-user-admin/form";
    }

    /** AS-IS POST /lclgvHnrUserMng/form/update - 저장 후 메시지와 함께 돌아간다. */
    @PostMapping("/form/update")
    public String update(@RequestParam String lclgvCd,
                         @RequestParam(required = false) Integer brnzGrdDntnAmt,
                         @RequestParam(required = false) String hnrUserStngTtl,
                         @RequestParam(required = false) String hnrUserRwrd,
                         @RequestParam(required = false) String hnrUserSlctnSeCd,
                         @RequestParam(required = false) String useYn,
                         @RequestParam(required = false) List<String> imageExplains,
                         @RequestParam(name = "prjImageFiles", required = false) List<MultipartFile> images,
                         HttpSession session) {
        Manager viewer = manager(session);
        if (isLocgovManager(viewer) && viewer.getLocgovCode() != null
                && !lclgvCd.equals(viewer.getLocgovCode())) {
            return "redirect:/admin/honor-users/form/" + viewer.getLocgovCode();
        }

        String message;
        try {
            honorUserAdminClient.save(lclgvCd, brnzGrdDntnAmt, hnrUserStngTtl, hnrUserRwrd,
                    hnrUserSlctnSeCd, useYn, imageExplains, images,
                    viewer == null ? null : viewer.getUserId());
            message = "저장에 성공했습니다.";
        } catch (ManagerException e) {
            message = "저장에 실패했습니다.";
        }

        // AS-IS: 지자체 담당자는 자기 설정 폼으로, 시스템·행안부는 목록으로 돌아간다
        if (isLocgovManager(viewer)) {
            return flashRedirect.to("/admin/honor-users/form/" + lclgvCd, message);
        }
        return flashRedirect.to("/admin/honor-users", message);
    }

    /** AS-IS POST /lclgvHnrUserMng/form/deleteItemFile - 대표이미지 삭제(ajax, 건수 반환). */
    @PostMapping("/form/deleteItemFile")
    @ResponseBody
    public int deleteItemFile(@RequestParam String lclgvCd) {
        return honorUserAdminClient.deleteMainImage(lclgvCd);
    }

    /** 대표이미지 - AS-IS는 /upload/lclgvHnrUserMng/{코드}/{파일명}을 직접 참조한다. */
    @GetMapping("/form/{lclgvCd}/main-image")
    @ResponseBody
    public ResponseEntity<byte[]> mainImage(@PathVariable String lclgvCd) {
        byte[] bytes = honorUserAdminClient.mainImageBytes(lclgvCd);
        return bytes != null ? ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes)
                : ResponseEntity.notFound().build();
    }

    /* ==================== 19103 열람현황 ==================== */

    /** AS-IS GET /lclgvHnrUserViewHist/list - 조회하지 않고 빈 목록(기간은 오늘로 채운다). */
    @GetMapping("/view-history")
    public String viewHistory(@ModelAttribute("searchParam") HonorUserSearchParam searchParam,
                              HttpServletRequest request, HttpSession session, Model model) {
        searchParam.applyViewHistDefaults();
        fillViewHist(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request),
                manager(session));
        return "honor-user-admin/view-history";
    }

    /** AS-IS POST /lclgvHnrUserViewHist/list - 실제 검색. */
    @PostMapping("/view-history")
    public String searchViewHistory(@ModelAttribute("searchParam") HonorUserSearchParam searchParam,
                                    HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = manager(session);
        searchParam.applyViewHistDefaults();

        List<ViewHistRow> all = loadViewHist(searchParam, viewer);
        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);

        fillViewHist(model, all.stream().skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage()).toList(), all.size(), pagination, viewer);
        return "honor-user-admin/view-history";
    }

    /** AS-IS GET /lclgvHnrUserViewHist/list/excel - 엑셀 다운로드(TO-BE는 CSV). */
    @GetMapping("/view-history/export")
    public void exportViewHistory(@ModelAttribute("searchParam") HonorUserSearchParam searchParam,
                                  HttpSession session, HttpServletResponse response) throws IOException {
        Manager viewer = manager(session);
        searchParam.applyViewHistDefaults();
        List<ViewHistRow> rows = loadViewHist(searchParam, viewer);

        StringBuilder csv = new StringBuilder("﻿");
        csv.append("No.,열람일자,시도,지자체,사용자명,열람횟수\n");
        for (ViewHistRow row : rows) {
            csv.append(row.rowNumber()).append(',')
                    .append(csvEscape(row.viewYm())).append(',')
                    .append(csvEscape(row.upperLocgovNm())).append(',')
                    .append(csvEscape(row.lclgvCdNm())).append(',')
                    .append(csvEscape(row.userName())).append(',')
                    .append(row.viewCnt()).append('\n');
        }
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"honor-view-history.csv\"; filename*=UTF-8''"
                        + java.net.URLEncoder.encode("기부혜택증 열람현황 엑셀.csv", StandardCharsets.UTF_8));
        response.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /* ==================== 내부 ==================== */

    /**
     * 열람현황 조립. donation이 (열람일자·회원·지자체) 묶음을 주고 <b>사용자명은 member에서</b>
     * 채운다. AS-IS가 OP_USER를 INNER JOIN하므로 <b>회원을 못 찾은 행은 버린다</b>.
     * 정렬도 AS-IS대로 지자체코드 → 사용자명 순이다.
     */
    private List<ViewHistRow> loadViewHist(HonorUserSearchParam searchParam, Manager viewer) {
        // 지자체 담당자는 AS-IS대로 자기 지자체로 강제 스코프된다
        String lclgvCd = isLocgovManager(viewer) ? viewer.getLocgovCode() : searchParam.getLclgvCd();
        String upperLocgovCode = isLocgovManager(viewer) ? null : searchParam.getUpperLocgovCode();

        List<HonorUserAdminClient.ViewHistRow> rows = honorUserAdminClient.viewHist(
                searchParam.getStartDate(), searchParam.getEndDate(), upperLocgovCode, lclgvCd);

        List<Long> userIds = rows.stream().map(HonorUserAdminClient.ViewHistRow::userId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, String> names = memberAdminClient.userNames(userIds);

        List<ViewHistRow> result = new java.util.ArrayList<>();
        List<HonorUserAdminClient.ViewHistRow> joined = rows.stream()
                .filter(r -> r.userId() != null && names.containsKey(r.userId()))
                .filter(r -> searchParam.matchesUserName(names.get(r.userId())))
                .sorted(Comparator.comparing((HonorUserAdminClient.ViewHistRow r) ->
                                r.lclgvCd() == null ? "" : r.lclgvCd())
                        .thenComparing(r -> names.getOrDefault(r.userId(), "")))
                .toList();
        for (int i = 0; i < joined.size(); i++) {
            HonorUserAdminClient.ViewHistRow r = joined.get(i);
            result.add(new ViewHistRow(i + 1, r.viewYm(), r.upperLocgovNm(), r.lclgvCdNm(),
                    names.get(r.userId()), r.viewCnt()));
        }
        return result;
    }

    /** AS-IS 목록 진입 권한분기 - 지자체 담당자는 자기 설정 폼으로. */
    private String listGate(Manager viewer) {
        if (!isLocgovManager(viewer)) {
            return null;
        }
        String code = viewer.getLocgovCode();
        if (code == null || code.isBlank()) {
            return "redirect:/admin";
        }
        return "redirect:/admin/honor-users/form/" + code;
    }

    private static boolean isLocgovManager(Manager viewer) {
        return viewer != null && ("ROLE_ADMIN_5".equals(viewer.getAuthority())
                || "ROLE_ADMIN_6".equals(viewer.getAuthority()));
    }

    private void fillList(Model model, List<Row> list, int count, Pagination pagination) {
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        model.addAttribute("provinces", provinces());
        // AS-IS는 시군구 목록을 ajax로 받지만 TO-BE admin은 지자체 목록을 이미 갖고 있어
        // 화면에 실어 보내고 JS가 시도로 걸러 쓴다(4401 등록화면과 같은 방식).
        model.addAttribute("locgovs", locgovClient.allLocgovs());
    }

    private void fillViewHist(Model model, List<ViewHistRow> list, int count, Pagination pagination,
                              Manager viewer) {
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        model.addAttribute("provinces", provinces());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        // AS-IS는 지자체 담당자에게 지자체 검색칸을 아예 보여주지 않는다
        model.addAttribute("locgovSearchVisible", !isLocgovManager(viewer));
    }

    /** 시도 목록 - AS-IS 공통코드 WDR 대신 지자체 목록에서 추린다(4402·4401과 같은 처리). */
    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            if (l.upperLocgovCode() != null) {
                map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
            }
        }
        return map;
    }

    private String locgovFullName(String lclgvCd) {
        return locgovClient.allLocgovs().stream()
                .filter(l -> lclgvCd.equals(l.locgovCode()))
                .map(l -> (l.upperLocgovNm() == null ? "" : l.upperLocgovNm()) + " "
                        + (l.locgovNm() == null ? "" : l.locgovNm()))
                .findFirst().orElse(lclgvCd);
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")
                ? "\"" + escaped + "\"" : escaped;
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
