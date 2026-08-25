package com.ghlove.admin.service;

import com.ghlove.admin.domain.LoginLog;
import com.ghlove.admin.domain.ManagerActionLog;
import com.ghlove.admin.repository.LoginLogRepository;
import com.ghlove.admin.repository.ManagerActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 관리자 로그인/액션 감사 로그 기록 (AS-IS OP_LOGIN_LOG / OP_MANAGER_ACTION_LOG,
 *  opmanager/log/login-log·action-log). */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String LOGIN_TYPE_MANAGER = "MANAGER";

    private final LoginLogRepository loginLogRepository;
    private final ManagerActionLogRepository managerActionLogRepository;

    @Transactional
    public void recordLogin(String loginId, boolean success, String remoteAddr, String memo) {
        LoginLog entry = new LoginLog();
        entry.setLoginType(LOGIN_TYPE_MANAGER);
        entry.setLoginId(loginId);
        entry.setSuccessFlag(success ? "Y" : "N");
        entry.setRemoteAddr(remoteAddr);
        entry.setMemo(memo);
        entry.setLoginDate(now());
        loginLogRepository.save(entry);
    }

    @Transactional
    public void recordAction(String loginId, String remoteAddr, String requestUri, String requestMethod) {
        ManagerActionLog entry = new ManagerActionLog();
        entry.setLoginType(LOGIN_TYPE_MANAGER);
        entry.setLoginId(loginId);
        entry.setRemoteAddr(remoteAddr);
        entry.setRequestUri(requestUri);
        entry.setRequestMethod(requestMethod);
        entry.setCreatedDate(now());
        managerActionLogRepository.save(entry);
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }
}
