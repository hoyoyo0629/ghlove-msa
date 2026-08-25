package com.ghlove.member.service;

import com.ghlove.member.domain.UserChangeLog;
import com.ghlove.member.domain.UserRole;
import com.ghlove.member.domain.UserRoleId;
import com.ghlove.member.domain.UserRoleRequest;
import com.ghlove.member.repository.UserChangeLogRepository;
import com.ghlove.member.repository.UserRoleRepository;
import com.ghlove.member.repository.UserRoleRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 지자체담당자/제공자 역할 신청·승인 워크플로 (SFR-002). 일반회원은 가입 시 ROLE_USER만
 * 자동 부여되고, ROLE_LOCALGOV/ROLE_PROVIDER는 이 신청→승인 절차를 거쳐야 부여된다.
 */
@Service
@RequiredArgsConstructor
public class RoleRequestService {

    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final List<String> REQUESTABLE_ROLES = List.of("ROLE_LOCALGOV", "ROLE_PROVIDER");
    private static final DateTimeFormatter CHANGE_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserRoleRequestRepository userRoleRequestRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserChangeLogRepository userChangeLogRepository;

    public List<UserRoleRequest> pendingRequests() {
        return userRoleRequestRepository.findByStatusOrderByRequestIdDesc(STATUS_REQUESTED);
    }

    public List<UserRoleRequest> requestsOf(Long userId) {
        return userRoleRequestRepository.findByUserIdOrderByRequestIdDesc(userId);
    }

    @Transactional
    public UserRoleRequest request(Long userId, String requestedRole, String reason) {
        if (userId == null || userId <= 0) {
            throw new MemberException("회원 ID를 입력해 주세요.");
        }
        if (!REQUESTABLE_ROLES.contains(requestedRole)) {
            throw new MemberException("신청할 수 없는 역할입니다.");
        }
        if (userRoleRepository.existsById(new UserRoleId(userId, requestedRole))) {
            throw new MemberException("이미 보유한 역할입니다.");
        }
        if (userRoleRequestRepository.existsByUserIdAndRequestedRoleAndStatus(userId, requestedRole, STATUS_REQUESTED)) {
            throw new MemberException("이미 심사중인 신청이 있습니다.");
        }

        UserRoleRequest request = new UserRoleRequest();
        request.setUserId(userId);
        request.setRequestedRole(requestedRole);
        request.setReason(reason);
        request.setStatus(STATUS_REQUESTED);
        request.setCreatedDate(LocalDateTime.now());
        return userRoleRequestRepository.save(request);
    }

    @Transactional
    public UserRoleRequest approve(Long requestId) {
        UserRoleRequest request = getOrThrow(requestId);
        if (!STATUS_REQUESTED.equals(request.getStatus())) {
            throw new MemberException("심사중인 신청만 승인할 수 있습니다.");
        }
        userRoleRepository.save(new UserRole(request.getUserId(), request.getRequestedRole()));
        request.setStatus(STATUS_APPROVED);
        request.setProcessedDate(LocalDateTime.now());
        recordChangeLog(request.getUserId(), "ROLE_GRANTED: " + request.getRequestedRole());
        return userRoleRequestRepository.save(request);
    }

    @Transactional
    public UserRoleRequest reject(Long requestId) {
        UserRoleRequest request = getOrThrow(requestId);
        if (!STATUS_REQUESTED.equals(request.getStatus())) {
            throw new MemberException("심사중인 신청만 반려할 수 있습니다.");
        }
        request.setStatus(STATUS_REJECTED);
        request.setProcessedDate(LocalDateTime.now());
        return userRoleRequestRepository.save(request);
    }

    private UserRoleRequest getOrThrow(Long requestId) {
        return userRoleRequestRepository.findById(requestId)
                .orElseThrow(() -> new MemberException("역할 신청을 찾을 수 없습니다."));
    }

    /** SFR-002 "주요 행위 기록 감사 로그(...권한 변경...)" - OP_USER_CHANGE_LOG 재사용. */
    private void recordChangeLog(Long userId, String parameter) {
        UserChangeLog log = new UserChangeLog();
        log.setUserId(userId);
        log.setParameter(parameter);
        log.setManagerId(userId);
        log.setCreatedDate(CHANGE_LOG_DATE_FORMAT.format(LocalDateTime.now()));
        userChangeLogRepository.save(log);
    }
}
