package com.ghlove.admin.web;

import com.ghlove.admin.service.CertLoginException;
import com.ghlove.admin.service.CertLoginService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.cert.X509Certificate;

/** 매직라인4웹 PKI 인증서 로그인 (지자체 담당자 인증). */
@Controller
@RequiredArgsConstructor
public class CertLoginController {

    public static final String SESSION_OFFICER_KEY = "loginOfficer";

    private final CertLoginService certLoginService;

    @GetMapping("/auth/cert-login")
    public String form() {
        return "auth/cert-login";
    }

    @PostMapping("/auth/cert-login")
    public String login(@RequestParam("certFile") MultipartFile certFile, HttpSession session, Model model) {
        try {
            X509Certificate cert = certLoginService.parseCertificate(certFile);
            var officer = certLoginService.authenticate(cert);
            session.setAttribute(SESSION_OFFICER_KEY, officer);
            model.addAttribute("officer", officer);
            return "auth/cert-login-success";
        } catch (CertLoginException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/cert-login";
        }
    }
}
