package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.OpEmail;
import com.ghlove.admin.domain.OpEmailDetail;
import com.ghlove.admin.domain.OpEmailFile;
import com.ghlove.admin.domain.Role;
import com.ghlove.admin.repository.CommonCodeRepository;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.OpEmailDetailRepository;
import com.ghlove.admin.repository.OpEmailFileRepository;
import com.ghlove.admin.repository.OpEmailRepository;
import com.ghlove.admin.repository.RoleRepository;
import com.ghlove.admin.service.integration.EmsMailClient;
import com.ghlove.admin.web.support.EmailDetailParam;
import com.ghlove.admin.web.support.EmailParam;
import com.ghlove.admin.web.support.EmailSendTarget;
import com.ghlove.admin.web.support.SendParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 이메일 발송 (AS-IS saleson.shop.email.EmailServiceImpl 이식).
 *
 * AS-IS 흐름: 등록(OP_EMAIL STATUS='R' + 첨부파일 + 대상권한) → 발송(대상 조회 → 30,000명 단위로
 * 끊어 EMS에 요청 → STATUS를 S/F로 갱신) → 목록·상세 진입 시 EMS 리포트 집계로 STATUS를
 * C(전송완료)/P(전송실패)/T(일부성공)로 재갱신.
 *
 * <b>EMS 리포트는 TO-BE에 없다</b> - AS-IS는 발송결과 집계(총/성공/실패 건수)와 발송인원 명단을
 * 외부 EMS 시스템의 별도 DB({@code EMS_REPORT}, {@code EMS_REPORT_DETAIL} - CUBRID 'ems'
 * 인스턴스, AS-IS는 emsDS라는 두 번째 datasource로 붙는다)에서 읽는다. 그 DB는 이 프로젝트에
 * 구성돼 있지 않아 집계는 비어 있고, 따라서 상태도 발송요청(R/S/F)에 머문다. AS-IS의 상태 재갱신
 * 규칙은 {@link #updateEmailStatus(List)}에 그대로 옮겨 뒀으니 datasource가 붙으면 그대로 동작한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpEmailService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** AS-IS 코드 타입 - 목록/상세의 발송상태 라벨. */
    private static final String CODE_TYPE_EMAIL_STATUS = "EMAIL_STATUS";

    /** AS-IS insertEmail이 넣는 최초 상태(발송요청). */
    private static final String STATUS_REQUESTED = "R";
    private static final String STATUS_REQUEST_SUCCESS = "S";
    private static final String STATUS_REQUEST_FAIL = "F";
    private static final String STATUS_SENT_ALL = "C";
    private static final String STATUS_SENT_NONE = "P";
    private static final String STATUS_SENT_PARTIAL = "T";

    /** AS-IS authTarget - A:권한별 S:답례품 E:개별 L:로그인인증이메일. */
    public static final String TARGET_AUTHORITY = "A";
    public static final String TARGET_SELLER = "S";
    public static final String TARGET_EACH = "E";
    public static final String TARGET_LOGIN = "L";

    /** AS-IS sendEmail의 분할 단위. */
    private static final int SEND_PARTITION_SIZE = 30000;

    /**
     * AS-IS OP_MANAGER / OP_USER의 "정상" 상태코드는 '9'인데, TO-BE는 같은 의미를 'ACTIVE'로 쓴다
     * (MANAGER_STATUS는 TO-BE 전용 코드타입). 대상 추출 조건은 AS-IS와 같은 뜻으로 맞춘다.
     */
    private static final String STATUS_CODE_ACTIVE = "ACTIVE";

    private final OpEmailRepository opEmailRepository;
    private final OpEmailDetailRepository opEmailDetailRepository;
    private final OpEmailFileRepository opEmailFileRepository;
    private final ManagerRepository managerRepository;
    private final RoleRepository roleRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final EmailAttachmentStorageService attachmentStorage;
    private final EmsMailClient emsMailClient;
    private final GiftClient giftClient;
    private final MemberAdminClient memberAdminClient;

    /** AS-IS emailMapper.getEmailListCount. */
    public int getEmailListCount(EmailParam searchParam) {
        return searchAll(searchParam).size();
    }

    /** AS-IS EmailServiceImpl.getEmailList - 조회 후 EMS 리포트로 상태를 재갱신한다. */
    @Transactional
    public List<OpEmail> getEmailList(EmailParam searchParam) {
        List<OpEmail> list = searchAll(searchParam);
        updateEmailStatus(list);
        list.forEach(this::fillDisplayFields);
        return list;
    }

    private List<OpEmail> searchAll(EmailParam searchParam) {
        // AS-IS sqlEmailWhere의 검색어 분기는 searchType이 'S'/'C'일 때만 타는데 화면이 보내는
        // 값은 SUBJECT/CONTENT라 어떤 경우에도 걸리지 않았고, 'C'(내용) 분기마저 SUBJECT를
        // LIKE했다 - 사용자 확인 후 화면이 보내는 값대로 제목/내용을 각각 보도록 고쳤다(2026-10-03).
        String keyword = blankToNull(searchParam.getSearchContent());
        String subjectKeyword = "SUBJECT".equals(searchParam.getSearchType()) ? keyword : null;
        String contentKeyword = "CONTENT".equals(searchParam.getSearchType()) ? keyword : null;
        return opEmailRepository.search(subjectKeyword, contentKeyword,
                blankToNull(searchParam.getSearchStartDate()),
                blankToNull(searchParam.getSearchEndDate()));
    }

    /** AS-IS 목록 쿼리가 조인으로 채우는 두 칸 - 발송상태 라벨과 발송자 이름. */
    private void fillDisplayFields(OpEmail email) {
        email.setStatusName(statusLabels().get(email.getStatus()));
        if (email.getFrstRegisterId() != null) {
            managerRepository.findById(email.getFrstRegisterId())
                    .ifPresent(manager -> email.setUserName(manager.getUserName()));
        }
    }

    private Map<String, String> statusLabels() {
        return commonCodeRepository.findByCodeTypeOrderByOrdering(CODE_TYPE_EMAIL_STATUS).stream()
                .collect(Collectors.toMap(CommonCode::getId, CommonCode::getLabel, (a, b) -> a));
    }

    /**
     * AS-IS EmailServiceImpl.insertEmail - OP_EMAIL(STATUS='R') → 첨부파일 → 대상권한 순서다.
     * SEND_DATE는 즉시(D)면 지금, 지정(R)이면 화면이 만든 yyyyMMddHHmmss를 그대로 쓴다.
     */
    @Transactional
    public OpEmail insertEmail(OpEmail email, MultipartFile files) {
        LocalDateTime now = LocalDateTime.now();
        email.setEmailId(null);
        email.setStatus(STATUS_REQUESTED);
        if ("D".equals(email.getSendType()) || email.getSendDate() == null || email.getSendDate().isBlank()) {
            email.setSendDate(DATE_TIME_FORMAT.format(now));
        }
        email.setFrstRegistPnttm(now);
        email.setLastUpdusrId(email.getFrstRegisterId());
        email.setLastUpdtPnttm(now);
        OpEmail saved = opEmailRepository.save(email);

        if (files != null && !files.isEmpty()) {
            String storedName = attachmentStorage.store(files);
            OpEmailFile file = new OpEmailFile();
            file.setEmailId(saved.getEmailId());
            file.setFileName(storedName);
            file.setOrgFileName(files.getOriginalFilename());
            file.setFileTy(attachmentStorage.extensionOf(files.getOriginalFilename()));
            file.setOrdering(opEmailFileRepository.nextOrdering(saved.getEmailId()));
            file.setCreatedDate(DATE_TIME_FORMAT.format(now));
            opEmailFileRepository.save(file);
        }

        if (TARGET_AUTHORITY.equals(email.getAuthTarget())) {
            for (OpEmailDetail detail : email.getAuthList()) {
                if (detail.getAuthority() == null || detail.getAuthority().isBlank()) {
                    continue;
                }
                opEmailDetailRepository.save(new OpEmailDetail(saved.getEmailId(), detail.getAuthority()));
            }
        }
        return saved;
    }

    /**
     * AS-IS EmailServiceImpl.sendEmail - 대상 목록을 만들어 30,000명 단위로 EMS에 요청하고
     * 마지막에 STATUS를 갱신한다. 예외 처리 분기도 AS-IS 그대로다(RuntimeException은 로그만
     * 남기고 상태를 바꾸지 않는다 - 그러면 발송요청 상태 R에 머문다).
     */
    @Transactional
    public boolean sendEmail(SendParam param) {
        boolean result = true;
        OpEmail email = getEmailDetail(param.getEmailId());
        try {
            List<EmailSendTarget> userList = null;
            if (TARGET_AUTHORITY.equals(email.getAuthTarget())) {
                userList = sendUserList(email);
            } else if (TARGET_EACH.equals(email.getAuthTarget())) {
                userList = param.getSendUserList();
            } else if (TARGET_SELLER.equals(email.getAuthTarget())) {
                userList = sendSellerUserList();
            } else if (TARGET_LOGIN.equals(email.getAuthTarget())) {
                // AS-IS 주석: srhan. Login email send - 화면에 없는 내부 발송 경로
                userList = param.getSendUserList();
            }

            if (userList != null && !userList.isEmpty()) {
                for (int i = 0; i < userList.size(); i += SEND_PARTITION_SIZE) {
                    List<EmailSendTarget> partition =
                            userList.subList(i, Math.min(i + SEND_PARTITION_SIZE, userList.size()));
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < partition.size(); j++) {
                        EmailSendTarget target = partition.get(j);
                        sb.append(j == 0 ? "" : ",")
                          .append(target.getEmail()).append(' ').append(target.getUserName());
                    }
                    sendEmailData(email, sb.toString());
                }
            }

            email.setStatus(STATUS_REQUEST_SUCCESS);
        } catch (RuntimeException e) {
            log.error("sendMail Error !!! {}", e.toString());
        } catch (Exception e) {
            email.setStatus(STATUS_REQUEST_FAIL);
            result = false;
            log.error("sendMail Error !!! {}", e.toString());
        } finally {
            opEmailRepository.findById(email.getEmailId()).ifPresent(stored -> {
                stored.setStatus(email.getStatus());
                opEmailRepository.save(stored);
            });
        }
        return result;
    }

    /** AS-IS sendEmailData - categoryNm/linkNm까지 AS-IS 값 그대로다. */
    private void sendEmailData(OpEmail email, String recipients) throws Exception {
        String categoryName = TARGET_LOGIN.equals(email.getAuthTarget()) ? "로그인인증이메일" : "포인트만료";
        emsMailClient.sendMail(email.getSubject(), email.getContent(),
                email.getSendType(), email.getSendDate(), recipients,
                categoryName, categoryName, email.getEmailId());
    }

    /**
     * AS-IS emailMapper.sendUserList - 고른 권한그룹에 속하고 이메일이 있는 정상 관리자다.
     * AS-IS는 OP_USER_ROLE을 조인하는데 TO-BE는 OP_MANAGER.AUTHORITY가 직접 권한을 들고 있다.
     */
    private List<EmailSendTarget> sendUserList(OpEmail email) {
        List<String> authorities = opEmailDetailRepository.findByEmailId(email.getEmailId()).stream()
                .map(OpEmailDetail::getAuthority)
                .toList();
        if (authorities.isEmpty()) {
            return List.of();
        }
        return managerRepository.findAll().stream()
                .filter(m -> authorities.contains(m.getAuthority()))
                .filter(m -> STATUS_CODE_ACTIVE.equals(m.getStatusCode()))
                .filter(m -> m.getEmail() != null && !m.getEmail().isBlank())
                .map(m -> new EmailSendTarget(m.getUserName(), m.getEmail()))
                .toList();
    }

    /** AS-IS emailMapper.sendSellerUserList - gift가 같은 조건으로 걸러 내려준다. */
    private List<EmailSendTarget> sendSellerUserList() {
        return giftClient.sellerEmailSendTargets().stream()
                .map(t -> new EmailSendTarget(t.userName(), t.email()))
                .toList();
    }

    /**
     * AS-IS emailMapper.getUserList - 개별 발송의 수신자 검색이다. AS-IS는 회원 표(OP_USER)를
     * 이름 완전일치(암호화된 이름으로)로 찾고, 상태가 정상이며 이름·이메일이 비어있지 않은 회원만
     * 돌려준다. 회원은 member 소유라 조회 API를 거친다(member는 이름 부분일치로 걸러주므로
     * AS-IS와 같게 완전일치만 남긴다).
     */
    public List<EmailSendTarget> getUserList(String userName) {
        if (userName == null || userName.isBlank()) {
            return List.of();
        }
        // member의 회원검색 API는 page가 0부터다(MemberAdminController도 0을 기본값으로 쓴다).
        return memberAdminClient.search(null, null, "USER_NAME", userName, null, null, 0, 1000)
                .content().stream()
                .filter(row -> userName.equals(row.userName()))
                .filter(row -> STATUS_CODE_ACTIVE.equals(row.statusCode()))
                .filter(row -> row.email() != null && !row.email().isBlank())
                .map(row -> new EmailSendTarget(row.userName(), row.email()))
                .toList();
    }

    /** AS-IS EmailServiceImpl.getEmailDetail - 상세 + 대상권한 + 첨부파일, 그리고 상태 재갱신. */
    @Transactional
    public OpEmail getEmailDetail(long emailId) {
        OpEmail email = opEmailRepository.findById(emailId)
                .orElseThrow(() -> new ManagerException("메일을 찾을 수 없습니다."));
        updateEmailStatus(List.of(email));
        fillDisplayFields(email);

        Map<String, String> roleNames = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getAuthority, Role::getRoleName, (a, b) -> a));
        List<OpEmailDetail> authList = opEmailDetailRepository.findByEmailId(emailId);
        authList.forEach(detail -> detail.setRoleName(roleNames.get(detail.getAuthority())));
        email.setAuthList(authList);
        email.setFileList(opEmailFileRepository.findByEmailIdOrderByOrdering(emailId));
        return email;
    }

    /** AS-IS emailMapper.getEmailFile. */
    public OpEmailFile getEmailFile(int emailFileId) {
        return opEmailFileRepository.findById(emailFileId).orElse(null);
    }

    /* ------------------------------------------------------------------ *
     * EMS 리포트 - 외부 EMS DB(EMS_REPORT / EMS_REPORT_DETAIL) 소관.
     * TO-BE에 그 datasource가 없어 비어 있다(0건/null). 꾸며서 성공으로 보이게 하지 않는다.
     * ------------------------------------------------------------------ */

    /**
     * AS-IS emsMapper.getEmsTotalCnt - 메일 한 건의 총/성공/실패 건수.
     * EMS 리포트 DB가 없어 집계가 없다(null). 상태도 C/P/T가 되지 않으니 상세화면의
     * 발송결과 영역 자체가 뜨지 않는다 - AS-IS에서 리포트가 아직 없을 때와 같은 모습이다.
     */
    public EmsTotalCount getEmsTotalCnt(long emailId) {
        return null;
    }

    /** AS-IS emsMapper.getEmsUserCnt. */
    public int getEmsUserCnt(EmailDetailParam param) {
        return 0;
    }

    /** AS-IS emsMapper.getEmsUserList - 발송인원 명단(이름/이메일/실발송시각/발송여부). */
    public List<EmailSendTarget> getEmsUserList(EmailDetailParam param) {
        return List.of();
    }

    /**
     * EMS 리포트 DB가 붙어 있는지 - 지금은 EMS 연계 자체가 꺼져 있으면 리포트도 없다고 본다.
     * 리포트 전용 datasource가 생기면 이 판단만 바꾸면 된다.
     */
    private boolean emsReportAvailable() {
        return emsMailClient.isEnabled();
    }

    /**
     * AS-IS EmailServiceImpl.updateEmailStatus - 발송요청성공(S) 건들을 EMS 리포트 집계와 맞춰
     * 전체성공이면 C, 전체실패면 P, 그 외(일부성공)면 T로 바꾼다. 집계가 아직 총건수에 못 미치면
     * (발송 진행 중) 건너뛴다. 규칙을 그대로 보존했고, 지금은 집계가 비어 있어 아무것도 바뀌지 않는다.
     */
    private void updateEmailStatus(List<OpEmail> list) {
        List<OpEmail> searchList = list.stream()
                .filter(e -> STATUS_REQUEST_SUCCESS.equals(e.getStatus()))
                .toList();
        if (searchList.isEmpty()) {
            return;
        }
        Map<Long, EmsTotalCount> report = getEmsReport(searchList);
        if (report.isEmpty()) {
            return;
        }
        Map<String, String> labels = statusLabels();
        for (OpEmail email : list) {
            EmsTotalCount total = report.get(email.getEmailId());
            if (total == null) {
                continue;
            }
            if (total.totCnt() > total.succCnt() + total.failCnt()) {
                continue;
            }
            String code;
            if (total.totCnt() == total.succCnt()) {
                code = STATUS_SENT_ALL;
            } else if (total.totCnt() == total.failCnt()) {
                code = STATUS_SENT_NONE;
            } else {
                code = STATUS_SENT_PARTIAL;
            }
            email.setStatus(code);
            email.setStatusName(labels.get(code));
            opEmailRepository.findById(email.getEmailId()).ifPresent(stored -> {
                stored.setStatus(code);
                opEmailRepository.save(stored);
            });
        }
    }

    /** AS-IS emsMapper.getEmsReport - MEMO(=EMAIL_ID)별 집계. 리포트 DB가 없어 빈 맵이다. */
    private Map<Long, EmsTotalCount> getEmsReport(List<OpEmail> list) {
        if (!emsReportAvailable()) {
            return new HashMap<>();
        }
        return new HashMap<>();
    }

    /** AS-IS saleson.shop.email.domain.EmsTotalCnt. */
    public record EmsTotalCount(long emailId, int totCnt, int succCnt, int failCnt) {
    }

    /** AS-IS 등록 폼이 체크박스로 넘기는 authList[i].authority를 엔티티 목록으로 만든다. */
    public static List<OpEmailDetail> toAuthList(List<String> authorities) {
        List<OpEmailDetail> list = new ArrayList<>();
        if (authorities == null) {
            return list;
        }
        for (String authority : authorities) {
            if (authority != null && !authority.isBlank()) {
                OpEmailDetail detail = new OpEmailDetail();
                detail.setAuthority(authority);
                list.add(detail);
            }
        }
        return list;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
