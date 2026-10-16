package com.ghlove.admin.web;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerAuthService;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.SellerManageClient;
import com.ghlove.admin.web.support.AdminRole;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 답례품제공자 관리 (AS-IS opmanager/seller, 메뉴 16151) - 판매자(입점업체) 목록·등록·수정.
 *
 * <p>기존 TO-BE 화면은 상태 변경 버튼이 있는 축소 구현(목록 + 짧은 폼)이었다. AS-IS {@code SellerManagerController}에 맞춰 다시 옮겼다.
 *
 * <p>★ AS-IS 특징(그대로): GET 진입은 빈 목록(POST 검색에서만 조회, 등록일 기본값=오늘) · 등록·수정 폼은 지자체 담당자(ROLE_ADMIN_5/6)만
 * 저장할 수 있다(그 밖은 폼 id가 {@code impossible-form}이라 저장 시 alert) · 등록 버튼·비밀번호 초기화는 시스템/행안부(SYS)에게 alert로 막힌다.
 * 지자체 담당자의 목록·등록은 자기 지자체로 한정된다(AS-IS는 요청 파라미터로 덮어쓸 수 있지만 TO-BE는 서버에서 강제).
 *
 * <p>옮기지 않은 것: 목록의 "로그인"(shadow-login - 제공자 포털 로그인 세션이 아직 없다), 일괄삭제/삭제(AS-IS도 화면 버튼이 주석처리),
 * 판매자 검색 팝업 find/find-popup(다른 화면용), MD 검색 팝업(담당MD 영역이 hidden), 비밀번호 변경 팝업(호출이 주석처리), 변경이력 팝업(본문 주석처리),
 * id-duplicate-Check(화면이 부르지 않음).
 */
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String LIST_URL = "/seller/list";

    private final SellerManageClient client;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;
    private final ManagerAuthService managerAuthService;
    private final FlashRedirect flashRedirect;

    // ---- 목록 ----

    /** AS-IS GET list - 진입에서는 조회하지 않는다(빈 목록). */
    @GetMapping({"", "/", "/list"})
    public String list(@ModelAttribute("sellerParam") SellerParam param, HttpServletRequest request,
                       HttpSession session, Model model) {
        fillList(param, request, session, model, false);
        return "seller/list";
    }

    /** AS-IS POST list - 검색. */
    @PostMapping("/list")
    public String search(@ModelAttribute("sellerParam") SellerParam param, HttpServletRequest request,
                         HttpSession session, Model model) {
        fillList(param, request, session, model, true);
        return "seller/list";
    }

    private void fillList(SellerParam param, HttpServletRequest request, HttpSession session, Model model, boolean doSearch) {
        Manager manager = manager(session);
        param.applyDefaults();

        // 지자체 한정: 지자체 담당자는 자기 지자체만, 그 밖은 화면의 시도/시군구 선택(시군구가 우선)
        String locgov = null;
        if (MenuService.isLocgovScoped(manager)) {
            locgov = manager.getLocgovCode();
        } else {
            if (has(param.getShWdr())) {
                locgov = param.getShWdr();
            }
            if (has(param.getShLocgovCode())) {
                locgov = param.getShLocgovCode();
            }
        }
        SellerManageClient.Search search = new SellerManageClient.Search(param.getStatusCode(), param.getWhere(), param.getQuery(),
                param.getStartDate(), param.getEndDate(), param.getItemApprovalType(), locgov);

        int count = doSearch ? client.count(search) : 0;
        Pagination pagination = Pagination.of(count, param.getPage(), param.getItemsPerPage()).withLinkFrom(request);
        List<SellerManageClient.ListRow> rows = doSearch ? client.list(search, pagination.getStartRow(), pagination.getItemsPerPage()) : List.of();

        model.addAttribute("list", rows);
        model.addAttribute("pagination", pagination);
        model.addAttribute("totalCount", count);
        model.addAttribute("adminRole", AdminRole.of(manager));
        model.addAttribute("wdr", wdrOptions());
        model.addAttribute("locgovNames", locgovNames());
    }

    // ---- 등록 ----

    /** AS-IS GET create. */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        Manager manager = manager(session);
        Map<String, Object> seller = emptySeller();
        seller.put("shippingFlag", "N");
        fillForm(model, manager, seller, "create", "/seller/create");
        return "seller/form";
    }

    /** AS-IS POST create - 지자체 담당자의 지자체로 등록하고 계정(OP_SELLER_USER)도 같이 만든다. */
    @PostMapping("/create")
    public String create(HttpServletRequest request, HttpSession session) {
        Manager manager = manager(session);
        Map<String, String> params = params(request);
        client.create(params, files(request), manager.getUserId(), manager.getLocgovCode(), managerAuthService.passwordExpiredDate());
        return flashRedirect.to(LIST_URL, "등록되었습니다.");
    }

    // ---- 수정 ----

    /** AS-IS GET edit/{sellerId}. */
    @GetMapping("/edit/{sellerId}")
    public String editForm(@PathVariable long sellerId, HttpSession session, Model model) {
        Map<String, Object> detail = client.detail(sellerId);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "판매자를 찾을 수 없습니다.");
        }
        Map<String, Object> seller = emptySeller();
        seller.putAll(detail);
        // AS-IS parseTelephoneNumber - 국번이 없는 번호(숫자 두 덩어리)는 "미선택-"을 붙여 국번 select가 "미선택"으로 선택되게 한다
        String tel = str(seller.get("telephoneNumber"));
        if (tel != null && tel.split("-").length == 2) {
            seller.put("telephoneNumber", "미선택-" + tel);
        }
        seller.put("locgovNm", locgovNames().get(str(seller.get("locgovCode"))));
        fillForm(model, manager(session), seller, "edit", "/seller/edit/" + sellerId);
        return "seller/form";
    }

    /** AS-IS POST edit/{sellerId}. */
    @PostMapping("/edit/{sellerId}")
    public String edit(@PathVariable long sellerId, HttpServletRequest request, HttpSession session) {
        Manager manager = manager(session);
        client.update(sellerId, params(request), files(request), manager.getUserId());
        return flashRedirect.to(LIST_URL, "수정되었습니다.");
    }

    // ---- ajax ----

    /** AS-IS POST makeSellerId - 지자체코드 기준 새 아이디. */
    @ResponseBody
    @PostMapping("/makeSellerId")
    public Map<String, Object> makeSellerId(@RequestParam(required = false) String locgovCode) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("isSuccess", true);
        r.put("data", client.makeSellerId(locgovCode));
        return r;
    }

    /** AS-IS POST init-seller-password - 본문 "S"(성공). */
    @ResponseBody
    @PostMapping("/init-seller-password")
    public String initSellerPassword(@RequestParam long sellerId, @RequestParam(required = false) String password, HttpSession session) {
        client.initPassword(sellerId, password, manager(session).getUserId());
        return "S";
    }

    /** AS-IS POST deleteFile - 첨부 삭제(file1/file2/file3). */
    @ResponseBody
    @PostMapping("/deleteFile")
    public Map<String, Object> deleteFile(@RequestParam long sellerId, @RequestParam String fileType, HttpSession session) {
        client.deleteFile(sellerId, fileType, manager(session).getUserId());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("isSuccess", true);
        return r;
    }

    // ---- helpers ----

    private void fillForm(Model model, Manager manager, Map<String, Object> seller, String mode, String action) {
        model.addAttribute("seller", seller);
        model.addAttribute("mode", mode);
        model.addAttribute("action", action);
        model.addAttribute("adminRole", AdminRole.of(manager));
        // AS-IS hasRole('ROLE_ADMIN_5') || hasRole('ROLE_ADMIN_6') - 이 권한만 폼을 저장할 수 있다
        model.addAttribute("canSave", "ROLE_ADMIN_5".equals(manager.getAuthority()) || "ROLE_ADMIN_6".equals(manager.getAuthority()));
        Map<String, String> locgov = new LinkedHashMap<>();
        locgov.put("id", manager.getLocgovCode());
        locgov.put("label", manager.getLocgovCode() == null ? null : locgovNames().get(manager.getLocgovCode()));
        model.addAttribute("userLocgovCodeDetails", locgov);
        model.addAttribute("telCodes", codes("TEL"));
        model.addAttribute("phoneCodes", codes("PHONE"));
        model.addAttribute("emailCodes", codes("EMAIL"));
        model.addAttribute("bankCodes", codes("CJ_BANK_LIST"));
        model.addAttribute("remittanceTypeCodes", codes("REMITTANCE_TYPE"));
        model.addAttribute("phoneArr", parts(seller.get("phoneNumber")));
        model.addAttribute("telArr", parts(seller.get("telephoneNumber")));
        // AS-IS fn:split(tel_arr[0], ',')[0] - 대표번호 국번(쉼표로 여러 개가 붙은 옛 데이터는 첫 번째)
        String telFirst = parts(seller.get("telephoneNumber")).get(0).split(",")[0];
        model.addAttribute("telFirst", telFirst);
        model.addAttribute("telMatched", codes("TEL").stream().anyMatch(c -> c.getId().equals(telFirst)));
        model.addAttribute("faxArr", parts(seller.get("faxNumber")));
        model.addAttribute("businessNumberArr", parts(seller.get("businessNumber")));
        model.addAttribute("secondTelArr", parts(seller.get("secondTelephoneNumber")));
        model.addAttribute("secondPhoneArr", parts(seller.get("secondPhoneNumber")));
        String tel = str(seller.get("telephoneNumber"));
        // AS-IS Seller.getManualNumberType - 대표번호가 "050x-…" 모양(첫 덩어리 4자리, 050으로 시작)이면 직접입력
        boolean manual = false;
        if (tel != null && !tel.isEmpty()) {
            String[] t = tel.split("-");
            manual = t.length == 3 && t[0].length() == 4 && "050".equals(t[0].substring(0, 3));
        }
        model.addAttribute("manualNumberType", manual);
    }

    /** AS-IS 폼 입력칸 이름 그대로 - 같은 이름이 여러 번 오면 첫 값(스프링 바인딩과 같다). */
    private static Map<String, String> params(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v != null && v.length > 0 ? v[0] : ""));
        return params;
    }

    private static MultipartFile[] files(HttpServletRequest request) {
        MultipartFile[] files = new MultipartFile[3];
        if (request instanceof MultipartHttpServletRequest mp) {
            for (int i = 0; i < 3; i++) {
                files[i] = mp.getFile("uploadFile" + (i + 1));
            }
        }
        return files;
    }

    /** "010-1234-5678" → ["010","1234","5678"]. 모자라는 자리는 빈 칸(AS-IS fn:split + 없는 인덱스는 빈 값). */
    private static List<String> parts(Object value) {
        List<String> out = new ArrayList<>(List.of("", "", ""));
        String s = str(value);
        if (s != null && !s.isEmpty()) {
            String[] p = s.split("-");
            for (int i = 0; i < p.length && i < 3; i++) {
                out.set(i, p[i]);
            }
        }
        return out;
    }

    private List<CommonCode> codes(String codeType) {
        return commonCodeService.listByType(codeType).stream().filter(c -> "Y".equals(c.getUseYn())).toList();
    }

    /** AS-IS CodeUtils.getCodeList("WDR") - 시·도 목록(id·label). */
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

    /** 지자체코드 → "시도 시군구"(AS-IS CONCAT(UPPER_LOCGOV_NM, ' ', LOCGOV_NM)). */
    private Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            names.put(l.locgovCode(), (l.upperLocgovNm() == null ? "" : l.upperLocgovNm() + " ") + l.locgovNm());
        }
        return names;
    }

    /** 폼이 읽는 키를 모두 가진 빈 판매자 - Map에 없는 키를 SpEL이 읽으면 예외라서 미리 채운다. */
    private static Map<String, Object> emptySeller() {
        Map<String, Object> m = new LinkedHashMap<>();
        for (String k : new String[]{"sellerId", "sellerName", "loginId", "userName", "telephoneNumber", "phoneNumber", "faxNumber", "email",
                "post", "address", "addressDetail", "secondUserName", "secondTelephoneNumber", "secondPhoneNumber", "secondEmail",
                "companyName", "representativeName", "businessNumber", "businessLocation", "businessType", "businessItems",
                "commissionRate", "remittanceType", "remittanceDay", "bankName", "bankInName", "bankAccountNumber", "shippingFlag",
                "shipping", "shippingFreeAmount", "shippingExtraCharge1", "shippingExtraCharge2", "itemApprovalType", "smsSendTime",
                "mdId", "mdName", "statusCode", "mailOrderNumber", "buySafetyUseConfirmNumber", "taxType", "fileNameCertificate1",
                "fileNameCertificate2", "fileNameCertificate3", "locgovCode", "locgovNm", "adultItemYn", "communityBusinessYn",
                "emailBefore", "emailAfter", "receiveSms", "mberCi", "userId"}) {
            m.put(k, null);
        }
        return m;
    }

    private Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    /** AS-IS SellerParam + SearchParam - 검색구분 기본값은 상호명, 등록일 기본값은 오늘, 출력수 10. */
    @Getter
    @Setter
    public static class SellerParam {
        private String shWdr;
        private String shLocgovCode;
        private String where = "COMPANY_NAME";
        private String query;
        private String startDate;
        private String endDate;
        private String statusCode = "";
        private String itemApprovalType = "";
        private int itemsPerPage = 10;
        private int page = 1;

        void applyDefaults() {
            String today = LocalDate.now().format(DAY);
            if (startDate == null || startDate.isBlank()) {
                startDate = today;
            }
            if (endDate == null || endDate.isBlank()) {
                endDate = today;
            }
        }
    }
}
