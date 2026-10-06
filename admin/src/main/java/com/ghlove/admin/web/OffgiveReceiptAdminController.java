package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.service.OffgiveClient;
import com.ghlove.admin.web.support.OffgiveSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 기부금 접수관리 (메뉴 15101) - AS-IS saleson.shop.offgive.OffgiveManagerController의
 * {@code GET/POST /opmanager/offgive/list} + {@code /list/download-excel} 이식.
 *
 * <p>오프라인 창구(농협 지점·센터)에서 접수된 기부금 목록이다. AS-IS 11컬럼:
 * 접수번호(지자체)·기부상태·전자납부번호·수납일·기부지자체·이름·기부금액·지점/센터명·신고일·
 * 특정사업명.
 *
 * <p><b>권한별 role과 스코프</b>(AS-IS 그대로):
 * <ul>
 *   <li>ROLE_ADMIN_1~4 → {@code SYSTEM}: 전체 조회 + <b>소속지점 라디오 검색칸이 보인다</b></li>
 *   <li>ROLE_ADMIN_7 → {@code OFF_MAIN}: 자기 <b>지점코드</b>로 고정(지점명은 고정 안 함 =
 *       같은 은행의 모든 지점을 본다)</li>
 *   <li>ROLE_ADMIN_8 → {@code OFF_SUB}: 자기 <b>지점코드 + 지점명</b>으로 고정(자기 지점만)</li>
 *   <li>그 외(지자체 담당자 등) → {@code LOC}: 지점 조건 없음</li>
 * </ul>
 * role을 못 정하면 AS-IS는 "권한이 없습니다."로 막는다.
 *
 * <p>AS-IS 동작 그대로: <b>진입(GET)에서는 조회하지 않는다</b>(검색해야 조회). 신청일 범위는
 * 비면 오늘로 채운다. 이름은 <b>마스킹</b>해서 보여준다(첫 글자 + ' * ' + 세 번째 글자부터).
 *
 * <p><b>미이식(사용자 결정 필요)</b>: AS-IS {@code ROLE_ADMIN_11}({@code OFF_CENTER}, 오프라인
 * 센터)은 TO-BE OP_ROLE에 없다(실측 ROLE_ADMIN_1~8까지만). 권한을 새로 만드는 일이라
 * 분기만 자리로 남기고 역할 자체는 추가하지 않았다 - 같은 메뉴의 기탁서 등록(15102)과 함께
 * 결정이 필요하다.
 */
@Controller
@RequestMapping("/offgive/list")
@RequiredArgsConstructor
public class OffgiveReceiptAdminController {

    /** 소속지점 공통코드 - 011 농협은행 / 012 농축협 / 035 제주은행. */
    private static final String CODE_TYPE_OFF_BANK = "OFF_BANK_LIST";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OffgiveClient offgiveClient;
    private final MemberAdminClient memberAdminClient;
    private final CommonCodeService commonCodeService;

    /** AS-IS 목록 한 행 - 코드라벨·마스킹까지 적용한 상태. */
    public record Row(int no, String cntrSn, String cntrSttusNm, String elctrnPayNo, String sttemntPayDe,
                      String locgovText, String maskedUserName, BigDecimal cntrAmt, String bankText,
                      String frstRegistPnttm, String prjSubject) {

        public String getCntrAmtText() {
            return cntrAmt == null ? "" : String.format("%,d", cntrAmt.longValue());
        }
    }

