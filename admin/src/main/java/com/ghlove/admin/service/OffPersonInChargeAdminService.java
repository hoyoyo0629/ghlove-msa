package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * D8 오프라인담당자 관리 (AS-IS OffPersonInChargeManagerController,
 * docs/as-is-admin-gap-deep-audit-part2.md 배치D D8). AS-IS는 ROLE_ADMIN_7(오프라인주담당자)/
 * ROLE_ADMIN_8(오프라인부담당자)이라는 별도 권한티어를 갖고 있었는데 이 프로젝트의 TO-BE
 * OP_ROLE에는 없었다 - 시드 INSERT로 새로 추가했다(DB 시딩은 이 라운드 SQL 참고). 지자체
 * 정/부담당자(D6)와 달리 승인요청 큐(ManagerRequestController)를 거치지 않고 관리자가
 * 화면에서 직접 즉시 계정을 만든다(AS-IS "등록" 버튼 = 바로 OP_MANAGER insert).
 */
@Service
@RequiredArgsConstructor
public class OffPersonInChargeAdminService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_LOCKED = "LOCKED";

    public static final String OFF_MAIN = "ROLE_ADMIN_7";
    public static final String OFF_SUB = "ROLE_ADMIN_8";
    private static final int MAIN_LIMIT = 2;

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    public SearchResult search(String locgovCode, String keyword, int page, int size) {
        List<Manager> all = managerRepository.findAllByOrderByUserIdDesc().stream()
                .filter(m -> OFF_MAIN.equals(m.getAuthority()) || OFF_SUB.equals(m.getAuthority()))
                .toList();
        if (locgovCode != null && !locgovCode.isBlank()) {
            all = all.stream().filter(m -> locgovCode.equals(m.getLocgovCode())).toList();
        }
        if (keyword != null && !keyword.isBlank()) {
            String v = keyword.trim();
            all = all.stream().filter(m -> (m.getLoginId() != null && m.getLoginId().contains(v))
                    || (m.getUserName() != null && m.getUserName().contains(v))).toList();
        }
        int totalElements = all.size();
        int totalPages = (int) Math.ceil(totalElements / (double) Math.max(size, 1));
        int from = Math.min(Math.max(page, 0) * size, totalElements);
        int to = Math.min(from + size, totalElements);
        return new SearchResult(all.subList(from, to), totalElements, totalPages);
    }

    public Manager get(Long userId) {
        return managerRepository.findById(userId)
                .orElseThrow(() -> new ManagerException("오프라인담당자 계정을 찾을 수 없습니다."));
    }

    public boolean loginIdAvailable(String loginId) {
        return loginId != null && !loginId.isBlank() && managerRepository.findByLoginId(loginId).isEmpty();
    }

    /** 등록 - 승인절차 없이 즉시 계정을 만든다. */
    @Transactional
    public String create(String loginId, String userName, String email, String authority, String locgovCode,
                          String bankCode, String empId, String psitnNm, String psitnDeptNm, String ofcpsNm) {
        if (loginId == null || loginId.isBlank()) {
            throw new ManagerException("아이디를 입력해 주세요.");
        }
        if (!loginIdAvailable(loginId)) {
            throw new ManagerException("이미 사용 중인 아이디입니다.");
        }
        validateAuthority(authority);
        if (locgovCode == null || locgovCode.isBlank()) {
            throw new ManagerException("소속 지자체를 선택해 주세요.");
        }
        if (OFF_MAIN.equals(authority)) {
            enforceMainLimit(locgovCode, null);
        }
        String tempPassword = generateTempPassword();
        Manager manager = new Manager();
        manager.setLoginId(loginId);
        manager.setPassword(passwordEncoder.encode(tempPassword));
        manager.setUserName(userName);
        manager.setEmail(email);
        manager.setStatusCode(STATUS_ACTIVE);
        manager.setLoginCount(0);
        manager.setLoginFailCount(0);
        manager.setAuthority(authority);
        manager.setLocgovCode(locgovCode);
        manager.setBankCode(bankCode);
        manager.setEmpId(empId);
        manager.setPsitnNm(psitnNm);
        manager.setPsitnDeptNm(psitnDeptNm);
        manager.setOfcpsNm(ofcpsNm);
        manager.setCreatedDate(now());
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
        return tempPassword;
    }

    /** 수정 - 주담당자 2명제한 + 권한이관(주↔부 전환). */
    @Transactional
    public void update(Long userId, String userName, String email, String authority, String locgovCode,
                        String statusCode, String bankCode, String empId, String psitnNm, String psitnDeptNm,
                        String ofcpsNm) {
        Manager manager = get(userId);
        validateAuthority(authority);
        if (locgovCode == null || locgovCode.isBlank()) {
            throw new ManagerException("소속 지자체를 선택해 주세요.");
        }
        if (OFF_MAIN.equals(authority) && !(OFF_MAIN.equals(manager.getAuthority()) && locgovCode.equals(manager.getLocgovCode()))) {
            enforceMainLimit(locgovCode, userId);
        }
        manager.setUserName(userName);
        manager.setEmail(email);
        manager.setAuthority(authority);
        manager.setLocgovCode(locgovCode);
        manager.setBankCode(bankCode);
        manager.setEmpId(empId);
        manager.setPsitnNm(psitnNm);
        manager.setPsitnDeptNm(psitnDeptNm);
        manager.setOfcpsNm(ofcpsNm);
        if (statusCode != null && !statusCode.isBlank()) {
            manager.setStatusCode(statusCode);
            if (STATUS_ACTIVE.equals(statusCode)) {
                manager.setLoginFailCount(0);
            }
        }
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
    }

    @Transactional
    public void delete(Long userId, Long currentManagerUserId) {
        if (userId.equals(currentManagerUserId)) {
            throw new ManagerException("본인 계정은 삭제할 수 없습니다.");
        }
        if (!managerRepository.existsById(userId)) {
            throw new ManagerException("오프라인담당자 계정을 찾을 수 없습니다.");
        }
        managerRepository.deleteById(userId);
    }

    @Transactional
    public String resetPassword(Long userId) {
        Manager manager = get(userId);
        String tempPassword = generateTempPassword();
        manager.setPassword(passwordEncoder.encode(tempPassword));
        manager.setLoginFailCount(0);
        if (STATUS_LOCKED.equals(manager.getStatusCode())) {
            manager.setStatusCode(STATUS_ACTIVE);
        }
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
        return tempPassword;
    }

    private void validateAuthority(String authority) {
        if (!OFF_MAIN.equals(authority) && !OFF_SUB.equals(authority)) {
            throw new ManagerException("오프라인 주담당자/부담당자 중에서 선택해 주세요.");
        }
    }

    private void enforceMainLimit(String locgovCode, Long excludeUserId) {
        long count = managerRepository.findAllByOrderByUserIdDesc().stream()
                .filter(m -> OFF_MAIN.equals(m.getAuthority()))
                .filter(m -> locgovCode.equals(m.getLocgovCode()))
                .filter(m -> excludeUserId == null || !m.getUserId().equals(excludeUserId))
                .count();
        if (count >= MAIN_LIMIT) {
            throw new ManagerException("이 지자체의 오프라인 주담당자는 최대 " + MAIN_LIMIT + "명까지 지정할 수 있습니다.");
        }
    }

    private static String generateTempPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
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

    public record SearchResult(List<Manager> content, long totalElements, int totalPages) {
    }
}
