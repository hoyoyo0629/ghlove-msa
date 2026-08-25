package com.ghlove.member.service;

import com.ghlove.member.domain.UserActionLog;
import com.ghlove.member.repository.UserActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 회원 액션 감사 로그 기록 (AS-IS OP_USER_ACTION_LOG, opmanager/log/user-action-log). */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserActionLogRepository userActionLogRepository;

    @Transactional
    public void recordAction(String loginId, String remoteAddr, String requestUri, String requestMethod) {
        UserActionLog entry = new UserActionLog();
        entry.setLoginId(loginId);
        entry.setRemoteAddr(remoteAddr);
        entry.setRequestUri(requestUri);
        entry.setRequestMethod(requestMethod);
        entry.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        userActionLogRepository.save(entry);
    }
}