    /** AS-IS GET /list - 권한 확인 후, 조회하지 않고 빈 목록. */
    @GetMapping
    public String list(@ModelAttribute("searchParam") OffgiveSearchParam searchParam,
                       HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String role = roleOf(viewer);
        if (role == null) {
            model.addAttribute("errorMessage", "권한이 없습니다.");
            return "offgive/list";
        }
        searchParam.applyDefaults();
        applyScope(searchParam, viewer, role);
        fill(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request),
                role, viewer);
        return "offgive/list";
    }

    /** AS-IS POST /list - 실제 검색. */
    @PostMapping
    public String search(@ModelAttribute("searchParam") OffgiveSearchParam searchParam,
                         HttpServletRequest request, HttpSession session, Model model) {
        Manager viewer = manager(session);
        String role = roleOf(viewer);
        if (role == null) {
            model.addAttribute("errorMessage", "권한이 없습니다.");
            return "offgive/list";
        }
        searchParam.applyDefaults();
        applyScope(searchParam, viewer, role);

        List<Row> all = load(searchParam);
        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);

        fill(model, all.stream().skip(pagination.getStartRow()).limit(pagination.getItemsPerPage()).toList(),
                all.size(), pagination, role, viewer);
        return "offgive/list";
    }

    /** AS-IS GET /list/download-excel - 파일명 "오프라인기부금접수현황목록_{일시}". TO-BE는 CSV. */
    @GetMapping("/download-excel")
    public void downloadExcel(@ModelAttribute("searchParam") OffgiveSearchParam searchParam,
                              HttpSession session, HttpServletResponse response) throws IOException {
        Manager viewer = manager(session);
        String role = roleOf(viewer);
        if (role == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        searchParam.applyDefaults();
        applyScope(searchParam, viewer, role);

        StringBuilder csv = new StringBuilder("﻿");
        csv.append("No.,접수번호(지자체),기부상태,전자납부번호,수납일,기부 지자체,이름,기부 금액,지점/센터명,신고일,특정사업에 기부하기 사업명\n");
        for (Row row : load(searchParam)) {
            csv.append(row.no()).append(',')
                    .append(csvEscape(row.cntrSn())).append(',')
                    .append(csvEscape(row.cntrSttusNm())).append(',')
                    .append(csvEscape(row.elctrnPayNo())).append(',')
                    .append(csvEscape(row.sttemntPayDe())).append(',')
                    .append(csvEscape(row.locgovText())).append(',')
                    .append(csvEscape(row.maskedUserName())).append(',')
                    .append(row.cntrAmt() == null ? "" : row.cntrAmt().toPlainString()).append(',')
                    .append(csvEscape(row.bankText())).append(',')
                    .append(csvEscape(row.frstRegistPnttm())).append(',')
                    .append(csvEscape(row.prjSubject())).append('\n');
        }

        String fileName = "오프라인기부금접수현황목록_"
                + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"offgive-receipts.csv\"; filename*=UTF-8''"
                + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /* ==================== 내부 ==================== */

    /** 검색 + 이름 채우기 + 번호 매기기. */
    private List<Row> load(OffgiveSearchParam searchParam) {
        List<OffgiveClient.AdminRow> rows = offgiveClient.adminSearch(
                searchParam.getShKeyword(), searchParam.getShText(),
                searchParam.getShFrstRegistPnttmStart(), searchParam.getShFrstRegistPnttmEnd(),
                searchParam.getShCntrAmtStart(), searchParam.getShCntrAmtEnd(),
                searchParam.getShCntrSttusCode(), searchParam.getShRceptBankCode(),
                searchParam.getShRceptBankNm());

        List<Long> userIds = rows.stream().map(OffgiveClient.AdminRow::userId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, String> names = memberAdminClient.userNames(userIds);
        Map<String, String> bankLabels = commonCodeService.labelsOf(CODE_TYPE_OFF_BANK);

        List<Row> result = new java.util.ArrayList<>();
        int no = rows.size();
        for (OffgiveClient.AdminRow r : rows) {
            result.add(new Row(no--, r.cntrSn(), statusName(r.cntrSttusCode()), r.elctrnPayNo(),
                    formatDate(r.sttemntPayDe()), locgovText(r.upperLocgovNm(), r.locgovNm()),
                    maskName(names.get(r.userId())), r.cntrAmt(),
                    bankText(bankLabels.get(r.rceptBankCode()), r.rceptBankNm()),
                    formatDateTime(r.frstRegistPnttm()), r.prjSubject()));
        }
        return result;
    }

    /**
     * AS-IS 권한 role 판정. ROLE_ADMIN_11(OFF_CENTER)은 TO-BE OP_ROLE에 없어 분기만 남겼다.
     *
     * @return SYSTEM / OFF_MAIN / OFF_SUB / OFF_CENTER / LOC, 권한이 없으면 null
     */
    private static String roleOf(Manager viewer) {
        if (viewer == null || viewer.getAuthority() == null
                || !viewer.getAuthority().startsWith("ROLE_ADMIN_")) {
            return null;
        }
        return switch (viewer.getAuthority()) {
            case "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4" -> "SYSTEM";
            case "ROLE_ADMIN_7" -> "OFF_MAIN";
            case "ROLE_ADMIN_8" -> "OFF_SUB";
            // AS-IS ROLE_ADMIN_11 - TO-BE에는 아직 없는 권한(오프라인 센터)
            case "ROLE_ADMIN_11" -> "OFF_CENTER";
            default -> "LOC";
        };
    }

    /** AS-IS 컨트롤러가 role에 따라 지점 조건을 강제하는 부분. */
    private static void applyScope(OffgiveSearchParam searchParam, Manager viewer, String role) {
        switch (role) {
            case "OFF_MAIN" -> {
                searchParam.setShRceptBankCode(viewer.getBankCode());
                searchParam.setShRceptBankNm("");
            }
            case "OFF_SUB", "OFF_CENTER" -> {
                searchParam.setShRceptBankCode(viewer.getBankCode());
                searchParam.setShRceptBankNm(viewer.getPsitnNm());
            }
            case "SYSTEM" -> searchParam.setShRceptBankNm("");
            default -> {
                // LOC - 지점 조건 없음
                searchParam.setShRceptBankCode("");
                searchParam.setShRceptBankNm("");
            }
        }
    }

    private void fill(Model model, List<Row> list, int count, Pagination pagination, String role,
                      Manager viewer) {
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        model.addAttribute("role", role);
        model.addAttribute("shRceptBankCode", commonCodeService.labelsOf(CODE_TYPE_OFF_BANK));
        model.addAttribute("offBank", viewer == null ? null : viewer.getBankCode());
        model.addAttribute("psitnNm", viewer == null ? null : viewer.getPsitnNm());
        model.addAttribute("offBankNm", viewer == null ? null
                : commonCodeService.labelsOf(CODE_TYPE_OFF_BANK).get(viewer.getBankCode()));

        // AS-IS가 날짜버튼용으로 내려주는 값들(1년은 year 키다 - 다른 화면의 year1과 다르다)
        LocalDate today = LocalDate.now();
        model.addAttribute("today", today.format(DAY));
        model.addAttribute("week", today.minusDays(7).format(DAY));
        model.addAttribute("month1", today.minusMonths(1).format(DAY));
        model.addAttribute("month3", today.minusMonths(3).format(DAY));
        model.addAttribute("year", today.minusYears(1).format(DAY));
    }

    /** AS-IS 기부상태 표기: 100 신고 / 200 수납 / 그 외 과오납. TO-BE 낱말을 그 표기로 바꾼다. */
    private static String statusName(String cntrSttusCode) {
        if (cntrSttusCode == null) {
            return "";
        }
        return switch (cntrSttusCode) {
            case "REQUESTED" -> "신고";
            case "COMPLETED" -> "수납";
            default -> "과오납";
        };
    }

    /**
     * AS-IS 이름 마스킹 - {@code 첫글자 + ' * ' + 세번째글자부터}다.
     * 두 글자 이름이면 뒤가 비어 "김 * "처럼 보이는데 AS-IS 그대로다.
     */
    private static String maskName(String userName) {
        if (userName == null || userName.isEmpty()) {
            return "";
        }
        String head = userName.substring(0, 1);
        String tail = userName.length() > 2 ? userName.substring(2) : "";
        return head + " * " + tail;
    }

    private static String locgovText(String upperLocgovNm, String locgovNm) {
        return (upperLocgovNm == null ? "" : upperLocgovNm) + " " + (locgovNm == null ? "" : locgovNm);
    }

    /** AS-IS '지점/센터명' = 은행명 + 공백 + 지점명. */
    private static String bankText(String bankNm, String rceptBankNm) {
        return (bankNm == null ? "" : bankNm) + " " + (rceptBankNm == null ? "" : rceptBankNm);
    }

    /** AS-IS op:date() - yyyyMMdd[...] → yyyy-MM-dd. */
    private static String formatDate(String raw) {
        if (raw == null || raw.length() < 8) {
            return "";
        }
        if (raw.charAt(4) == '-') {
            return raw.substring(0, 10);
        }
        return raw.substring(0, 4) + "-" + raw.substring(4, 6) + "-" + raw.substring(6, 8);
    }

    /** AS-IS 신고일은 frstRegistPnttm의 앞 19자다(yyyy-MM-dd HH:mm:ss 모양). */
    private static String formatDateTime(String raw) {
        if (raw == null) {
            return "";
        }
        if (raw.length() >= 14 && raw.charAt(4) != '-') {
            return formatDate(raw) + " " + raw.substring(8, 10) + ":" + raw.substring(10, 12)
                    + ":" + raw.substring(12, 14);
        }
        return raw.length() > 19 ? raw.substring(0, 19) : raw;
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")
                ? "\"" + escaped + "\"" : escaped;
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

}
