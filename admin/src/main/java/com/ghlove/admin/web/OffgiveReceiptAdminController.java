package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.service.OffgiveClient;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
import com.ghlove.admin.web.support.OffgiveSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
 * <p><b>ROLE_ADMIN_11 미추가 - 확정(2026-10-12)</b>: AS-IS {@code OffgiveServiceImpl} 등
 * 코드에는 {@code ROLE_ADMIN_11}({@code OFF_CENTER}, 복지센터) 분기가 있지만, AS-IS
 * 분석문서(`05_기반계층_설계/README.md` §4.2)에 "ROLE_ADMIN_11 | (WCM) | DB에 없음. 코드만
 * 참조"라고 명시돼 있다 - 즉 <b>AS-IS 실제 운영 OP_ROLE 테이블에도 이 역할이 없어서, AS-IS
 * 자신도 이 분기를 한 번도 실행한 적이 없는 죽은 코드</b>다. "AS-IS에 코드가 있다고 실제로
 * 쓰인다는 뜻은 아니다" 원칙의 권한(Role) 버전 - TO-BE도 이 역할을 추가하지 않고 분기만
 * 자리로 남긴다(AS-IS가 꺼 둔 상태를 그대로 유지, [[as-is-parity-includes-disabled-state]]).
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
    private final ExcelDownloadWriter excelDownloadWriter;

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

        List<Row> all = load(searchParam, role);
        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);

        fill(model, all.stream().skip(pagination.getStartRow()).limit(pagination.getItemsPerPage()).toList(),
                all.size(), pagination, role, viewer);
        return "offgive/list";
    }

    /**
     * AS-IS GET /list/download-excel - {@code OffgiveManagerController.downloadExcelProcessByLocgovListPrj} →
     * {@code OffgiveServiceImpl.streamOffgiveList}(살아있는 코드) → {@code ExcelDownloadView}(로그인ID 암호화) 그대로.
     * 시트 "OFFGIVE_DATA", 제목 "오프라인 기부금 접수 관리 목록", 11컬럼·컬럼폭 AS-IS 값, No는 전체건수에서 역순(천단위 콤마).
     * 화면과 달리 <b>이름은 마스킹하지 않고</b>, 수납일·신고일·금액도 가공하지 않은 값이며, 기부지자체·지점/센터명은
     * Java 문자열 더하기라 값이 없으면 "null"이 찍힌다(AS-IS 그대로). 화면 JS가 신청일 범위가 비면 막는다.
     */
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

        List<OffgiveClient.AdminRow> rows = search(searchParam, role);
        Map<Long, String> names = namesOf(rows);
        Map<String, String> bankLabels = commonCodeService.labelsOf(CODE_TYPE_OFF_BANK);

        // SXSSF window 100, 임시파일 압축 - AS-IS streamOffgiveList 그대로
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        Sheet sheet = workbook.createSheet("OFFGIVE_DATA");
        ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);
        String[] headers = {"No", "접수번호(지자체)", "기부상태", "전자납부번호", "수납일", "기부 지자체", "이름",
                "기부 금액", "지점/센터명", "신고일", "특정사업에 기부하기 사업명"};
        int[] widths = {2000, 7500, 3000, 7500, 5000, 7500, 5000, 5000, 7500, 7500, 7500};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "오프라인 기부금 접수 관리 목록",
                headers, widths);

        int totalCount = rows.size();
        for (OffgiveClient.AdminRow r : rows) {
            org.apache.poi.ss.usermodel.Row xlRow = sheet.createRow(rowIndex++);
            xlRow.setHeight((short) 400);
            excelDownloadWriter.dataCell(xlRow, 0, styles, String.format("%,d", totalCount--));
            excelDownloadWriter.dataCell(xlRow, 1, styles, r.cntrSn());
            excelDownloadWriter.dataCell(xlRow, 2, styles, statusName(r.cntrSttusCode()));
            excelDownloadWriter.dataCell(xlRow, 3, styles, r.elctrnPayNo());
            excelDownloadWriter.dataCell(xlRow, 4, styles, r.sttemntPayDe());
            excelDownloadWriter.dataCell(xlRow, 5, styles, r.upperLocgovNm() + " " + r.locgovNm());
            excelDownloadWriter.dataCell(xlRow, 6, styles, names.get(r.userId()));
            excelDownloadWriter.dataCell(xlRow, 7, styles, r.cntrAmt() == null ? null : r.cntrAmt().toPlainString());
            excelDownloadWriter.dataCell(xlRow, 8, styles, bankLabels.get(r.rceptBankCode()) + " " + r.rceptBankNm());
            excelDownloadWriter.dataCell(xlRow, 9, styles, asIsDatetimeString(r.frstRegistPnttm()));
            excelDownloadWriter.dataCell(xlRow, 10, styles, r.prjSubject());
        }

        // 오프라인기부금접수현황목록_20260115142019.xlsx (AS-IS Const.DATETIME_FORMAT)
        String fileName = "오프라인기부금접수현황목록_"
                + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, viewer.getLoginId());
    }

    /* ==================== 내부 ==================== */

    private List<OffgiveClient.AdminRow> search(OffgiveSearchParam searchParam, String role) {
        return offgiveClient.adminSearch(
                searchParam.getShKeyword(), searchParam.getShText(),
                searchParam.getShFrstRegistPnttmStart(), searchParam.getShFrstRegistPnttmEnd(),
                searchParam.getShCntrAmtStart(), searchParam.getShCntrAmtEnd(),
                searchParam.getShCntrSttusCode(), searchParam.getShRceptBankCode(),
                searchParam.getShRceptBankNm(), role);
    }

    private Map<Long, String> namesOf(List<OffgiveClient.AdminRow> rows) {
        List<Long> userIds = rows.stream().map(OffgiveClient.AdminRow::userId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        return memberAdminClient.userNames(userIds);
    }

    /**
     * AS-IS 엑셀 신고일 - CUBRID DATETIME을 MyBatis가 문자열로 받은 값(java.sql.Timestamp 문자열, 예
     * "2026-08-14 10:18:56.0")을 그대로 쓴다. TO-BE는 yyyyMMddHHmmss(밀리초 없음)로 저장해 ".0"으로 끝난다.
     */
    private static String asIsDatetimeString(String raw) {
        if (raw == null || raw.length() < 14 || raw.charAt(4) == '-') {
            return raw;
        }
        return java.sql.Timestamp.valueOf(java.time.LocalDateTime.parse(raw.substring(0, 14),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))).toString();
    }

    /** 검색 + 이름 채우기 + 번호 매기기. */
    private List<Row> load(OffgiveSearchParam searchParam, String role) {
        List<OffgiveClient.AdminRow> rows = search(searchParam, role);
        Map<Long, String> names = namesOf(rows);
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
     * AS-IS 권한 role 판정. {@code ROLE_ADMIN_11}(OFF_CENTER) 분기는 AS-IS 자신도 OP_ROLE에
     * 없어 실행된 적 없는 죽은 코드라 TO-BE도 추가하지 않는다(클래스 주석 참고) - 자리만 유지.
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
            // AS-IS에도 할당 불가능한 죽은 분기(클래스 주석) - 자리만 유지, OP_ROLE에 추가 안 함
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

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

}
