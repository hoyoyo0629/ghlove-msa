package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.PrivacyAccessLog;
import com.ghlove.admin.domain.PrivacyAccessLogHist;
import com.ghlove.admin.repository.CommonCodeRepository;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.PrivacyAccessLogHistRepository;
import com.ghlove.admin.repository.PrivacyAccessLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 개인정보 접근로그 기록·조회 (AS-IS saleson.common.controller.CommonController의
 * {@code /common/opmanager/privacy-access-log} 및 {@code privacy-access-log-update} 이식).
 *
 * <p>AS-IS 엑셀 다운로드 흐름은 2단계다 - ① 다운로드 버튼을 누르면 사유 모달이 떠서 사유를
 * {@code privacy-access-log}로 먼저 보내 로그를 남기고(동시에 변경이력 1건도 남긴다),
 * ② 성공하면 그때 실제 다운로드 URL로 이동한다. 그래서 <b>사유 기록은 다운로드를 수행하는
 * 서비스가 아니라 관리자 콘솔(admin)이 담당한다</b>.
 *
 * <p>예전 TO-BE는 order 서비스의 CSV export가 자기 표({@code ord.OD_EXCEL_DOWNLOAD_LOG})에
 * 사유를 쓰고 있어서 1411 화면(개인정보 접근로그를 읽는다)에 아무것도 보이지 않았다 -
 * 사용자 확인 후 기록 지점을 AS-IS와 같은 {@code OP_PRIVACY_ACCESS_LOG}로 옮겼다(2026-10-03).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrivacyAccessLogService {

    /** 1411 목록이 보는 수행업무 - AS-IS {@code PL.TASK = '엑셀 다운로드'}. */
    public static final String TASK_EXCEL_DOWNLOAD = PrivacyTask.EXCEL_DOWNLOAD.getTitle();

    /** AS-IS UserType.MANAGER. */
    private static final String LOGIN_TYPE_MANAGER = "MANAGER";

    /** AS-IS 사유구분 코드타입. */
    private static final String CODE_TYPE_REASON_TYPE = "EXCELDOWNLOAD_REASON_TYPE";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PrivacyAccessLogRepository privacyAccessLogRepository;
    private final PrivacyAccessLogHistRepository privacyAccessLogHistRepository;
    private final ManagerRepository managerRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final EntityManager entityManager;

    /**
     * AS-IS {@code CommonController.privacyAccessLog} - 사유를 받아 접근로그를 남기고, 바로
     * 변경이력 1건도 남긴다(그래서 이력이 2건 이상이면 "수정된 것"으로 읽힌다).
     *
     * @param url 다운로드 URL - AS-IS는 이 URL로 PrivacyAccess를 찾아 요청명/수행업무를 정하고,
     *            등록되지 않은 URL이면 저장을 거부한다.
     */
    @Transactional
    public PrivacyAccessLog record(String url, String reason, String reasonType,
                                   Manager manager, HttpServletRequest request) {
        if (reason == null || reason.isBlank()) {
            throw new ManagerException("엑셀 다운로드 사유를 입력해주세요.");
        }
        PrivacyAccess privacyAccess = PrivacyAccess.findByUrl(url);
        if (privacyAccess == null) {
            throw new ManagerException("개인정보 접근 URL이 아닙니다. 설정파일을 확인해주세요.");
        }

        PrivacyAccessLog log = new PrivacyAccessLog();
        log.setCreatedAt(LocalDateTime.now());
        log.setUrl(url);
        log.setName(privacyAccess.getName());
        log.setMethod(request == null ? null : request.getMethod());
        // AS-IS setDefaultInfo - GET이면 task, 그 외에는 actionTask의 제목을 쓴다
        PrivacyTask task = "GET".equals(log.getMethod())
                ? privacyAccess.getTask() : privacyAccess.getActionTask();
        log.setTask(task == null ? null : task.getTitle());
        log.setIp(clientIp(request));
        log.setLoginType(LOGIN_TYPE_MANAGER);
        log.setManagerId(manager == null ? null : manager.getUserId());
        // AS-IS는 현재 로그인 사용자의 userId를 넣는다 - 관리자 콘솔에서는 관리자 자신이다
        log.setUserId(manager == null ? null : manager.getUserId());
        log.setReason(reason);
        log.setReasonType(reasonType);
        PrivacyAccessLog saved = privacyAccessLogRepository.save(log);

        recordHist(saved.getId(), reason, reasonType, manager);
        return saved;
    }

    /**
     * AS-IS {@code CommonController.privacyAccessLogUpdate} - 변경이력을 남기고 본 행의 사유를 갱신한다.
     * AS-IS는 <b>사유를 등록한 본인만</b> 수정할 수 있다.
     */
    @Transactional
    public void updateReason(Long privacyAccessLogId, String reason, String reasonType, Manager manager) {
        if (reason == null || reason.isBlank()) {
            throw new ManagerException("엑셀 다운로드 사유를 입력해주세요.");
        }
        PrivacyAccessLog target = privacyAccessLogRepository.findById(privacyAccessLogId)
                .orElseThrow(() -> new ManagerException("접근로그를 찾을 수 없습니다."));
        Long managerId = manager == null ? null : manager.getUserId();
        if (managerId == null || !managerId.equals(target.getManagerId())) {
            throw new ManagerException("엑셀 다운로드 사유를 등록한 본인만 수정이 가능합니다.");
        }
        recordHist(privacyAccessLogId, reason, reasonType, manager);
        privacyAccessLogRepository.updateReason(privacyAccessLogId, reason, reasonType);
    }

    private void recordHist(Long privacyAccessLogId, String reason, String reasonType, Manager manager) {
        PrivacyAccessLogHist hist = new PrivacyAccessLogHist();
        hist.setPrivacyAccessLogId(privacyAccessLogId);
        hist.setCreatedAt(LocalDateTime.now());
        hist.setManagerId(manager == null ? null : manager.getUserId());
        hist.setReason(reason);
        hist.setReasonType(reasonType);
        privacyAccessLogHistRepository.save(hist);
    }

    /**
     * 1411 목록 - AS-IS 쿼리대로 TASK='엑셀 다운로드'만, 권한에 따라 본인 것만,
     * 등록일 범위로 걸러 가져온 뒤 화면 표시용 로그인ID·사유구분 라벨을 채운다.
     *
     * [[hql-null-param-needs-cast]]: {@code (:x is null or ...)} 꼴의 Spring Data {@code @Query}는
     * 값이 null일 때 PostgreSQL이 파라미터 타입을 못 정해 거절한다 - cast로 타입을 명시해도
     * Hibernate가 null 값 자체를 bytea로 바인딩해버려 "cannot cast type bytea to bigint"로
     * 또 거절당했다(2026-10-07, 1411 화면 재발). 그래서 다른 레포지토리(QestnarRepository 등)와
     * 같은 "조건부 조립" 방식으로 바꿨다 - 값이 있을 때만 그 조건과 파라미터를 붙이므로 null
     * 바인딩 자체가 없다.
     */
    @Transactional(readOnly = true)
    public List<PrivacyAccessLog> excelDownloadLogs(String startDay, String endDay, Long onlyManagerId) {
        List<PrivacyAccessLog> rows = searchExcelDownloadLogs(
                TASK_EXCEL_DOWNLOAD, onlyManagerId, startOfDay(startDay), endOfDay(endDay));

        Map<String, String> reasonTypeLabels = commonCodeRepository
                .findByCodeTypeOrderByOrdering(CODE_TYPE_REASON_TYPE).stream()
                .collect(Collectors.toMap(CommonCode::getId, CommonCode::getLabel, (a, b) -> a));

        for (PrivacyAccessLog row : rows) {
            if (row.getManagerId() != null) {
                managerRepository.findById(row.getManagerId())
                        .map(Manager::getLoginId)
                        .ifPresent(row::setLoginId);
            }
            row.setReasonTypeName(reasonTypeLabels.get(row.getReasonType()));
        }
        return rows;
    }

    /** AS-IS {@code exceldownload-log-mapper.getExceldownloadLogListByParam}의 안쪽 쿼리를
     *  조건부로 조립한다 - 값이 있는 조건만 붙인다(위 주석 참고). */
    private List<PrivacyAccessLog> searchExcelDownloadLogs(String task, Long managerId,
                                                            LocalDateTime from, LocalDateTime to) {
        StringBuilder jpql = new StringBuilder("select p from PrivacyAccessLog p where p.task = :task");
        if (managerId != null) {
            jpql.append(" and p.managerId = :managerId");
        }
        if (from != null) {
            jpql.append(" and p.createdAt >= :from");
        }
        if (to != null) {
            jpql.append(" and p.createdAt <= :to");
        }
        jpql.append(" order by p.createdAt desc, p.id desc");

        var query = entityManager.createQuery(jpql.toString(), PrivacyAccessLog.class)
                .setParameter("task", task);
        if (managerId != null) {
            query.setParameter("managerId", managerId);
        }
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        return query.getResultList();
    }

    /** AS-IS 사유 수정 팝업의 사유타입 select 옵션 - EXCELDOWNLOAD_REASON_TYPE을 등록순서대로. */
    @Transactional(readOnly = true)
    public Map<String, String> reasonTypeOptions() {
        return commonCodeRepository.findByCodeTypeOrderByOrdering(CODE_TYPE_REASON_TYPE).stream()
                .collect(Collectors.toMap(CommonCode::getId, CommonCode::getLabel, (a, b) -> a, LinkedHashMap::new));
    }

    /** 사유 전문/이력 팝업용 - 라벨까지 채워 돌려준다. */
    @Transactional(readOnly = true)
    public PrivacyAccessLog detail(Long id) {
        PrivacyAccessLog log = privacyAccessLogRepository.findById(id).orElse(null);
        if (log == null) {
            return null;
        }
        log.setReasonTypeName(reasonTypeLabelOf(log.getReasonType()));
        if (log.getManagerId() != null) {
            managerRepository.findById(log.getManagerId()).map(Manager::getLoginId).ifPresent(log::setLoginId);
        }
        return log;
    }

    @Transactional(readOnly = true)
    public List<PrivacyAccessLogHist> histOf(Long privacyAccessLogId) {
        List<PrivacyAccessLogHist> rows =
                privacyAccessLogHistRepository.findByPrivacyAccessLogIdOrderByCreatedAtDesc(privacyAccessLogId);
        for (PrivacyAccessLogHist row : rows) {
            row.setReasonTypeName(reasonTypeLabelOf(row.getReasonType()));
            if (row.getManagerId() != null) {
                managerRepository.findById(row.getManagerId()).map(Manager::getLoginId).ifPresent(row::setLoginId);
            }
        }
        return rows;
    }

    public long histCountOf(Long privacyAccessLogId) {
        return privacyAccessLogHistRepository.countByPrivacyAccessLogId(privacyAccessLogId);
    }

    private String reasonTypeLabelOf(String reasonType) {
        if (reasonType == null) {
            return null;
        }
        return commonCodeRepository.findByCodeTypeOrderByOrdering(CODE_TYPE_REASON_TYPE).stream()
                .filter(c -> reasonType.equals(c.getId()))
                .map(CommonCode::getLabel)
                .findFirst()
                .orElse(null);
    }

    /** AS-IS CommonUtils.getClientIp - 프록시 헤더를 먼저 본다. */
    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headers = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR" };
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                int comma = value.indexOf(',');
                return comma > 0 ? value.substring(0, comma).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static LocalDateTime startOfDay(String yyyymmdd) {
        LocalDate date = parseDay(yyyymmdd);
        return date == null ? null : date.atStartOfDay();
    }

    /** AS-IS는 종료일을 '235959'로 잇는다 - 같은 뜻의 경계값. */
    private static LocalDateTime endOfDay(String yyyymmdd) {
        LocalDate date = parseDay(yyyymmdd);
        return date == null ? null : date.atTime(LocalTime.of(23, 59, 59));
    }

    private static LocalDate parseDay(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(yyyymmdd, DAY);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
