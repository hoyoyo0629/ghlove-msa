package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.service.AuditLogService;
import com.ghlove.admin.service.ManagerAuthService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.CertLoginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 관리자(운영자) 로그인 - AS-IS OP_MANAGER 기반 2단계(아이디/비번 → 이메일 인증번호) 로그인.
 *  /auth/cert-login(지자체 담당자용)과는 별개의, 운영관리 콘솔 접근용 계정 체계다. */
@Controller
@RequiredArgsConstructor
public class ManagerAuthController {

    public static final String SESSION_MANAGER_KEY = "loginManager";
    private static final String SESSION_PENDING_LOGIN_ID_KEY = "pendingManagerLoginId";

    private final ManagerAuthService managerAuthService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final MenuService menuService;
    private final AuditLogService auditLogService;
    private final com.ghlove.admin.service.MemberAdminClient memberAdminClient;
    private final com.ghlove.admin.service.SmsIpsService smsIpsService;
    private final com.ghlove.admin.service.MemberClient memberClient;

    /** 대민 사이트 주소 - 계정 잠김 화면의 "비밀번호 찾기 바로가기"(AS-IS SalesonProperty.getSalesonUrlFrontend()). */
    @org.springframework.beans.factory.annotation.Value("${ghlove.storefront.base-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * AS-IS {@code /opmanager/login-main}(login_main.jsp). AS-IS {@code /opmanager/login}은 대기열(NetFunnel) 확인 뒤
     * 이 화면으로 보내는 중간 페이지라 TO-BE는 바로 이 화면을 연다. {@code error}는 AS-IS {@code ${param.error}}
     * (4 잠김 / 5 비밀번호 임시·만료 / 98 중복로그인 / 99 세션종료 / 6 권한중지)를 화면 JS가 그대로 읽는다.
     */
    @GetMapping("/admin/login")
    public String form(@RequestParam(required = false) String returnTo, Model model) {
        model.addAttribute("returnTo", returnTo);
        model.addAttribute("emailBypass", managerAuthService.emailBypass());
        return "admin/login";
    }

    /** AS-IS {@code /opmanager/login-lock}(main/login-lock.jsp) - 계정 잠김 해지 안내. */
    @GetMapping("/admin/login-lock")
    public String loginLock(Model model) {
        model.addAttribute("frontendUrl", frontendUrl);
        return "admin/login-lock";
    }

    /**
     * 아이디 로그인 1단계 - AS-IS {@code /opmanager/user/login-user-valid}. 응답 {@code data.code}는 AS-IS 결과코드
     * 그대로다({@link ManagerAuthService#validateLogin}). SUCC면 2단계(이메일 인증번호) 대상을 세션에 담는다.
     * AS-IS처럼 비밀번호 불일치(ERR_MNG_PASS)는 로그인 실패 이력을 남긴다.
     */
    @PostMapping("/admin/login/check")
    @ResponseBody
    public Map<String, Object> check(@RequestParam String loginId, @RequestParam String password,
                                      HttpSession session, HttpServletRequest request) {
        ManagerAuthService.LoginValidResult valid = managerAuthService.validateLogin(loginId, password);
        session.removeAttribute(SESSION_PENDING_LOGIN_ID_KEY);
        // AS-IS 화면은 SUCC와 PASS_CHANGE_C(3개월 변경 안내)일 때 이메일 인증 단계로 간다 - 비밀번호 변경은 인증 뒤에 묻는다.
        if ("SUCC".equals(valid.code()) || "PASS_CHANGE_C".equals(valid.code())) {
            session.setAttribute(SESSION_PENDING_LOGIN_ID_KEY, loginId);
        }
        if ("ERR_MNG_PASS".equals(valid.code()) || valid.code().startsWith("PASS_CHANGE_")) {
            auditLogService.recordLogin(loginId, false, request.getRemoteAddr(), valid.code());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", valid.code());
        // EMPTY_CI면 화면이 대민 회원정보 수정으로 보낸다(AS-IS hidden #frontendUrl).
        data.put("frontendUrl", frontendUrl);
        return data(data);
    }

    /**
     * 아이디 로그인 2단계 - AS-IS {@code /opmanager/user/login-user-email}. 이메일이 없으면 {@code NOEMAIL}, 있으면
     * 인증번호를 발송하고 {@code "SUCC" + 이메일}(화면이 앞 3자리만 남기고 가린다).
     * 이메일 연계가 꺼진 로컬에서는 발송한 인증번호를 {@code devCode}로 같이 내려 화면이 자동으로 넣고 로그인한다
     * (AS-IS 로컬 우회 {@code IS_LOCAL_DEV}와 같은 방식 - AS-IS는 localhost면 인증번호를 250324로 고정한다).
     */
    @PostMapping("/admin/login/email")
    @ResponseBody
    public Map<String, Object> email(HttpSession session) {
        String loginId = (String) session.getAttribute(SESSION_PENDING_LOGIN_ID_KEY);
        Manager manager = loginId == null ? null : managerAuthService.findByLoginId(loginId);
        if (manager == null) {
            return data("FAIL");
        }
        if (manager.getEmail() == null || manager.getEmail().isBlank()) {
            return data("NOEMAIL");
        }
        Map<String, Object> body = data("SUCC" + manager.getEmail());
        putDevCode(body, managerAuthService.sendAuthCode(manager));
        return body;
    }

    /** 이메일 미등록 계정 - AS-IS {@code /opmanager/user/login-user-email-save}: 이메일을 저장하고 인증번호를 보낸다. */
    @PostMapping("/admin/login/save-email")
    @ResponseBody
    public Map<String, Object> saveEmail(HttpSession session, @RequestParam String email) {
        String loginId = (String) session.getAttribute(SESSION_PENDING_LOGIN_ID_KEY);
        if (loginId == null || email == null || email.isBlank()) {
            return data("FAIL");
        }
        try {
            Map<String, Object> body = data("SUCC");
            putDevCode(body, managerAuthService.saveEmailAndSendCode(loginId, email));
            return body;
        } catch (ManagerException e) {
            return data("FAIL");
        }
    }

    /**
     * 아이디 로그인 3단계 - AS-IS {@code /opmanager/user/login-user-auth-check}. 인증번호가 맞으면 세션을 확정하고
     * ({@code setSessionByManager}) {@code SUCC}, 틀리거나 만료면 {@code FAIL}.
     */
    @PostMapping("/admin/login/verify")
    @ResponseBody
    public Map<String, Object> verify(HttpSession session, @RequestParam String code,
                                       @RequestParam(required = false) String returnTo, HttpServletRequest request) {
        String loginId = (String) session.getAttribute(SESSION_PENDING_LOGIN_ID_KEY);
        if (loginId == null) {
            return data("FAIL");
        }
        try {
            Manager manager = managerAuthService.verifyAuthCode(loginId, code);
            // AS-IS: 인증번호가 맞으면 getLoginUserValid를 다시 돌려 PASS_CHANGE_C면 세션 없이 그 코드를 돌려준다(화면이 변경 팝업).
            if ("PASS_CHANGE_C".equals(managerAuthService.passwordChangeType(manager))) {
                return data("PASS_CHANGE_C");
            }
            session.removeAttribute(SESSION_PENDING_LOGIN_ID_KEY);
            session.setAttribute(SESSION_MANAGER_KEY, manager);
            // AS-IS setSessionByManager: 로그인 직후 표시 - 메인화면이 첫 진입에 오프라인 담당자 안내를 띄우고 지운다.
            session.setAttribute(AdminHomeController.SESSION_LOGIN_NOW, "Y");
            auditLogService.recordLogin(loginId, true, request.getRemoteAddr(), null);
            sendManagerLoginSms(manager);
            Map<String, Object> body = data("SUCC");
            body.put("redirect", returnTo != null && !returnTo.isBlank() ? returnTo : defaultLandingPage(manager));
            return body;
        } catch (ManagerException e) {
            auditLogService.recordLogin(loginId, false, request.getRemoteAddr(), e.getMessage());
            return data("FAIL");
        }
    }

    /**
     * 비밀번호 변경 팝업 - AS-IS {@code /opmanager/user/popup/temp-password-change}(user/popup/temp-password-change.jsp).
     * pageType: I 관리자 비밀번호 초기화 / C 관리자 비밀번호 변경(3개월) / F 초기 비밀번호 설정.
     */
    @PostMapping("/admin/login/popup/temp-password-change")
    public String tempPasswordChangePopup(@RequestParam String pageType, @RequestParam String loginId, Model model) {
        model.addAttribute("pageType", pageType);
        model.addAttribute("loginId", loginId);
        return "admin/temp-password-change";
    }

    /** 비밀번호 변경 저장 - AS-IS {@code /opmanager/user/temp-password-change}. 응답 data는 AS-IS 결과코드 그대로. */
    @PostMapping("/admin/login/temp-password-change")
    @ResponseBody
    public Map<String, Object> tempPasswordChange(@RequestParam String loginId, @RequestParam String password,
                                                  @RequestParam String currentPassword) {
        String code;
        try {
            code = managerAuthService.changeTempPassword(loginId, currentPassword, password);
        } catch (RuntimeException e) {
            code = "FAIL";
        }
        Map<String, Object> body = data(code);
        body.put("isSuccess", true);
        return body;
    }

    /**
     * AS-IS {@code setSessionByManager} 6단계 "관리자 로그인 국민비서 발송"(MANAGER_LOGIN, 812-A024). 관리자의 회원정보
     * (같은 USER_ID)로 보내고 수신동의는 보지 않는다(AS-IS가 receiveSms를 "0"으로 넣는다). 회원이 없거나 실패해도 로그인은
     * 그대로 진행한다(AS-IS도 예외를 잡고 넘어간다).
     */
    private void sendManagerLoginSms(Manager manager) {
        try {
            com.ghlove.admin.service.MemberAdminClient.SmsReceiver u = memberAdminClient.smsReceiver(manager.getUserId());
            if (u != null) {
                smsIpsService.send(com.ghlove.admin.service.SmsType.MANAGER_LOGIN,
                        new com.ghlove.admin.service.MemberAdminClient.SmsReceiver(u.userId(), u.userName(), u.phoneNumber(), "0", u.mberCi()));
            }
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(ManagerAuthController.class)
                    .error("setSessionByManager 국민비서 발송 실패 : {}", manager.getUserId(), e);
        }
    }

    private static Map<String, Object> data(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data", data);
        return body;
    }

    private static void putDevCode(Map<String, Object> body, String devCode) {
        if (devCode != null) {
            body.put("devCode", devCode);
        }
    }


    /**
     * 인증서 로그인 - AS-IS {@code /opmanager/user/pki/login-user-valid}. AS-IS는 MagicLine4Web(공동인증서)·금융인증서 SDK가
     * 서명값에서 DN을 꺼내 보내지만, 로컬엔 SDK·정부 PKI 연계가 없어 인증서 파일을 받아 서버가 X.509 DN을 읽는다.
     * certType P(공동인증서)면 MBER_DN, F(금융인증서)면 MBER_FIN_DN으로 찾는다. 응답 data는 AS-IS 결과
     * ({@link ManagerAuthService#pkiLogin}) 그대로이고 SUCC면 세션을 만들고 로그인 문자를 보낸다.
     */
    @PostMapping("/admin/login/cert")
    @ResponseBody
    public Map<String, Object> certLogin(@RequestParam MultipartFile certFile,
                                          @RequestParam(required = false) String certType,
                                          @RequestParam(required = false) String returnTo,
                                          HttpSession session, HttpServletRequest request) {
        String dn;
        try {
            dn = managerAuthService.parseCertificate(certFile).getSubjectX500Principal().getName();
        } catch (CertLoginException e) {
            Map<String, Object> body = data(Map.of("code", "ERR_DN"));
            body.put("isSuccess", true);
            return body;
        }
        boolean fin = "F".equals(certType);
        ManagerAuthService.PkiLoginResult result = managerAuthService.pkiLogin(fin ? null : dn, fin ? dn : null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", result.code());
        if (result.statusCode() != null) {
            data.put("statusCode", result.statusCode());
        }
        if (result.mberDn() != null) {
            data.put("mberDn", result.mberDn());
        }
        if (result.mberFinDn() != null) {
            data.put("mberFinDn", result.mberFinDn());
        }
        Map<String, Object> body = data(data);
        body.put("isSuccess", true);
        if ("SUCC".equals(result.code())) {
            Manager manager = result.manager();
            session.setAttribute(SESSION_MANAGER_KEY, manager);
            session.setAttribute(AdminHomeController.SESSION_LOGIN_NOW, "Y");
            auditLogService.recordLogin(manager.getLoginId(), true, request.getRemoteAddr(), "인증서 로그인");
            sendManagerLoginSms(manager);
            body.put("redirect", returnTo != null && !returnTo.isBlank() ? returnTo : defaultLandingPage(manager));
        }
        return body;
    }

    /** 인증서 등록 화면에서 본인 확인을 통과한 회원번호 - 등록·삭제는 이 회원에게만 한다. */
    private static final String SESSION_PKI_USER_ID = "pkiFormUserId";
    /** 인증서 등록 화면에서 읽은 인증서 DN - 등록은 이 값만 받는다. */
    private static final String SESSION_PKI_DN = "pkiFormDn";

    /** AS-IS {@code GET /opmanager/user/pki/form}(user/pki/form.jsp) - 로그인 전 인증서 등록/삭제. */
    @GetMapping("/admin/user/pki/form")
    public String pkiForm() {
        return "admin/pki-form";
    }

    /** AS-IS {@code GET /opmanager/user/pki/form/valid} - 본인 확인. 응답 data는 AS-IS PkiManagerLoginResult 모양. */
    @GetMapping("/admin/user/pki/form/valid")
    @ResponseBody
    public Map<String, Object> pkiFormValid(@RequestParam(required = false) String loginId,
                                            @RequestParam(required = false) String phoneNumber,
                                            @RequestParam(required = false) String birthday,
                                            @RequestParam(required = false) String offEmpId,
                                            @RequestParam(required = false) String charge,
                                            HttpSession session) {
        ManagerAuthService.PkiFormValidResult result;
        try {
            result = managerAuthService.pkiFormValid(charge, loginId, phoneNumber, birthday, offEmpId);
        } catch (RuntimeException e) {
            result = new ManagerAuthService.PkiFormValidResult("FAIL", null, null, null, null);
        }
        session.removeAttribute(SESSION_PKI_USER_ID);
        if ("SUCC".equals(result.code())) {
            session.setAttribute(SESSION_PKI_USER_ID, result.userId());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", result.code());
        data.put("userId", result.userId());
        data.put("mberDn", result.mberDn());
        data.put("mberFinDn", result.mberFinDn());
        data.put("mberCi", result.mberCi());
        Map<String, Object> body = data(data);
        body.put("isSuccess", true);
        return body;
    }

    /**
     * 인증서 DN 읽기 - AS-IS는 MagicLine(공동인증서, signedFormRGhlove 콜백 {@code {result:"SUCCESS", DN}})·금융인증서 SDK
     * ({@code {dn}})가 DN을 돌려준다. 로컬엔 SDK가 없어 인증서 파일을 받아 서버가 읽고 같은 모양으로 돌려준다.
     */
    @PostMapping("/admin/user/pki/cert-dn")
    @ResponseBody
    public Map<String, Object> pkiCertDn(@RequestParam MultipartFile certFile, HttpSession session) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            String dn = managerAuthService.parseCertificate(certFile).getSubjectX500Principal().getName();
            session.setAttribute(SESSION_PKI_DN, dn);
            body.put("result", "SUCCESS");
            body.put("DN", dn);
            body.put("dn", dn);
        } catch (CertLoginException e) {
            body.put("result", "FAIL");
        }
        return body;
    }

    /**
     * AS-IS {@code POST /opmanager/user/pki/update} - 회원에 인증서 DN 등록. AS-IS는 화면이 보낸 회원번호·DN을 그대로 믿지만
     * TO-BE는 본인 확인을 통과한 회원과 방금 읽은 인증서 DN만 받는다(남의 계정에 인증서를 다는 것을 막는다).
     */
    @PostMapping("/admin/user/pki/update")
    @ResponseBody
    public Map<String, Object> pkiUpdate(@RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String mberDn,
                                         @RequestParam(required = false) String mberFinDn,
                                         HttpSession session) {
        String code = "FAIL";
        Object verified = session.getAttribute(SESSION_PKI_USER_ID);
        Object readDn = session.getAttribute(SESSION_PKI_DN);
        boolean dnOk = readDn != null && (readDn.equals(mberDn) || readDn.equals(mberFinDn));
        if (userId != null && userId.equals(verified) && dnOk) {
            code = memberClient.updateDn(userId, mberDn, mberFinDn);
            session.removeAttribute(SESSION_PKI_DN);
        }
        Map<String, Object> body = data(Map.of("code", code));
        body.put("isSuccess", true);
        return body;
    }

    /** AS-IS {@code POST /opmanager/user/pki/delete} - 회원 인증서 DN 삭제(종류 P/F). 본인 확인을 통과한 회원만. */
    @PostMapping("/admin/user/pki/delete")
    @ResponseBody
    public Map<String, Object> pkiDelete(@RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String mberDn,
                                         @RequestParam(required = false) String mberFinDn,
                                         HttpSession session) {
        String code = "FAIL";
        if (userId != null && userId.equals(session.getAttribute(SESSION_PKI_USER_ID))) {
            code = memberClient.deleteDn(userId, type, mberDn, mberFinDn);
        }
        Map<String, Object> body = data(Map.of("code", code));
        body.put("isSuccess", true);
        return body;
    }


    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SESSION_MANAGER_KEY);
        return "redirect:/admin/login";
    }

