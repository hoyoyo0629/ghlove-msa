package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.ManagerRequest;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.ManagerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 관리자 권한 요청 (AS-IS G_MNGR_REQST + ManagerRequestController) - 이미 회원가입된
 * 사람이 운영관리 콘솔 접근을 신청하고, 시스템관리자(ROLE_ADMIN)가 승인/거절한다. AS-IS는
 * 로그인 화면에서 아이디/비번을 다시 입력해 신원을 확인하지만, 이 프로젝트는 SFR-010의
 * 공유 JWT로 이미 회원 로그인 상태를 알 수 있어 그걸로 신원을 대신한다.
 */
@Service
@RequiredArgsConstructor
public class ManagerRequestService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ManagerRequestRepository managerRequestRepository;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ManagerRequest submit(Long userId, String loginId, String locgovCode, String reqstSeCode,
                                  String psitnNm, String psitnDeptNm, String ofcpsNm, String cttpc) {
        if (managerRepository.findByLoginId(loginId).isPresent()) {
            throw new ManagerException("이미 관리자 계정이 있습니다. 관리자 로그인을 이용해 주세요.");
        }
        List<ManagerRequest> existing = managerRequestRepository.findByUserIdOrderByReqstSnDesc(userId);
        boolean hasPending = existing.stream().anyMatch(r -> ManagerRequest.STATUS_PENDING.equals(r.getConfmSttusCode()));
        if (hasPending) {
            throw new ManagerException("이미 심사 중인 신청이 있습니다.");
        }

        ManagerRequest request = new ManagerRequest();
        request.setUserId(userId);
        request.setReqstSn(existing.isEmpty() ? 1 : existing.get(0).getReqstSn() + 1);
        request.setLoginId(loginId);
        request.setLocgovCode(locgovCode);
        request.setReqstSeCode(reqstSeCode);
        request.setPsitnNm(psitnNm);
        request.setPsitnDeptNm(psitnDeptNm);
        request.setOfcpsNm(ofcpsNm);
        request.setCttpc(cttpc);
        request.setConfmSttusCode(ManagerRequest.STATUS_PENDING);
        request.setFrstRegisterId(userId);
        request.setFrstRegistPnttm(now());
        return managerRequestRepository.save(request);
    }

    public List<ManagerRequest> pending() {
        return managerRequestRepository.findByConfmSttusCodeOrderByFrstRegistPnttmDesc(ManagerRequest.STATUS_PENDING);
    }

    public List<ManagerRequest> history(Long userId) {
        return managerRequestRepository.findByUserIdOrderByReqstSnDesc(userId);
    }

    /** 승인 - OP_MANAGER 행을 새로 만들고 임시 비밀번호를 발급한다(반환값 - 승인 화면에서
     *  1회 노출, 이 프로젝트의 이메일 모크 정책과 동일하게 실제 메일 전송 대신 화면 표시).
     *  authority는 승인자가 화면에서 직접 고른 AS-IS 실제 6단계 역할 중 하나 - 신청서 자체의
     *  locgovCode(광역단위, 참고용)와 달리 지자체 정/부담당자(ROLE_ADMIN_5/6)의 실제 조회범위
     *  locgovCode는 승인 시점에 승인자가 기초단체 단위로 명시적으로 배정한다. */
    @Transactional
    public String approve(Long userId, Integer reqstSn, Long approverManagerId, String requesterUserName,
                           String requesterEmail, String requesterPhone, String authority, String assignedLocgovCode) {
        ManagerRequest request = managerRequestRepository.findById(new com.ghlove.admin.domain.ManagerRequestId(userId, reqstSn))
                .orElseThrow(() -> new ManagerException("신청 내역을 찾을 수 없습니다."));
        if (!ManagerRequest.STATUS_PENDING.equals(request.getConfmSttusCode())) {
            throw new ManagerException("이미 처리된 신청입니다.");
        }
        if (authority == null || authority.isBlank()) {
            throw new ManagerException("부여할 권한을 선택해 주세요.");
        }
        if (MenuService.LOCGOV_SCOPED_ROLES.contains(authority) && (assignedLocgovCode == null || assignedLocgovCode.isBlank())) {
            throw new ManagerException("지자체 담당자는 소속 지자체를 선택해 주세요.");
        }
        request.setConfmSttusCode(ManagerRequest.STATUS_APPROVED);
        request.setLastUpdusrId(approverManagerId);
        request.setLastUpdtPnttm(now());
        managerRequestRepository.save(request);

        String tempPassword = generateTempPassword();
        Manager manager = new Manager();
        manager.setLoginId(request.getLoginId());
        manager.setPassword(passwordEncoder.encode(tempPassword));
        manager.setUserName(requesterUserName);
        manager.setEmail(requesterEmail);
        manager.setStatusCode("ACTIVE");
        manager.setLoginCount(0);
        manager.setLoginFailCount(0);
        manager.setAuthority(authority);
        manager.setLocgovCode(MenuService.LOCGOV_SCOPED_ROLES.contains(authority) ? assignedLocgovCode : null);
        manager.setCreatedDate(now());
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
        return tempPassword;
    }

    @Transactional
    public void reject(Long userId, Integer reqstSn, Long approverManagerId, String reason) {
        ManagerRequest request = managerRequestRepository.findById(new com.ghlove.admin.domain.ManagerRequestId(userId, reqstSn))
                .orElseThrow(() -> new ManagerException("신청 내역을 찾을 수 없습니다."));
        if (!ManagerRequest.STATUS_PENDING.equals(request.getConfmSttusCode())) {
            throw new ManagerException("이미 처리된 신청입니다.");
        }
        request.setConfmSttusCode(ManagerRequest.STATUS_REJECTED);
        request.setRejectResn(reason);
        request.setLastUpdusrId(approverManagerId);
        request.setLastUpdtPnttm(now());
        managerRequestRepository.save(request);
    }

    private static String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }
}
