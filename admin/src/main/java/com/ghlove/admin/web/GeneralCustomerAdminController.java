package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.DonationClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerAuthService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.service.PointClient;
import com.ghlove.admin.web.support.GeneralCustomerSearchParam;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 일반회원관리 (메뉴 4101) - AS-IS saleson.shop.user.GeneralCustomerManagerController
 * ({@code /opmanager/user/customer}) 재현.
 *
 * <p>AS-IS는 운영관리 한 덩어리라 회원·기부·포인트를 한 DB에서 조인했다. MSA에서는 세 서비스에
 * 흩어져 있어 admin이 조립한다:
 * <ul>
 *   <li>회원(가입일·아이디·이름·주소·수신동의·가입경로·배송지·탈퇴) → <b>member</b></li>
 *   <li>기부내역·기부누적액·발생포인트 → <b>donation</b>({@code g_cntr})</li>
 *   <li>사용포인트 → <b>point</b>({@code g_cntr_use_point})</li>
 *   <li>가입경로·기부형태·민간연계기관 <b>라벨</b> → admin의 {@code OP_COMMON_CODE}</li>
 * </ul>
 *
 * <p>AS-IS 동작 그대로: <b>진입(GET)에서는 조회하지 않고</b> 빈 목록을 내려준다(검색해야 조회).
 * 가입일 범위는 비면 오늘로 채운다. 목록은 <b>정상회원만</b> 나온다
 * ({@code STATUS_CODE = 9} - 탈퇴/휴면은 4105·4107 전용화면에서 본다).
 *
 * <p>포인트 내역은 AS-IS가 {@code G_CNTR}(적립)과 {@code G_CNTR_USE_POINT}(사용)를 UNION ALL한
 * 뒤 등록시각 역순으로 세우고, 각 행마다 <b>그 시점까지의 지자체별 잔액</b>(적립합 - 사용합)을
 * 계산한다. 두 표가 다른 서비스에 있으므로 각자 받아 와 {@link #buildPointRows}에서 같은 공식으로
 * 합친다.
 *
 * <p>TO-BE 전용으로 이미 있던 계정잠금 해제·권한 회수(SFR-002)는 엔드포인트를 그대로 두고
 * 상세화면 맨 아래 별도 블록으로 남겼다 - AS-IS 4101에는 없는 기능이라 섞지 않았다.
 */
@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class GeneralCustomerAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 가입경로 공통코드 - 100 온라인 / 200 오프라인. */
    private static final String CODE_TYPE_SBSCRB_SE = "SBSCRB_SE";
    /** 기부형태 공통코드. */
    private static final String CODE_TYPE_CNTR_PATH = "CNTR_PATH";
    /** 민간연계기관 공통코드. */
    private static final String CODE_TYPE_LINK_INSTT = "LINK_INSTT_CD";

    private final MemberAdminClient memberAdminClient;
    private final DonationClient donationClient;
    private final PointClient pointClient;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;
    private final ManagerAuthService managerAuthService;

    /** AS-IS 목록 한 행 - 9컬럼. */
    public record Row(Long userId, String createdDate, String loginId, String userName, String address,
                      String addressDetail, String receiveEmail, BigDecimal totalCntrAmt,
                      Long totalCntrBlcePoint, String sbscrbSeNm) {

        /** AS-IS {@code GeneralCustomerEncryptor.masking()}이 적용하는 마스킹을 그대로 재현한다. */
        public String getAddressText() {
            return (maskAddress(address) + " " + maskAddressDetail(addressDetail)).strip();
        }

        /** AS-IS: '0'이면 동의, 그 외(1·null)는 비동의. */
        public String getReceiveEmailText() {
            return "0".equals(receiveEmail) ? "동의" : "비동의";
        }
    }

    /** 기부내역 한 행 - 코드라벨까지 붙인 상태. */
    public record CntrRow(String cntrDe, String cntrLocgovText, String cntrPathNm, String linkInsttNm,
                          BigDecimal cntrAmt, BigDecimal cntrPoint, String elctrnPayNo, String sttemntPayDe) {

        /** AS-IS: 납부일이 있을 때만 발생포인트를 찍고, 없으면 '-'다. */
        public boolean isPaid() {
            return sttemntPayDe != null && !sttemntPayDe.isBlank();
        }

        /** AS-IS: 납부일이 없으면 '미결제'. */
        public String getSttemntPayDeText() {
            return isPaid() ? sttemntPayDe : "미결제";
        }
    }

    /** 포인트 내역 한 행 - OCC(적립)/USE(사용) 두 종류를 한 모양으로 합친 것. */
    public record PointRow(String pointType, String cntrDe, String cntrLocgovText, BigDecimal cntrAmt,
                           BigDecimal cntrPoint, Long cntrUsePoint, long cntrBlcePoint, String orderCode) {

        public boolean isOcc() {
            return "OCC".equals(pointType);
        }
    }

    /* ==================== 목록 ==================== */

    /** AS-IS GET /list - 조회하지 않고 빈 목록. */
    @GetMapping
    public String list(@ModelAttribute("searchParam") GeneralCustomerSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        searchParam.applyDefaults();
        fillList(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request));
        return "member-admin/list";
    }

    /** AS-IS POST /list - 실제 검색. */
    @PostMapping
    public String search(@ModelAttribute("searchParam") GeneralCustomerSearchParam searchParam,
                         HttpServletRequest request, Model model) {
        searchParam.applyDefaults();

        // member 검색 API는 page가 0부터다(화면은 1부터)
        MemberAdminClient.SearchResult result = memberAdminClient.search(
                searchParam.getFromDateForApi(), searchParam.getToDateForApi(),
                searchParam.getSrchKey(), searchParam.getSrchValue(),
                searchParam.getSrchSbscrbSeCode(), searchParam.getSrchReceiveEmail(),
                Math.max(searchParam.getPage() - 1, 0), searchParam.getItemsPerPage());

        List<Long> userIds = result.content().stream().map(MemberAdminClient.Row::userId).toList();
        Map<Long, DonationClient.MemberCumulativeTotal> donationTotals =
                donationClient.memberDonationTotals(userIds);
        Map<Long, Long> usedPoints = pointClient.memberUsedPointTotals(userIds);
        Map<String, String> sbscrbSeLabels = commonCodeService.labelsOf(CODE_TYPE_SBSCRB_SE);

        List<Row> rows = result.content().stream().map(r -> {
            DonationClient.MemberCumulativeTotal total = donationTotals.get(r.userId());
            BigDecimal amount = total == null ? null : total.totalCntrAmt();
            Long balance = null;
            if (total != null && total.totalCntrPoint() != null) {
                balance = total.totalCntrPoint().longValue() - usedPoints.getOrDefault(r.userId(), 0L);
            }
            return new Row(r.userId(), formatDate(r.createdDate()), r.loginId(), r.userName(), r.address(),
                    r.addressDetail(), r.receiveEmail(),
                    amount != null && amount.signum() != 0 ? amount : null, balance,
                    sbscrbSeLabels.get(r.sbscrbSeCode()));
        }).toList();

        int count = (int) result.totalElements();
        fillList(model, rows, count,
                Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                        .withLinkFrom(request));
        return "member-admin/list";
    }

    /* ==================== 상세 ==================== */

    /**
     * AS-IS GET /details/{userId}. {@code getGeneralCustomerDetails}가
     * {@code GeneralCustomerEncryptor.masking()}(마스킹 true)을 거친 값을 돌려주므로 주소뿐 아니라
     * 휴대폰·이메일도 이 화면에서는 마스킹된 채로 보인다 - 비마스킹은 개인정보 열람 팝업
     * (informationAccess, info-access.html) 전용이다.
     */
    @GetMapping("/details/{userId}")
    public String details(@PathVariable Long userId, Model model) {
        MemberAdminClient.Detail detail = memberAdminClient.detail(userId);
        fillDetail(model, detail);
        model.addAttribute("addressText", detail == null ? ""
                : (maskAddress(detail.address()) + " " + maskAddressDetail(detail.addressDetail())).strip());
        model.addAttribute("phoneNumberText", detail == null ? "" : maskPhoneNumber(detail.phoneNumber()));
        model.addAttribute("emailText", detail == null ? "" : maskEmail(detail.email()));
        model.addAttribute("cumclativeTotal", cumulativeTotal(userId));
        return "member-admin/details";
    }

    /** AS-IS GET /details/{userId}/cntr-list - 상세화면이 ajax로 끼워넣는 조각(layout=blank). */
    @GetMapping("/details/{userId}/cntr-list")
    public String cntrList(@PathVariable Long userId,
                           @RequestParam(defaultValue = "1") int page,
                           Model model) {
        int itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        DonationClient.MemberCntrList list = donationClient.memberCntrList(
                userId, Math.max(page - 1, 0) * itemsPerPage, itemsPerPage);

        Map<String, String> pathLabels = commonCodeService.labelsOf(CODE_TYPE_CNTR_PATH);
        Map<String, String> insttLabels = commonCodeService.labelsOf(CODE_TYPE_LINK_INSTT);
        List<CntrRow> rows = list.content().stream()
                .map(r -> new CntrRow(r.cntrDe(), locgovText(r.cntrUpperLocgovNm(), r.cntrLocgovNm()),
                        pathLabels.get(r.cntrPathCode()), insttLabels.get(r.linkInsttCd()),
                        r.cntrAmt(), r.cntrPoint(), r.elctrnPayNo(), r.sttemntPayDe()))
                .toList();

        model.addAttribute("list", rows);
        model.addAttribute("count", list.count());
        model.addAttribute("pagination",
                Pagination.of(list.count(), page).withLink("javascript:getCntrList([page])"));
        return "member-admin/cntr-list";
    }

    /** AS-IS GET /details/{userId}/point-list - 적립(donation) + 사용(point)을 합친 조각. */
    @GetMapping("/details/{userId}/point-list")
    public String pointList(@PathVariable Long userId,
                            @RequestParam(defaultValue = "1") int page,
                            Model model) {
        List<PointRow> all = buildPointRows(userId);
        Pagination pagination = Pagination.of(all.size(), page).withLink("javascript:getPointList([page])");

        model.addAttribute("list", all.stream().skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage()).toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "member-admin/point-list";
    }

    /* ==================== 개인정보 열람 ==================== */

    /** AS-IS GET /popup/password/{userId} - 비밀번호 확인 팝업(layout=base). */
    @GetMapping("/popup/password/{userId}")
    public String passwordConfirmPopup(@PathVariable Long userId,
                                       @RequestParam(required = false) String message,
                                       Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("message", message);
        return "member-admin/password-confirm";
    }

    /**
     * AS-IS POST /popup/access/{userId} - 운영자 비밀번호 확인 후 비마스킹 상세를 보여준다.
     * 틀리면 AS-IS처럼 비밀번호 팝업으로 되돌린다("비밀번호가 일치하지 않습니다.").
     * 통과하면 member에 개인정보 열람 이력을 남긴다(G_INDVDLINFO_READNG_HIST).
     */
    @PostMapping("/popup/access/{userId}")
    public String informationAccess(@PathVariable Long userId, @RequestParam(required = false) String password,
                                    HttpSession session, Model model) {
        Manager viewer = viewer(session);
        try {
            managerAuthService.checkCredentials(viewer.getLoginId(), password);
        } catch (ManagerException e) {
            return "redirect:/admin/members/popup/password/" + userId
                    + "?message=" + encode("비밀번호가 일치하지 않습니다.");
        }
        memberAdminClient.recordPiiAccess(userId, viewer.getUserId());

        fillDetail(model, memberAdminClient.detail(userId));
        return "member-admin/info-access";
    }

    /* ==================== 회원탈퇴 ==================== */

    /** AS-IS GET /popup/secede/{userId} - 회원탈퇴 팝업(600x330). */
    @GetMapping("/popup/secede/{userId}")
    public String secedePopup(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("cumclativeTotal", cumulativeTotal(userId));
        return "member-admin/secede-write";
    }

    /**
     * AS-IS POST /secede - ajax. 응답 {isSuccess, data:{code, isLogout}}이고 화면은
     * SUCC/ERR_ALR_SECEDE/ERR_ONE_PASS를 구분해 문구를 띄운다.
     */
    @PostMapping("/secede")
    @ResponseBody
    public Map<String, Object> secede(@RequestParam Long userId,
                                      @RequestParam(required = false) String leaveReason,
                                      HttpSession session) {
        Manager viewer = viewer(session);
        String code = memberAdminClient.withdraw(userId, leaveReason,
                viewer == null ? null : viewer.getUserId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", code);
        // AS-IS는 탈퇴 대상이 로그인한 사용자 자신이면 로그아웃시킨다. admin 콘솔의 로그인 주체는
        // OP_MANAGER이고 탈퇴 대상은 회원(OP_USER)이라 실제로 겹치는 경우는 없지만 조건은 남긴다.
        data.put("isLogout", viewer != null && userId.equals(viewer.getUserId()) ? "Y" : "N");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        return response;
    }

    /* ==================== 배송지 관리 ==================== */

    /** AS-IS GET /delivery/{userId} - 배송지 조회 팝업(800x480). */
    @GetMapping("/delivery/{userId}")
    public String deliveryPopup(@PathVariable Long userId, Model model) {
        model.addAttribute("list", memberAdminClient.deliveries(userId));
        return "member-admin/delivery";
    }

    /* ==================== 내부 ==================== */

    private void fillList(Model model, List<Row> list, int count, Pagination pagination) {
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);

        // AS-IS list.jsp 하단의 숨은 "날짜 셋팅 영역"
        LocalDate today = LocalDate.now();
        Map<String, String> searchDates = new LinkedHashMap<>();
        searchDates.put("today", today.format(DAY));
        searchDates.put("week", today.minusDays(7).format(DAY));
        searchDates.put("month1", today.minusMonths(1).format(DAY));
        searchDates.put("month3", today.minusMonths(3).format(DAY));
        searchDates.put("year1", today.minusMonths(12).format(DAY));
        model.addAttribute("searchDates", searchDates);
    }

    /**
     * 상세·열람팝업 공통 모델. member는 날짜를 원본(yyyyMMddHHmmss)으로 내려주고 AS-IS 화면은
     * 가입일을 {@code yyyy-MM-dd}, 마지막 로그인을 {@code yyyy-MM-dd HH:mm:ss}로 찍는다
     * (AS-IS 매퍼의 DATE_FORMAT) - 그 변환을 여기서 한다.
     */
    private void fillDetail(Model model, MemberAdminClient.Detail detail) {
        model.addAttribute("details", detail);
        model.addAttribute("sbscrbSeNm", detail == null ? null
                : commonCodeService.labelsOf(CODE_TYPE_SBSCRB_SE).get(detail.sbscrbSeCode()));
        model.addAttribute("createdDateText", detail == null ? "" : formatDate(detail.createdDate()));
        model.addAttribute("loginDateText", detail == null ? "" : formatDateTime(detail.loginDate()));
    }

    /** 누적합계 - 기부금액·발생포인트는 donation, 사용포인트는 point, 잔액은 둘의 차. */
    private Map<String, Object> cumulativeTotal(Long userId) {
        DonationClient.MemberCumulativeTotal donation = donationClient.memberCumulativeTotal(userId);
        long used = pointClient.memberUsedPointTotals(List.of(userId)).getOrDefault(userId, 0L);
        long earned = donation.totalCntrPoint() == null ? 0L : donation.totalCntrPoint().longValue();

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("totalCntrAmt", donation.totalCntrAmt() == null ? BigDecimal.ZERO : donation.totalCntrAmt());
        total.put("totalCntrPoint", earned);
        total.put("totalCntrUsePoint", used);
        total.put("totalCntrBlcePoint", earned - used);
        return total;
    }

    /**
     * 포인트 내역 조립 - AS-IS {@code getGeneralCustomerPointListByParam}의 UNION ALL + 잔액공식을
     * 그대로 따른다.
     *
     * <p>각 행의 잔액은 <b>같은 지자체</b>에서 그 행의 등록시각까지 적립된 포인트 합에서, 같은
     * 기준으로 사용된 포인트 합을 뺀 값이다(AS-IS의 CURRENT_CNTR_POINT - CURRENT_CNTR_USE_POINT).
     * 적립 쪽 합은 납부완료 건만, 사용 쪽 합은 <b>use_se_code를 가리지 않고</b> 모두 더한다 -
     * AS-IS 서브쿼리가 그렇다. 정렬은 등록시각 역순, 같으면 USE_SN 역순이다.
     */
    private List<PointRow> buildPointRows(Long userId) {
        List<DonationClient.MemberPointOccRow> occRows = donationClient.memberPointOccRows(userId);
        List<PointClient.MemberPointUseRow> useRows = pointClient.memberPointUseRows(userId);
        Map<String, LocgovClient.LocgovInfo> locgovs = locgovsByCode();

        record Entry(String pointType, String pnttm, Integer useSn, String locgovCode, String cntrDe,
                     BigDecimal cntrAmt, BigDecimal cntrPoint, Long cntrUsePoint, String orderCode) {
        }

        List<Entry> entries = new ArrayList<>();
        for (DonationClient.MemberPointOccRow r : occRows) {
            entries.add(new Entry("OCC", r.frstRegistPnttm(), null, r.cntrLocgovCode(), r.cntrDe(),
                    r.cntrAmt(), r.cntrPoint(), null, null));
        }
        for (PointClient.MemberPointUseRow r : useRows) {
            entries.add(new Entry("USE", r.frstRegistPnttm(), r.useSn(), r.cntrLocgovCode(),
                    r.pointUseDe(), null, null, r.cntrUsePoint(), r.orderCode()));
        }

        entries.sort(Comparator.comparing((Entry e) -> e.pnttm() == null ? "" : e.pnttm())
                .thenComparing(e -> e.useSn() == null ? 0 : e.useSn())
                .reversed());

        List<PointRow> rows = new ArrayList<>();
        for (Entry e : entries) {
            long earned = occRows.stream()
                    .filter(o -> sameLocgov(o.cntrLocgovCode(), e.locgovCode()))
                    .filter(o -> upTo(o.frstRegistPnttm(), e.pnttm()))
                    .mapToLong(o -> o.cntrPoint() == null ? 0L : o.cntrPoint().longValue())
                    .sum();
            long used = useRows.stream()
                    .filter(u -> sameLocgov(u.cntrLocgovCode(), e.locgovCode()))
                    .filter(u -> upTo(u.frstRegistPnttm(), e.pnttm()))
                    .mapToLong(u -> u.cntrUsePoint() == null ? 0L : u.cntrUsePoint())
                    .sum();

            LocgovClient.LocgovInfo locgov = locgovs.get(e.locgovCode());
            String locgovText = locgov == null ? "" : locgovText(locgov.upperLocgovNm(), locgov.locgovNm());
            rows.add(new PointRow(e.pointType(), formatDate(e.cntrDe()), locgovText, e.cntrAmt(),
                    e.cntrPoint(), e.cntrUsePoint(), earned - used, e.orderCode()));
        }
        return rows;
    }

    private Map<String, LocgovClient.LocgovInfo> locgovsByCode() {
        Map<String, LocgovClient.LocgovInfo> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.locgovCode(), l);
        }
        return map;
    }

    private static boolean sameLocgov(String a, String b) {
        return a != null && a.equals(b);
    }

    /** AS-IS {@code A.FRST_REGIST_PNTTM <= GC.FRST_REGIST_PNTTM} - 둘 다 yyyyMMddHHmmss 문자열이다. */
    private static boolean upTo(String candidate, String pivot) {
        if (candidate == null || pivot == null) {
            return false;
        }
        return candidate.compareTo(pivot) <= 0;
    }

    /** AS-IS는 지자체를 '시도명 공백 시군구명'으로 붙여 보여준다. */
    private static String locgovText(String upperLocgovNm, String locgovNm) {
        return (upperLocgovNm == null ? "" : upperLocgovNm) + " " + (locgovNm == null ? "" : locgovNm);
    }

    /**
     * AS-IS {@code DefaultDataMasking.mask(..., Masking.GH_ADDRESS)} verbatim 재현 - 공백으로
     * 나눈 토큰 중 앞 2개(시/도, 시/군/구)는 그대로 두고, 그 뒤 토큰은 각각
     * <b>(글자수-1)개의 '*'</b>로 바꿔 <b>구분자 없이 이어붙인다</b>(AS-IS 원본이 그렇다 - 토큰이
     * 여러 개 남으면 공백 없이 별표만 길게 이어진다).
     */
    private static String maskAddress(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        String[] tokens = address.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i < 2) {
                sb.append(tokens[i]).append(" ");
            } else if (!tokens[i].isEmpty()) {
                sb.append("*".repeat(Math.max(tokens[i].length() - 1, 0)));
            }
        }
        return sb.toString();
    }

    /**
     * AS-IS {@code DefaultDataMasking.mask(..., Masking.ADDRESS_DETAIL)} verbatim 재현 - 공백으로
     * 나눈 토큰 전부(첫 토큰 포함)를 각각 (글자수-1)개의 '*'로 바꾸고 토큰마다 뒤에 공백을 붙인다.
     */
    private static String maskAddressDetail(String addressDetail) {
        if (addressDetail == null || addressDetail.isBlank()) {
            return "";
        }
        String[] tokens = addressDetail.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                sb.append("*".repeat(Math.max(token.length() - 1, 0))).append(" ");
            }
        }
        return sb.toString().stripTrailing();
    }

    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("^(\\d{2,3})-?(\\d{3,4})-?(\\d{4})$");

    /**
     * AS-IS {@code DefaultDataMasking.mask(..., Masking.GH_PHONE_NUMBER)} verbatim 재현 - 첫
     * 그룹(지역/통신사 번호)만 남기고 가운데·끝 그룹은 전부 '*'로 바꾼다(일반 PHONE_NUMBER보다
     * 더 많이 가리는 고향사랑 전용 변형). 패턴에 안 맞으면 원문 그대로(AS-IS 그대로).
     */
    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        java.util.regex.Matcher m = PHONE_PATTERN.matcher(phoneNumber);
        if (!m.matches()) {
            return phoneNumber;
        }
        boolean hyphen = phoneNumber.indexOf('-') > -1;
        String sep = hyphen ? "-" : "";
        return m.group(1) + sep + "*".repeat(m.group(2).length()) + sep + "*".repeat(m.group(3).length());
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile("^(.)(.*)([@]{1})(.*)$");

    /**
     * AS-IS {@code DefaultDataMasking.mask(..., Masking.GH_EMAIL)} verbatim 재현 - 아이디 첫
     * 글자만 남기고 나머지를 '*'로 바꾼다. 단, <b>아이디가 2글자뿐이면(첫글자+1글자) AS-IS 알고리즘이
     * 그 첫 글자까지 통째로 별표로 덮어버린다</b>(원본 구현의 그 특이점까지 그대로 옮긴다).
     * 도메인은 가리지 않는다. 패턴에 안 맞으면 원문 그대로.
     */
    private static String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        java.util.regex.Matcher m = EMAIL_PATTERN.matcher(email);
        if (!m.matches()) {
            return email;
        }
        String first = m.group(1);
        String rest = m.group(2);
        String maskedLocal = rest.length() < 2
                ? "*".repeat(first.length() + rest.length())
                : first + rest.charAt(0) + "*".repeat(rest.length() - 1);
        return maskedLocal + m.group(3) + m.group(4);
    }

    /** yyyyMMdd[HHmmss] → yyyy-MM-dd (AS-IS DATE_FORMAT). 이미 구분자가 있으면 앞 10자만 쓴다. */
    private static String formatDate(String raw) {
        if (raw == null || raw.length() < 8) {
            return raw;
        }
        if (raw.charAt(4) == '-') {
            return raw.substring(0, 10);
        }
        return raw.substring(0, 4) + "-" + raw.substring(4, 6) + "-" + raw.substring(6, 8);
    }

    /** yyyyMMddHHmmss → yyyy-MM-dd HH:mm:ss (AS-IS 마지막 로그인 표기). */
    private static String formatDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        if (raw.length() < 14) {
            return formatDate(raw);
        }
        if (raw.charAt(4) == '-') {
            return raw;
        }
        return formatDate(raw) + " " + raw.substring(8, 10) + ":" + raw.substring(10, 12)
                + ":" + raw.substring(12, 14);
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