    /**
     * AS-IS {@code op.manager.js}의 {@code Manager.procSessionTimeout}이 자동 로그아웃 시
     * {@code location.href='/op_security_logout?target=/opmanager'}로 이동한다 - 복사해 온
     * 자산이라 이 URL이 그대로 호출되는데 TO-BE에는 없어서 404가 됐다. 자산을 고치지 않고
     * 엔드포인트를 맞춘다(세션 정리 후 로그인 화면으로).
     */
    @GetMapping("/op_security_logout")
    public String securityLogout(HttpSession session) {
        session.removeAttribute(SESSION_MANAGER_KEY);
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/access-denied")
    public String accessDenied(HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(SESSION_MANAGER_KEY);
        model.addAttribute("homeUrl", manager != null ? defaultLandingPage(manager) : "/admin/login");
        return "admin/access-denied";
    }

    /** 로그인 직후 랜딩 화면 - AS-IS처럼 관리자 메인(/admin, AdminHomeController = AS-IS main/index.jsp).
     *  권한별로 보이는 위젯이 다를 뿐 모든 로그인 관리자가 같은 주소로 간다. */
    private String defaultLandingPage(Manager manager) {
        return "/admin";
    }

    private static Map<String, Object> result(String status, String message, String devCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        if (message != null) {
            body.put("message", message);
        }
        if (devCode != null) {
            body.put("devCode", devCode);
        }
        return body;
    }
}
