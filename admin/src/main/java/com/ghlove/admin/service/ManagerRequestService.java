package com.ghlove.admin.service;

import com.ghlove.admin.domain.MailConfig;
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
    private final MailConfigService mailConfigService;
    private final com.ghlove.admin.service.integration.EmsMailClient emsMailClient;
    private final LocgovClient locgovClient;

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

    /** AS-IS 승인관리 목록 조회 - 상태·등록일 범위로 거른다(아이디/이름/이메일은 컨트롤러에서). */
    public List<ManagerRequest> search(String confmSttusCode, String startDate, String endDate) {
        return managerRequestRepository.search(confmSttusCode, startDate, endDate);
    }

    public List<ManagerRequest> pending() {
        return managerRequestRepository.findByConfmSttusCodeOrderByFrstRegistPnttmDesc(ManagerRequest.STATUS_PENDING);
    }

    public List<ManagerRequest> history(Long userId) {
        return managerRequestRepository.findByUserIdOrderByReqstSnDesc(userId);
    }

    /**
     * 승인 - AS-IS {@code ManagerRequestServiceImpl.updateManagerRequestApproval} 이식.
     * <b>AS-IS는 승인자가 권한을 고르지 않는다</b> - 신청구분(reqstSeCode)이 그대로 부여 권한이 되고
     * (AS-IS는 OP_USER_ROLE에 ROLE_OPMANAGER + reqstSeCode 두 행을 넣지만, TO-BE op_manager는
     * authority 단일 컬럼이라 reqstSeCode를 그대로 넣는다), 소속 지자체도 신청서의 locgovCode를
     * 그대로 쓴다. 예전 TO-BE가 폼에 넣었던 "부여 권한 드롭다운 / 소속 지자체 입력"은 AS-IS에 없는
     * 발명이라 제거했다([[no-invented-features-ask-first]]).
     *
     * <p>AS-IS는 승인 시 member 계정을 manager로 동기화(기존 member 비밀번호 사용)하지만 TO-BE는
     * op_manager가 member와 분리돼 있어 자격증명을 따로 만들어야 한다(사용자 결정: 메일 통지).
     * 그래서 임시 비밀번호를 발급해 승인 메일에 실어 보낸다 - AS-IS 승인 메일에는 없던 항목이지만,
     * 분리된 인증구조에서 메일 통지를 성립시키는 데 필요한 최소 추가다.
     */
    @Transactional
    public void approve(Long userId, Integer reqstSn, Long approverManagerId, MemberClient.MemberInfo member) {
        ManagerRequest request = managerRequestRepository.findById(new com.ghlove.admin.domain.ManagerRequestId(userId, reqstSn))
                .orElseThrow(() -> new ManagerException("신청 내역을 찾을 수 없습니다."));
        // AS-IS 1-2: 현재 '대기' 상태인지 확인
        if (!ManagerRequest.STATUS_PENDING.equals(request.getConfmSttusCode())) {
            throw new ManagerException("이미 처리된 신청입니다.");
        }
        // AS-IS 1-3: 중복 아이디 체크
        if (managerRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new ManagerException("이미 관리자 계정이 존재합니다.");
        }
        String authority = request.getReqstSeCode();   // AS-IS: 신청구분이 곧 부여 권한

        // AS-IS 2-1: 승인 처리
        request.setConfmSttusCode(ManagerRequest.STATUS_APPROVED);
        request.setLastUpdusrId(approverManagerId);
        request.setLastUpdtPnttm(now());
        managerRequestRepository.save(request);

        // AS-IS 2-2/2-3: 사용자 정보를 관리자 테이블로 이관 + 권한 셋팅
        String tempPassword = generateTempPassword();
        Manager manager = new Manager();
        manager.setLoginId(request.getLoginId());
        manager.setPassword(passwordEncoder.encode(tempPassword));
        manager.setUserName(member != null ? member.userName() : null);
        manager.setEmail(member != null ? member.email() : null);
        manager.setStatusCode("ACTIVE");
        manager.setLoginCount(0);
        manager.setLoginFailCount(0);
        manager.setAuthority(authority);
        manager.setLocgovCode(MenuService.LOCGOV_SCOPED_ROLES.contains(authority) ? request.getLocgovCode() : null);
        manager.setCreatedDate(now());
        manager.setUpdatedDate(now());
        managerRepository.save(manager);

        // AS-IS 3: 메일 발송(승인)
        sendManagerRequestMail("manager_request_approval", request, member, null, tempPassword);
    }

    /** 거절 - AS-IS {@code updateManagerRequestReject} 이식. 대기 상태 + 거절사유 필수 검사 후 메일 통지. */
    @Transactional
    public void reject(Long userId, Integer reqstSn, Long approverManagerId, String reason, MemberClient.MemberInfo member) {
        ManagerRequest request = managerRequestRepository.findById(new com.ghlove.admin.domain.ManagerRequestId(userId, reqstSn))
                .orElseThrow(() -> new ManagerException("신청 내역을 찾을 수 없습니다."));
        if (!ManagerRequest.STATUS_PENDING.equals(request.getConfmSttusCode())) {
            throw new ManagerException("이미 처리된 신청입니다.");
        }
        // AS-IS 1-3: 거절사유 존재 확인
        if (reason == null || reason.trim().isEmpty()) {
            throw new ManagerException("거절사유를 입력해 주세요.");
        }
        request.setConfmSttusCode(ManagerRequest.STATUS_REJECTED);
        request.setRejectResn(reason);
        request.setLastUpdusrId(approverManagerId);
        request.setLastUpdtPnttm(now());
        managerRequestRepository.save(request);

        // AS-IS 3: 메일 발송(거절)
        sendManagerRequestMail("manager_request_reject", request, member, reason, null);
    }

    /**
     * AS-IS {@code ManagerRequestServiceImpl.sendMail} 이식. 템플릿(op_mail_config)이 등록돼 있고
     * buyerSendFlag='Y'이며 수신자 이메일이 있을 때만 발송한다(AS-IS와 동일한 게이트). 토큰
     * {@code {loginId}/{adminRole}/{rejectResn}}을 치환한다. AS-IS는 추가로 member의 이메일 수신동의
     * (receiveEmail=='0')도 보는데, 그 플래그가 TO-BE MemberInfo에 없어 이 조건만 생략했다(미이식).
     * 실제 발송은 EmsMailClient(ghlove.integrations.ems.enabled=false면 로그만)로 나간다.
     */
    private void sendManagerRequestMail(String templateId, ManagerRequest request,
                                        MemberClient.MemberInfo member, String rejectResn, String tempPassword) {
        try {
            if (member == null || member.email() == null || member.email().isBlank()) {
                return;
            }
            MailConfig config = mailConfigService.findByTemplateId(templateId);
            if (config == null || !"Y".equals(config.getBuyerSendFlag())) {
                return;
            }
            String adminRole = adminRoleText(request.getReqstSeCode(), request.getLocgovCode());
            String subject = renderTokens(config.getBuyerSubject(), request.getLoginId(), adminRole, rejectResn);
            String content = renderTokens(config.getBuyerContent(), request.getLoginId(), adminRole, rejectResn);
            if (tempPassword != null) {
                // 분리된 op_manager 인증구조 때문에 필요한 최소 추가(위 approve 주석 참고).
                content = content + "\n\n임시 비밀번호: " + tempPassword;
            }
            String recipients = member.email() + " " + (member.userName() != null ? member.userName() : "");
            emsMailClient.sendMail(subject, content, "D", "", recipients,
                    "관리자권한", "관리자권한", 0);
        } catch (Exception e) {
            // AS-IS도 메일 실패가 승인/거절 자체를 되돌리지 않는다(sendMail은 예외를 삼킨다).
            log.warn("관리자 권한 요청 메일 발송 실패 templateId={} : {}", templateId, e.toString());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagerRequestService.class);

    /** AS-IS {@code ManagerRequestApprovalMail.getAdminRoleText} - 신청구분 → "{구분} {등급}". */
    private String adminRoleText(String reqstSeCode, String locgovCode) {
        if (reqstSeCode == null) {
            return " ";
        }
        String type;
        String grade;
        switch (reqstSeCode) {
            case "ROLE_ADMIN_1" -> { type = "시스템";     grade = "주담당자"; }
            case "ROLE_ADMIN_2" -> { type = "시스템";     grade = "부담당자"; }
            case "ROLE_ADMIN_3" -> { type = "행정안전부"; grade = "주담당자"; }
            case "ROLE_ADMIN_4" -> { type = "행정안전부"; grade = "부담당자"; }
            case "ROLE_ADMIN_5" -> { type = locgovName(locgovCode); grade = "주담당자"; }
            case "ROLE_ADMIN_6" -> { type = locgovName(locgovCode); grade = "부담당자"; }
            case "ROLE_ADMIN_7" -> { type = "오프라인";   grade = "주담당자"; }
            case "ROLE_ADMIN_8" -> { type = "오프라인";   grade = "부담당자"; }
            default -> { type = ""; grade = ""; }   // AS-IS default: 빈 값
        }
        return type + " " + grade;
    }

    private String locgovName(String locgovCode) {
        if (locgovCode == null || locgovCode.isBlank()) {
            return "";
        }
        return locgovClient.allLocgovs().stream()
                .filter(l -> locgovCode.equals(l.locgovCode()))
                .map(l -> (l.upperLocgovNm() + " " + l.locgovNm()).trim())
                .findFirst().orElse("");
    }

    private static String renderTokens(String text, String loginId, String adminRole, String rejectResn) {
        if (text == null) {
            return "";
        }
        return text
                .replace("{loginId}", loginId != null ? loginId : "")
                .replace("{adminRole}", adminRole != null ? adminRole : "")
                .replace("{rejectResn}", rejectResn != null ? rejectResn : "");
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
