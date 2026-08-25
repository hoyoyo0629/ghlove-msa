package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.ManagerLoginEmail;
import com.ghlove.admin.repository.ManagerLoginEmailRepository;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.service.integration.EmailClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 관리자(운영자) 로그인 - AS-IS OP_MANAGER + OP_MANAGER_LOGIN_EMAIL 기반. AS-IS(opmanager/
 * user/login_main.jsp)와 동일하게 2단계다: (1) 아이디/비번 확인만으로는 세션이 서지 않고,
 * (2) 이메일로 받은 6자리 인증번호(5분 유효)까지 맞아야 최종 로그인된다. 실패 5회 시
 * 계정 잠금은 member의 MemberService.login()과 동일한 로직.
 */
@Service
@RequiredArgsConstructor
public class ManagerAuthService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_LOCKED = "LOCKED";
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int AUTH_CODE_VALID_MINUTES = 5;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ManagerRepository managerRepository;
    private final ManagerLoginEmailRepository managerLoginEmailRepository;
    private final CommonCodeService commonCodeService;
    private final PasswordEncoder passwordEncoder;
    private final EmailClient emailClient;
    private final CertLoginService certLoginService;

    /** 1단계: 아이디/비번 확인. 통과해도 아직 로그인은 아니다(이메일 인증 전까지 세션 미확정). */
    @Transactional(noRollbackFor = ManagerException.class)
    public Manager checkCredentials(String loginId, String rawPassword) {
        Manager manager = managerRepository.findByLoginId(loginId).orElse(null);
        if (manager == null) {
            throw new ManagerException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!STATUS_ACTIVE.equals(manager.getStatusCode())) {
            throw new ManagerException(statusBlockedMessage(manager.getStatusCode()));
        }
        if (!passwordEncoder.matches(rawPassword, manager.getPassword())) {
            int failCount = (manager.getLoginFailCount() == null ? 0 : manager.getLoginFailCount()) + 1;
            manager.setLoginFailCount(failCount);
            manager.setLoginTryDate(now());
            boolean justLocked = failCount >= MAX_LOGIN_FAIL_COUNT;
            if (justLocked) {
                manager.setStatusCode(STATUS_LOCKED);
            }
            managerRepository.save(manager);
            if (justLocked) {
                throw new ManagerException("로그인 실패 횟수를 초과하여 계정이 잠겼습니다. 시스템 관리자에게 문의하세요.");
            }
            throw new ManagerException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return manager;
    }

    /** 2단계: 인증번호 생성+발송. EmailClient가 모크(enabled=false)면 개발 편의상 코드를 그대로 반환한다. */
    @Transactional
    public String sendAuthCode(Manager manager) {
        String code = generateCode();
        ManagerLoginEmail row = new ManagerLoginEmail();
        row.setLoginId(manager.getLoginId());
        row.setAuthNum(code);
        row.setCreatedAt(LocalDateTime.now());
        managerLoginEmailRepository.save(row);
        emailClient.sendAuthCode(manager.getEmail(), code);
        return emailClient.enabled ? null : code;
    }

    /** 이메일이 없는 계정이 최초 인증 시 이메일을 등록하면서 바로 인증번호를 받는다. */
    @Transactional
    public String saveEmailAndSendCode(String loginId, String email) {
        Manager manager = managerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ManagerException("계정을 찾을 수 없습니다."));
        manager.setEmail(email);
        managerRepository.save(manager);
        return sendAuthCode(manager);
    }

    /** 3단계: 인증번호 확인 - 통과해야 실제 로그인(횟수/일시 갱신)이 완료된다. */
    @Transactional
    public Manager verifyAuthCode(String loginId, String inputCode) {
        ManagerLoginEmail row = managerLoginEmailRepository.findFirstByLoginIdOrderByCreatedAtDesc(loginId)
                .orElseThrow(() -> new ManagerException("인증번호를 먼저 요청해 주세요."));
        if (row.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(AUTH_CODE_VALID_MINUTES))) {
            throw new ManagerException("인증 시간이 초과되었습니다. 다시 시도해 주세요.");
        }
        if (inputCode == null || !inputCode.trim().equals(row.getAuthNum())) {
            throw new ManagerException("인증번호가 일치하지 않습니다.");
        }
        Manager manager = managerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ManagerException("계정을 찾을 수 없습니다."));
        manager.setLoginFailCount(0);
        manager.setLoginCount((manager.getLoginCount() == null ? 0 : manager.getLoginCount()) + 1);
        manager.setLoginDate(now());
        return managerRepository.save(manager);
    }

    /**
     * 인증서(금융/공동인증서) 로그인 - AS-IS login_main.jsp의 doSignData()/doFinanceSignData()가
     * MagicLine4Web 브라우저 SDK로 서명 데이터를 만들어 DN을 얻는 대신, CertLoginService(지자체
     * 담당자용)와 동일하게 인증서 파일을 업로드받아 로컬에서 X.509를 직접 파싱해 DN을 얻는다 -
     * SDK 없이도 실제로 동작하는 부분. 인증서 자체가 2차 인증 역할이라 이메일 인증번호는 건너뛴다.
     */
    @Transactional(noRollbackFor = ManagerException.class)
    public Manager loginByCertificate(java.security.cert.X509Certificate cert) {
        String subjectDn = cert.getSubjectX500Principal().getName();
        Manager manager = managerRepository.findByCertSubjectDn(subjectDn)
                .orElseThrow(() -> new ManagerException("등록되지 않은 인증서입니다. 인증서 등록 후 다시 시도해 주세요."));
        if (!STATUS_ACTIVE.equals(manager.getStatusCode())) {
            throw new ManagerException(statusBlockedMessage(manager.getStatusCode()));
        }
        manager.setLoginFailCount(0);
        manager.setLoginCount((manager.getLoginCount() == null ? 0 : manager.getLoginCount()) + 1);
        manager.setLoginDate(now());
        return managerRepository.save(manager);
    }

    public java.security.cert.X509Certificate parseCertificate(org.springframework.web.multipart.MultipartFile file) {
        return certLoginService.parseCertificate(file);
    }

    /** 로그인 후 "인증서 등록" - 이미 ID/PW+이메일 인증으로 로그인한 관리자가 자기 인증서를 연결한다. */
    @Transactional
    public void registerCertificate(Long managerUserId, java.security.cert.X509Certificate cert) {
        String subjectDn = cert.getSubjectX500Principal().getName();
        managerRepository.findByCertSubjectDn(subjectDn).ifPresent(existing -> {
            if (!existing.getUserId().equals(managerUserId)) {
                throw new ManagerException("이미 다른 계정에 등록된 인증서입니다.");
            }
        });
        Manager manager = managerRepository.findById(managerUserId)
                .orElseThrow(() -> new ManagerException("계정을 찾을 수 없습니다."));
        manager.setCertSubjectDn(subjectDn);
        managerRepository.save(manager);
    }

    @Transactional
    public void unregisterCertificate(Long managerUserId) {
        Manager manager = managerRepository.findById(managerUserId)
                .orElseThrow(() -> new ManagerException("계정을 찾을 수 없습니다."));
        manager.setCertSubjectDn(null);
        managerRepository.save(manager);
    }

    private String statusBlockedMessage(String statusCode) {
        String label = commonCodeService.labelsOf("MANAGER_STATUS").getOrDefault(statusCode, statusCode);
        return "이용할 수 없는 계정입니다. (상태: " + label + ")";
    }

    private static String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }
}
