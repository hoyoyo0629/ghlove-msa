package com.ghlove.admin.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 관리자 로그인 이메일 2차인증 발송 (AS-IS EmailService - 실제 SMTP 발송). 다른 외부연동과
 * 동일한 enabled=false 모크 패턴 - SMTP 서버가 없는 개발환경에서는 실제 발송 대신 로그만
 * 남기고, 화면에서 바로 테스트할 수 있도록 코드를 반환값에 그대로 노출한다(운영에서
 * enabled=true가 되면 이 노출은 없어져야 한다 - ManagerAuthService에서 enabled 여부로
 * 분기).
 */
@Component
@Slf4j
public class EmailClient {

    public final boolean enabled;

    public EmailClient(@Value("${ghlove.integrations.email.enabled}") boolean enabled) {
        this.enabled = enabled;
    }

    public void sendAuthCode(String toEmail, String authNum) {
        if (!enabled) {
            log.info("[email] disabled - mock 인증번호 발송 to={} code={}", toEmail, authNum);
            return;
        }
        log.warn("[email] enabled=true이지만 실제 SMTP 연동이 구현되어 있지 않습니다. to={}", toEmail);
    }

    /** "이메일 발송"(opmanager/email) 대량메일 도구용 범용 발송. */
    public void send(String toEmail, String subject, String content) {
        if (!enabled) {
            log.info("[email] disabled - mock 메일 발송 to={} subject={}", toEmail, subject);
            return;
        }
        log.warn("[email] enabled=true이지만 실제 SMTP 연동이 구현되어 있지 않습니다. to={}", toEmail);
    }
}
