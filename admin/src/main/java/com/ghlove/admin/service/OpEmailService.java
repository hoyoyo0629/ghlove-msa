package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.OpEmail;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.OpEmailRepository;
import com.ghlove.admin.service.integration.EmailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 이메일 발송 (AS-IS opmanager/email) - admin 자신의 관리자 계정 대상 임의 메일 작성/발송. */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpEmailService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String STATUS_SENT = "S";
    private static final String STATUS_FAILED = "F";
    /** AUTH_TARGET은 VARCHAR(1)이라 한 글자로 관리자/역할 대상을 구분한다. */
    public static final String TARGET_ALL = "A";
    public static final String TARGET_ADMIN = "D";
    public static final String TARGET_OPERATOR = "O";

    private final OpEmailRepository opEmailRepository;
    private final ManagerRepository managerRepository;
    private final EmailClient emailClient;

    public List<OpEmail> list() {
        return opEmailRepository.findAllByOrderByEmailIdDesc();
    }

    public OpEmail get(Long emailId) {
        return opEmailRepository.findById(emailId).orElseThrow(() -> new ManagerException("메일을 찾을 수 없습니다."));
    }

    /** 대상(전체 관리자 / ROLE_ADMIN / ROLE_OPERATOR)에게 즉시 발송한다 - EmailClient가
     *  enabled=false 모크면 실제 발송 없이 로그만 남긴다(관리자 로그인 2FA와 동일 관행). */
    @Transactional
    public OpEmail sendNow(String subject, String content, String authTarget, Long senderId) {
        String roleFilter = TARGET_ADMIN.equals(authTarget) ? "ROLE_ADMIN"
                : TARGET_OPERATOR.equals(authTarget) ? "ROLE_OPERATOR" : null;
        List<Manager> targets = managerRepository.findAll().stream()
                .filter(m -> roleFilter == null || roleFilter.equals(m.getAuthority()))
                .filter(m -> m.getEmail() != null && !m.getEmail().isBlank())
                .toList();

        OpEmail email = new OpEmail();
        email.setSubject(subject);
        email.setContent(content);
        email.setAuthTarget(authTarget);
        email.setFrstRegisterId(senderId);
        email.setFrstRegistPnttm(LocalDateTime.now());

        boolean anyFailed = false;
        for (Manager target : targets) {
            try {
                emailClient.send(target.getEmail(), subject, content);
            } catch (RuntimeException e) {
                anyFailed = true;
                log.warn("Email send failed to {}", target.getEmail(), e);
            }
        }
        email.setStatus(anyFailed ? STATUS_FAILED : STATUS_SENT);
        email.setSendDate(DATE_FORMAT.format(LocalDateTime.now()));
        return opEmailRepository.save(email);
    }
}
