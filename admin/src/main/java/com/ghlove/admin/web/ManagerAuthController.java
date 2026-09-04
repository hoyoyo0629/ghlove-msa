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
    private final MenuService menuService;
    private final AuditLogService auditLogService;

    @GetMapping("/admin/login")
    public String form(@RequestParam(required = false) String returnTo, Model model) {
        model.addAttribute("returnTo", returnTo);
        return "admin/login";
    }

    /** 1단계: 아이디/비번 확인. 통과하면 세션에 loginId만 임시로 담아두고(2단계 대상 식별용),
     *  이메일이 있으면 바로 인증번호를 보낸다. */
    @PostMapping("/admin/login/check")
    @ResponseBody
    public Map<String, Object> check(@RequestParam String loginId, @RequestParam String password,
                                      HttpSession session, HttpServletRequest request) {
        try {
            Manager manager = managerAuthService.checkCredentials(loginId, password);
            session.setAttribute(SESSION_PENDING_LOGIN_ID_KEY, loginId);
            if (manager.getEmail() == null || manager.getEmail().isBlank()) {
                return result("NEED_EMAIL", null, null);
            }
            String devCode = managerAuthService.sendAuthCode(manager);
            String maskedEmail = maskEmail(manager.getEmail());
            return result("CODE_SENT", maskedEmail + " (으)로 인증번호를 발송했습니다.", devCode);
        } catch (ManagerException e) {
            auditLogService.recordLogin(loginId, false, request.getRemoteAddr(), e.getMessage());
            return result("ERROR", e.getMessage(), null);
        }
    }

    /** 이메일이 등록돼 있지 않은 계정이 최초 인증 시 이메일을 등록하면서 인증번호를 받는다. */
    @PostMapping("/admin/login/save-email")
    @ResponseBody
    public Map<String, Object> saveEmail(HttpSession session, @RequestParam String email) {
        String loginId = (String) session.getAttribute(SESSION_PENDING_LOGIN_ID_KEY);
        if (loginId == null) {
            return result("ERROR", "다시 로그인해 주세요.", null);
        }
        try {
            String devCode = managerAuthService.saveEmailAndSendCode(loginId, email);
            return result("CODE_SENT", maskEmail(email) + " (으)로 인증번호를 발송했습니다.", devCode);
        } catch (ManagerException e) {
            return result("ERROR", e.getMessage(), null);
        }
    }

    /** 3단계: 인증번호 확인 - 통과해야 실제 세션이 확정된다. */
    @PostMapping("/admin/login/verify")
    @ResponseBody
    public Map<String, Object> verify(HttpSession session, @RequestParam String code,
                                       @RequestParam(required = false) String returnTo, HttpServletRequest request) {
        String loginId = (String) session.getAttribute(SESSION_PENDING_LOGIN_ID_KEY);
        if (loginId == null) {
            return result("ERROR", "다시 로그인해 주세요.", null);
        }
        try {
            Manager manager = managerAuthService.verifyAuthCode(loginId, code);
            session.removeAttribute(SESSION_PENDING_LOGIN_ID_KEY);
            session.setAttribute(SESSION_MANAGER_KEY, manager);
            auditLogService.recordLogin(loginId, true, request.getRemoteAddr(), null);
            Map<String, Object> body = result("OK", null, null);
            body.put("redirect", returnTo != null && !returnTo.isBlank() ? returnTo : defaultLandingPage(manager));
            return body;
        } catch (ManagerException e) {
            auditLogService.recordLogin(loginId, false, request.getRemoteAddr(), e.getMessage());
            return result("ERROR", e.getMessage(), null);
        }
    }

    /** 인증서(금융/공동인증서) 로그인 - AS-IS MagicLine4Web SDK 대신 인증서 파일 업로드로
     *  Subject DN을 얻는다(2단계 참고). 인증서 자체가 2차 인증이라 이메일 인증번호는 생략. */
    @PostMapping("/admin/login/cert")
    @ResponseBody
    public Map<String, Object> certLogin(@RequestParam MultipartFile certFile,
                                          @RequestParam(required = false) String returnTo,
                                          HttpSession session, HttpServletRequest request) {
        try {
            X509Certificate cert = managerAuthService.parseCertificate(certFile);
            Manager manager = managerAuthService.loginByCertificate(cert);
            session.setAttribute(SESSION_MANAGER_KEY, manager);
            auditLogService.recordLogin(manager.getLoginId(), true, request.getRemoteAddr(), "인증서 로그인");
            Map<String, Object> body = result("OK", null, null);
            body.put("redirect", returnTo != null && !returnTo.isBlank() ? returnTo : defaultLandingPage(manager));
            return body;
        } catch (CertLoginException | ManagerException e) {
            return result("ERROR", e.getMessage(), null);
        }
    }

    /** 인증서 등록 - 이미 로그인한 관리자만. 로그인 화면의 "인증서 등록/삭제"는 여기로 안내한다. */
    @GetMapping("/admin/my-cert")
    public String myCertForm(HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(SESSION_MANAGER_KEY);
        model.addAttribute("manager", manager);
        return "admin/my-cert";
    }

    @PostMapping("/admin/my-cert/register")
    public String registerCert(HttpSession session, @RequestParam MultipartFile certFile, Model model) {
        Manager manager = (Manager) session.getAttribute(SESSION_MANAGER_KEY);
        try {
            X509Certificate cert = managerAuthService.parseCertificate(certFile);
            managerAuthService.registerCertificate(manager.getUserId(), cert);
            return "redirect:/admin/my-cert?message=" + encode("인증서가 등록되었습니다.");
        } catch (CertLoginException | ManagerException e) {
            return "redirect:/admin/my-cert?errorMessage=" + encode(e.getMessage());
        }
    }

    @PostMapping("/admin/my-cert/unregister")
    public String unregisterCert(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(SESSION_MANAGER_KEY);
        managerAuthService.unregisterCertificate(manager.getUserId());
        return "redirect:/admin/my-cert?message=" + encode("인증서 등록이 해제되었습니다.");
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SESSION_MANAGER_KEY);
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/access-denied")
    public String accessDenied(HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(SESSION_MANAGER_KEY);
        model.addAttribute("homeUrl", manager != null ? defaultLandingPage(manager) : "/admin/login");
        return "admin/access-denied";
    }

    /** 로그인 직후 랜딩 화면. AdminHomeController(/admin)가 실제 대시보드(오늘 주문/회원
     *  카운트+최근 공지)를 보여주고 위젯 접근권한과 무관하게 모든 로그인 관리자가 볼 수
     *  있다 - 예전엔 이 대시보드 자체가 없어서 "접근 가능한 첫 메뉴"로 그냥 넘겨버렸는데,
     *  그러면 지자체담당자(ROLE_ADMIN_5/6)가 로그인할 때마다 자기 업무와 무관한 메뉴에
     *  떨어지는 문제가 있었다. */
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

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        int visible = Math.min(3, at);
        return email.substring(0, visible) + "***" + email.substring(at);
    }
}
