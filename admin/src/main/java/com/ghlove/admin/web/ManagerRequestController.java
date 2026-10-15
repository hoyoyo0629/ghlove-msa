package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.ManagerRequest;
import com.ghlove.admin.service.*;
import com.ghlove.admin.web.support.ManagerRequestParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 권한 요청 (AS-IS opmanager/manager-request/*, G_MNGR_REQST) - 이미 회원가입돼 있는 사람이 관리자 로그인 화면에서
 * 회원 아이디·비밀번호(또는 등록한 인증서)로 확인받고 신청하면(비로그인 화면), 승인관리(1406) 권한자가 승인/거절한다.
 * 대민 로그인을 거치는 별도 진입점은 AS-IS에 없다.
 */
@Controller
@RequiredArgsConstructor
public class ManagerRequestController {

    private final ManagerRequestService managerRequestService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final CommonCodeService commonCodeService;
    private final MemberClient memberClient;
    private final LocgovClient locgovClient;
    private final DesignatedProjectClient designatedProjectClient;
    private final WelfareCenterAdminClient welfareCenterAdminClient;
    private final com.ghlove.admin.repository.RoleRepository roleRepository;
    private final com.ghlove.admin.repository.ManagerRepository managerRepository;

    /** 로그인 화면·인증서 로그인에서 확인된 신청 회원 - 신청 저장(create) 때 이 값만 믿는다. */
    static final String SESSION_REQUEST_USER_ID = "managerRequestUserId";

    /**
     * AS-IS {@code POST /opmanager/manager-request/form}(비로그인) - 관리자 로그인 화면에서 관리자가 아닌 회원이 아이디·비밀번호를
     * 맞게 넣으면(결과코드 REQ_NOT) 화면이 이 주소로 아이디·비밀번호를 POST한다. 회원 확인에 실패하면 로그인 화면으로 돌려보내
     * "사용자 정보를 다시 확인해주세요."를 띄운다(AS-IS ViewUtils.redirect 메시지).
     */
    @PostMapping("/admin/manager-requests/form")
    public String formFromLogin(@RequestParam(required = false) String loginId,
                                @RequestParam(required = false) String password,
                                HttpSession session, Model model) {
        MemberClient.CredentialCheck check = memberClient.checkCredential(loginId, password);
        MemberClient.MemberInfo member = check != null && "OK".equals(check.result())
                ? memberClient.fetchOrNull(check.userId()) : null;
        if (member == null) {
            return flashRedirect.to("/admin/login", "사용자 정보를 다시 확인해주세요.");
        }
        return requestForm(member, session, model);
    }

    /**
     * AS-IS {@code POST /opmanager/manager-request/pkiForm}·{@code pkiFormFin} - 인증서 로그인에서 관리자가 아닌 회원(ERR_MNG_NOT)이면
     * 화면이 DN을 POST한다. 정상(9) 회원 중 그 DN을 가진 사람을 찾는다(AS-IS checkSignForUser). 없으면 로그인 화면으로
     * "사용자 정보를 다시 확인해주세요. "(AS-IS 문구 끝 공백까지 그대로).
     */
    @PostMapping("/admin/manager-requests/pkiForm")
    public String formFromCert(@RequestParam(required = false) String mberDn, HttpSession session, Model model) {
        return formByDn(mberDn, null, session, model);
    }

    @PostMapping("/admin/manager-requests/pkiFormFin")
    public String formFromFinCert(@RequestParam(required = false) String mberFinDn, HttpSession session, Model model) {
        return formByDn(null, mberFinDn, session, model);
    }

    private String formByDn(String mberDn, String mberFinDn, HttpSession session, Model model) {
        boolean noDn = (mberDn == null || mberDn.isEmpty()) && (mberFinDn == null || mberFinDn.isEmpty());
        MemberClient.ManagerSyncInfo user = noDn ? null : memberClient.byDn(mberDn, mberFinDn);
        MemberClient.MemberInfo member = user != null && "9".equals(user.asIsStatusCode())
                ? memberClient.fetchOrNull(user.userId()) : null;
        if (member == null) {
            return flashRedirect.to("/admin/login", "사용자 정보를 다시 확인해주세요. ");
        }
        return requestForm(member, session, model);
    }

    /** AS-IS managerRequestForm 모델 - 신청자 정보 + 공통코드 5종(REQST_SE_CODE·WDR·OFF_BANK_LIST·PHONE·TEL). */
    private String requestForm(MemberClient.MemberInfo member, HttpSession session, Model model) {
        session.setAttribute(SESSION_REQUEST_USER_ID, member.userId());
        model.addAttribute("userId", member.userId());
        model.addAttribute("loginId", member.loginId());
        model.addAttribute("userName", member.userName());
        model.addAttribute("phoneNumber", member.phoneNumber());
        model.addAttribute("email", member.email());
        model.addAttribute("birthday", member.birthday());
        model.addAttribute("reqstSeCodeList", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("upperLocgovCodeList", commonCodeService.labelsOf("WDR"));
        model.addAttribute("bankCodeList", commonCodeService.labelsOf("OFF_BANK_LIST"));
        model.addAttribute("phoneCodeList", commonCodeService.labelsOf("PHONE"));
        model.addAttribute("telCodeList", commonCodeService.labelsOf("TEL"));
        model.addAttribute("frontendUrl", frontendUrl);
        return "admin/manager-request-form";
    }

    @org.springframework.beans.factory.annotation.Value("${ghlove.storefront.base-url:http://localhost:5173}")
    private String frontendUrl;

    /** AS-IS {@code POST /opmanager/manager-request/code-child/list}(비로그인) - 소속 지자체(시군구) 목록. */
    @PostMapping("/admin/manager-requests/code-child/list")
    @ResponseBody
    public Map<String, Object> codeChildList(@RequestParam String codeType) {
        List<Map<String, String>> list = commonCodeService.labelsOf(codeType).entrySet().stream()
                .map(e -> Map.of("id", e.getKey(), "label", e.getValue()))
                .toList();
        return Map.of("isSuccess", true, "data", list);
    }

    /** AS-IS {@code POST /opmanager/designated-donation/partList}(conditionType=REQUEST) - 지정기부 부서 목록. */
    @PostMapping("/admin/manager-requests/designated-part-list")
    @ResponseBody
    public Map<String, Object> designatedPartList(@RequestParam(required = false) String locgovCode) {
        List<Map<String, Object>> list = designatedProjectClient.departments(locgovCode).stream()
                .filter(d -> "Y".equals(d.useYn()))
                .map(d -> Map.<String, Object>of("dsgncntrPartId", d.deptId(), "dsgncntrPartName", d.deptNm()))
                .toList();
        return Map.of("isSuccess", true, "data", list);
    }

    /** AS-IS {@code POST /opmanager/welfareCenter/wlfrCntrMngList}(conditionType=REQUEST) - 행정복지센터 목록. */
    @PostMapping("/admin/manager-requests/wlfr-cntr-mng-list")
    @ResponseBody
    public Map<String, Object> wlfrCntrMngList(@RequestParam(required = false) String lclgvCd) {
        List<Map<String, Object>> list;
        try {
            list = welfareCenterAdminClient.list(lclgvCd).stream()
                    .map(w -> Map.<String, Object>of("pbadmsWlfrCntrId", w.id(), "pbadmsWlfrCntrNm", w.name()))
                    .toList();
        } catch (RuntimeException e) {
            list = List.of();
        }
        return Map.of("isSuccess", true, "data", list);
    }

    /**
     * AS-IS {@code POST /opmanager/manager-request/create}(비로그인) - 응답 data "SUCC"/"FAIL". AS-IS는 화면이 보낸 userId를
     * 그대로 쓰지만 TO-BE는 신청 폼을 열 때 확인한 회원(세션)만 받는다 - 남의 회원번호로 신청하는 것을 막는다.
     */
    @PostMapping("/admin/manager-requests/create")
    @ResponseBody
    public Map<String, Object> create(@RequestParam(required = false) String reqstSeCode,
                                      @RequestParam(required = false) String locgovCode,
                                      @RequestParam(required = false) String psitnCode,
                                      @RequestParam(required = false) String psitnNm,
                                      @RequestParam(required = false) String psitnDeptNm,
                                      @RequestParam(required = false) String ofcpsNm,
                                      @RequestParam(required = false) String cttpc,
                                      HttpSession session) {
        String result = "FAIL";
        Object sessionUserId = session.getAttribute(SESSION_REQUEST_USER_ID);
        MemberClient.MemberInfo member = sessionUserId instanceof Long id ? memberClient.fetchOrNull(id) : null;
        if (member != null) {
            try {
                managerRequestService.submit(member.userId(), member.loginId(), locgovCode, reqstSeCode, psitnCode,
                        psitnNm, psitnDeptNm, ofcpsNm, cttpc);
                session.removeAttribute(SESSION_REQUEST_USER_ID);
                result = "SUCC";
            } catch (RuntimeException e) {
                result = "FAIL";
            }
        }
        return Map.of("isSuccess", true, "data", result);
    }


    /**
     * 승인관리 목록(1406) - AS-IS user/manager-request/list.jsp 재현.
     * 검색구분(아이디/이름/이메일)·등록일 범위·상태(전체/대기200/승인100/거절300)로 걸러
     * 화면출력·페이징과 함께 보여준다. 상태는 상세 팝업 링크이고 "이력"은 신청이력 팝업이다.
     *
     * AS-IS는 보는 사람의 권한에 따라 컬럼을 감춘다: 시스템/행안부(ALL)만 "구분"을 보고,
     * 지자체 담당자(ROLE_ADMIN_6)는 "소속"을 보지 않는다. 그 판정값을 loginUserAuth로 내려준다.
     *
     * 예전 TO-BE는 대기 건만 뿌리고 검색·페이징·상태필터·상세/이력 팝업이 없었다.
     */
    @RequestMapping(value = "/admin/manager-requests", method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("searchParam") ManagerRequestParam searchParam,
                        @RequestParam(required = false) String errorMessage,
                        HttpSession session, HttpServletRequest request, Model model) {
        if (searchParam.getItemsPerPage() <= 0) {
            searchParam.setItemsPerPage(10);
        }
        List<ManagerRequest> found = managerRequestService.search(
                blankToNull(searchParam.getSrchConfmSttusCode()),
                blankToNull(searchParam.getSrchStartCreated()),
                blankToNull(searchParam.getSrchEndCreated()));

        Map<Long, MemberClient.MemberInfo> memberByUserId = new LinkedHashMap<>();
        for (ManagerRequest r : found) {
            memberByUserId.computeIfAbsent(r.getUserId(), memberClient::fetchOrNull);
        }

        // 아이디/이름/이메일 검색 - 이름·이메일은 member 서비스 값이라 여기서 거른다
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        List<ManagerRequest> all = found.stream()
                .filter(r -> visibleTo(r, viewer))
                .filter(r -> matchesKeyword(searchParam, r, memberByUserId.get(r.getUserId())))
                .toList();

        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);

        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        model.addAttribute("loginUserAuth", loginUserAuth(session));
        model.addAttribute("memberByUserId", memberByUserId);
        model.addAttribute("locgovNames", locgovNames());
        model.addAttribute("reqstSeCodeLabels", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("confmSttusLabels", CONFM_STTUS_LABELS);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("provinces", provinces());
        return "admin/manager-request-list";
    }

    /** AS-IS 상세 팝업(manager-request/popup/details/{userId}/{reqstSn}) - 600x650. */
    @GetMapping("/admin/manager-requests/popup/details/{userId}/{reqstSn}")
    public String detailsPopup(@PathVariable Long userId, @PathVariable Integer reqstSn, HttpSession session,
                               Model model) {
        // AS-IS getManagerRequestDetailsAuthCount - 볼 권한이 없는 신청이면 상세를 비워 둔다.
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        ManagerRequest details = managerRequestService.history(userId).stream()
                .filter(r -> reqstSn.equals(r.getReqstSn()))
                .filter(r -> visibleTo(r, viewer))
                .findFirst()
                .orElse(null);
        model.addAttribute("details", details);
        model.addAttribute("member", memberClient.fetchOrNull(userId));
        model.addAttribute("locgovNames", locgovNames());
        model.addAttribute("reqstSeCodeLabels", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("confmSttusLabels", CONFM_STTUS_LABELS);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        return "admin/manager-request-details";
    }

    /** AS-IS 이력 팝업 - 한 사용자의 신청 이력(최신순). */
    @GetMapping("/admin/manager-requests/popup/history/{userId}")
    public String historyPopup(@PathVariable Long userId, Model model) {
        // AS-IS getManagerRequestHistory는 ORDER BY LAST_UPDT_PNTTM DESC. 미처리(대기) 건은
        // lastUpdtPnttm이 비어 있어 뒤로 간다(CUBRID NULL=최소값, DESC면 마지막).
        var history = managerRequestService.history(userId).stream()
                .sorted(java.util.Comparator.comparing(
                        (ManagerRequest r) -> r.getLastUpdtPnttm() == null ? "" : r.getLastUpdtPnttm())
                        .reversed())
                .toList();
        model.addAttribute("list", history);
        model.addAttribute("reqstSeCodeLabels", commonCodeService.labelsOf("REQST_SE_CODE"));
        model.addAttribute("confmSttusLabels", CONFM_STTUS_LABELS);
        // AS-IS "작성자" = 변경한 관리자의 권한명 + (로그인아이디). AS-IS는 updId를 OP_USER_ROLE·OP_ROLE과
        // 조인해 얻지만, TO-BE는 G_MNGR_REQST.LAST_UPDUSR_ID(관리자 userId)로 op_manager를 찾아
        // authority→op_role.role_name + login_id로 구성한다. lastUpdusrId 하나당 한 번만 조회한다.
        Map<String, String> roleNames = roleRepository.findAllByOrderByRoleSeq().stream()
                .collect(java.util.stream.Collectors.toMap(com.ghlove.admin.domain.Role::getAuthority,
                        com.ghlove.admin.domain.Role::getRoleName, (a, b) -> a));
        Map<Long, String> updaterLabels = new LinkedHashMap<>();
        for (ManagerRequest r : history) {
            Long updId = r.getLastUpdusrId();
            if (updId == null || updaterLabels.containsKey(updId)) {
                continue;
            }
            managerRepository.findById(updId).ifPresent(m -> {
                String roleName = roleNames.getOrDefault(m.getAuthority(), m.getAuthority());
                updaterLabels.put(updId, roleName + " (" + m.getLoginId() + ")");
            });
        }
        model.addAttribute("updaterLabels", updaterLabels);
        return "admin/manager-request-history";
    }

    /** AS-IS CONFM_STTUS_CODE - 200=대기 100=승인 300=거절. */
    private static final Map<String, String> CONFM_STTUS_LABELS =
            Map.of("200", "대기", "100", "승인", "300", "거절");

    /** AS-IS가 화면에 내려주는 접근권한 판정값 - 'ALL'(시스템/행안부) / ROLE_ADMIN_6 / ROLE_ADMIN_8. */
    private String loginUserAuth(HttpSession session) {
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (viewer == null) {
            return "ALL";
        }
        String authority = viewer.getAuthority();
        if (MenuService.UNRESTRICTED_ROLES.contains(authority)) {
            return "ALL";
        }
        if ("ROLE_ADMIN_5".equals(authority) || "ROLE_ADMIN_6".equals(authority)) {
            return "ROLE_ADMIN_6";
        }
        if ("ROLE_ADMIN_7".equals(authority) || "ROLE_ADMIN_8".equals(authority)) {
            return "ROLE_ADMIN_8";
        }
        return "ALL";
    }

    /**
     * AS-IS {@code ManagerRequestServiceImpl.getReqstSeCode} + 목록 WHERE - 보는 사람별 승인 대상:
     * 시스템주(1)·행안부주(3)는 전체, 지자체주(5)는 <b>자기 지자체의 지자체부담당자(6) 신청만</b>, 오프라인주(7)는 오프라인부(8) 신청만,
     * 그 외(행안부부 4 등 메뉴 권한은 있어도)는 아무것도 보지 못한다.
     */
    private static boolean visibleTo(ManagerRequest r, Manager viewer) {
        String authority = viewer != null ? viewer.getAuthority() : null;
        if ("ROLE_ADMIN_1".equals(authority) || "ROLE_ADMIN_3".equals(authority)) {
            return true;
        }
        if ("ROLE_ADMIN_5".equals(authority)) {
            return "ROLE_ADMIN_6".equals(r.getReqstSeCode()) && viewer.getLocgovCode() != null
                    && viewer.getLocgovCode().equals(r.getLocgovCode());
        }
        if ("ROLE_ADMIN_7".equals(authority)) {
            return "ROLE_ADMIN_8".equals(r.getReqstSeCode());
        }
        return false;
    }

    private boolean matchesKeyword(ManagerRequestParam param, ManagerRequest request, MemberClient.MemberInfo member) {
        String keyword = param.getSrchValue();
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String trimmed = keyword.trim();
        return switch (param.getSrchKey() == null ? "LOGIN_ID" : param.getSrchKey()) {
            case "USER_NAME" -> member != null && member.userName() != null && member.userName().contains(trimmed);
            case "EMAIL" -> member != null && member.email() != null && member.email().contains(trimmed);
            default -> request.getLoginId() != null && request.getLoginId().contains(trimmed);
        };
    }

    /** locgovCode → "시도 시군구" (목록·상세의 소속 지자체 표시). */
    private Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> names.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm()));
        return names;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /**
     * AS-IS {@code POST /opmanager/manager-request/confirm} 이식 - 승인/거절을 한 엔드포인트가
     * 처리하고 JSON을 돌려준다(팝업이 AJAX로 호출 → 성공 시 opener 새로고침 + self.close).
     * confmSttusCode: 100=승인 / 300=거절. 거절이면 rejectResn 필수.
     * 응답: {@code {isSuccess:true, data:{code:"SUCC"|"ERR", message?}}}.
     */
    @PostMapping("/admin/manager-requests/confirm")
    @ResponseBody
    public Map<String, Object> confirm(@RequestParam String confmSttusCode,
                                       @RequestParam Long userId,
                                       @RequestParam Integer reqstSn,
                                       @RequestParam(required = false) String rejectResn,
                                       HttpSession session) {
        Manager approver = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        Long approverId = approver != null ? approver.getUserId() : null;
        MemberClient.MemberInfo member = memberClient.fetchOrNull(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        // 목록·상세와 같은 범위만 처리한다(AS-IS confirm은 범위를 다시 보지 않지만, 상세를 못 여는 신청을 주소로 직접 승인하는 걸 막는다).
        boolean visible = managerRequestService.history(userId).stream()
                .anyMatch(r -> reqstSn.equals(r.getReqstSn()) && visibleTo(r, approver));
        if (!visible) {
            data.put("code", "ERR");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("isSuccess", true);
            body.put("data", data);
            return body;
        }
        try {
            if ("300".equals(confmSttusCode)) {
                managerRequestService.reject(userId, reqstSn, approverId, rejectResn, member);
                data.put("code", "SUCC");
            } else if ("100".equals(confmSttusCode)) {
                managerRequestService.approve(userId, reqstSn, approverId, member);
                data.put("code", "SUCC");
            } else {
                data.put("code", "ERR");
                data.put("message", "상태값이 올바르지 않습니다.");
            }
        } catch (ManagerRequestService.ApprovalException e) {
            data.put("code", e.code);
            if (e.statusCode != null) {
                data.put("statusCode", e.statusCode);
            }
        } catch (ManagerException e) {
            data.put("code", "ERR");
            data.put("message", e.getMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", true);
        body.put("data", data);
        return body;
    }

    /** upperLocgovCode/upperLocgovNm만 중복 없이 뽑는다 (기존 ReceiptController의 시/도
     *  드롭다운 구성 패턴과 동일). */
    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}
